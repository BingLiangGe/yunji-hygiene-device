package com.yunji.hygiene.config;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Netty 线程组配置（Boss / Worker / Business）
 *
 * - bossGroup：只负责 accept（建立 TCP 连接），绝对不能被阻塞
 * - workerGroup：负责 Socket 读写（I/O 事件），绝对不能被阻塞
 * - businessGroup：用于业务处理线程池（Handler 中包含 DB/HTTP/Redis/文件IO 等阻塞操作时，把业务逻辑丢到这里执行）
 *
 * 典型 pipeline 用法：
 * - ctx.channel().pipeline().addLast(businessGroup, "bizHandler", new XxxHandler());
 *  TODO  建议把 Bean 改成带销毁方法，避免停机后线程不退出：
 */
@Configuration
public class EventLoopGroupConfig {

    /** boss 线程数：处理连接建立（accept） */
    @Value("${netty.threads.boss}")
    private int bossThreadsNum;

    /** worker 线程数：处理网络读写（read/write） */
    @Value("${netty.threads.worker}")
    private int workerThreadsNum;

    /** business 线程数：处理业务阻塞任务（DB/HTTP/Redis/磁盘等） */
    @Value("${netty.threads.business}")
    private int businessThreadsNum;

    /**
     * bossGroup：负责 TCP 连接建立（accept）
     * 注意：不要在 boss 线程里做任何耗时/阻塞操作
     */
    @Bean(name = "bossGroup")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreadsNum);
    }

    /**
     * workerGroup：负责 Socket 读写（I/O 事件）
     * 注意：不要在 worker 线程里做数据库/HTTP 等阻塞操作，否则会影响所有连接的读写延迟
     */
    @Bean(name = "workerGroup")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerThreadsNum);
    }

    /**
     * businessGroup：业务线程池（执行耗时/阻塞逻辑）
     * - 在 ChannelPipeline 中添加 handler 时可以指定该 group，让 handler 事件在业务线程池中执行
     * - 适合 DB 查询、远程调用、复杂计算、文件操作等
     */
    @Bean(name = "businessGroup")
    public EventExecutorGroup businessGroup() {
        return new DefaultEventExecutorGroup(businessThreadsNum);
    }
}
