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
 * @Author: peter
 * @Date: 2025-01-16
 * @Description:
 * @Version: 1.0
 */

@Slf4j
@Component
public class NettyTcpServer {

    @Value("${netty.port}")
    private int port;

    @Autowired
    @Qualifier("bossGroup")
    private NioEventLoopGroup bossGroup;

    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    @Autowired
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    @Autowired
    private JT808ChannelInitializer jt808ChannelInitializer;


    /**
     * 启动Server
     *
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(jt808ChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, 1024) //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .childOption(ChannelOption.TCP_NODELAY, true)//立即写出
                .childOption(ChannelOption.SO_KEEPALIVE, true);//长连接
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);//内存泄漏检测 开发推荐PARANOID 线上SIMPLE
        ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
        if (channelFuture.isSuccess()) {
            log.info("TCP服务启动完毕,port={}", this.port);
        }
    }
    /**
     * 销毁资源并唤醒设备
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
