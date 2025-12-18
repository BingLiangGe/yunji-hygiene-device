package com.yunji.hygiene.server;

import com.yunji.hygiene.config.ChannelManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * NettyTcpServer：Netty TCP 服务启动类（JT808 TCP 接入层）
 *
 * 一、这个类在系统中的定位
 * - 属于“设备接入层 TCP Server”
 * - 负责监听端口、接受设备连接、初始化 pipeline（编码/解码/心跳/业务处理器）
 * - Spring Boot 启动后自动启动 Netty（@PostConstruct）
 * - Spring Boot 停机时自动触发优雅关闭（@PreDestroy）
 *
 * 二、线程模型说明（非常关键）
 * 1) bossGroup（accept线程）
 * - 只负责接受连接（accept），把连接分配给 workerGroup
 * - 绝对不能做耗时/阻塞操作（否则影响新连接建立）
 *
 * 2) workerGroup（IO线程）
 * - 负责 Socket 的读写、触发 ChannelPipeline 的事件传播
 * - 同样不允许做耗时业务（数据库、HTTP、Redis 等），否则会拖慢 IO
 *
 * 3) businessGroup（业务线程池）
 * - 专门用于“耗时业务逻辑”的执行线程
 * - 例如：ReportMsgHandler 中涉及数据库操作，所以你在 initializer 里把 readHandler 放 businessGroup
 *
 * 三、Netty 启动流程（start 方法）
 * - 创建 ServerBootstrap
 * - group(bossGroup, workerGroup) 绑定线程模型
 * - channel(NioServerSocketChannel.class) 指定服务端 channel 类型（NIO）
 * - childHandler(jt808ChannelInitializer) 为每条新连接初始化 pipeline
 * - option(...) 配置服务端 socket 参数
 * - childOption(...) 配置子连接（每条 TCP 连接）的参数
 * - bind(port).sync() 绑定端口并阻塞等待绑定完成
 *
 * 四、关键参数解释
 * 1) SO_REUSEADDR=true
 * - 允许端口复用，解决快速重启时 TIME_WAIT 导致的端口占用问题
 * - 常用于服务端快速发布/重启
 *
 * 2) SO_BACKLOG=1024
 * - 服务端 accept 队列长度（对应 TCP listen backlog）
 * - 高并发连接时，如果 backlog 太小，可能出现连接被拒绝或延迟
 *
 * 3) TCP_NODELAY=true
 * - 关闭 Nagle 算法，避免小包合并导致的延迟（减少“指令下发延迟”）
 * - 对实时指令/心跳类业务更友好
 *
 * 4) SO_KEEPALIVE=true
 * - TCP 层保活（内核级 keepalive），用于检测半开连接
 * - 但它不是应用层心跳：超时周期由系统参数决定，不能替代 IdleStateHandler 心跳机制
 *
 * 五、ResourceLeakDetector 内存泄漏检测
 * - 主要检测 ByteBuf 是否被正确 release
 * - 开发环境可用 PARANOID（最严格，开销大）
 * - 线上推荐 SIMPLE（开销小，能发现明显泄漏）
 *
 * 六、优雅停机流程（shutdown 方法）
 * 1) 先广播通知设备（notifyAllDevicesBeforeShutdown）
 * - 目的是：服务端停机前，给所有在线设备发一个“唤醒/心跳/下线通知”之类的包
 * - 这样设备可以尽快感知服务要下线，主动重连或进入安全状态（视协议设计）
 *
 * 2) shutdownGracefully()
 * - bossGroup：停止接受新连接
 * - workerGroup：停止 IO 事件循环
 * - businessGroup：停止业务线程池（等待已提交任务执行完/超时）
 *
 * 注意：
 * - 当前代码是“同步等待关闭完成 syncUninterruptibly”，保证停机过程完整
 * - 停机期间如果业务线程池还在跑耗时任务，会延长停机时间（这是符合优雅停机语义的）
 */
@Slf4j
@Component
public class NettyTcpServer {

    /** TCP监听端口 */
    @Value("${netty.port}")
    private int port;

    /** boss 线程组：accept 新连接 */
    @Autowired
    @Qualifier("bossGroup")
    private NioEventLoopGroup bossGroup;

    /** worker 线程组：处理 socket IO 读写 */
    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    /** 业务线程池：处理耗时业务逻辑（DB/HTTP/Redis 等） */
    @Autowired
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    /** 每条连接的 pipeline 初始化器（Idle + Frame + Decoder + Encoder + Handler） */
    @Autowired
    private JT808ChannelInitializer jt808ChannelInitializer;

    /**
     * Spring 启动完成后自动启动 TCP Server
     * bind().sync() 会阻塞直到绑定成功
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(jt808ChannelInitializer)

                // ====== 服务端参数（ServerSocketChannel）======
                .option(ChannelOption.SO_REUSEADDR, true) // 端口复用，快速重启
                .option(ChannelOption.SO_BACKLOG, 1024)   // accept 队列长度

                // ====== 子连接参数（SocketChannel）======
                .childOption(ChannelOption.TCP_NODELAY, true) // 关闭Nagle，小包低延迟
                .childOption(ChannelOption.SO_KEEPALIVE, true); // TCP保活

        // ByteBuf 泄漏检测：线上 SIMPLE，开发可 PARANOID
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);

        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        if (channelFuture.isSuccess()) {
            log.info("TCP服务启动完毕,port={}", this.port);
        }
    }

    /**
     * Spring 容器关闭前执行：
     * - 广播通知设备（可用于唤醒/告知下线/触发重连）
     * - 优雅关闭 Netty 线程池（等待任务结束）
     * TODO 在 start() 里把 ChannelFuture 保存成成员变量（比如 serverChannelFuture 或 serverChannel），在 shutdown() 时先 serverChannel.close()，再 shutdownGracefully，这样停机会更完整、更可控（尤其是你后续如果要支持“重启 Netty 服务”）
     */
    @PreDestroy
    public void shutdown() {
        log.info("正在关闭 Netty 服务，准备广播唤醒所有设备...");
        ChannelManager.notifyAllDevicesBeforeShutdown();
        log.info("正在关闭 Netty 服务，已经广播唤醒所有设备...");

        bossGroup.shutdownGracefully().syncUninterruptibly();
        log.info("已关闭netty boss线程");

        workerGroup.shutdownGracefully().syncUninterruptibly();
        log.info("已关闭netty work线程");

        businessGroup.shutdownGracefully().syncUninterruptibly();
        log.info("已关闭netty business线程");

        log.info("NettyTcpServer 关闭成功");
    }
}
