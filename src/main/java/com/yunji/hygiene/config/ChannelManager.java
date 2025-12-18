package com.yunji.hygiene.config;

import com.yunji.hygiene.entity.domain.DataPacket;
import com.yunji.hygiene.entity.domain.DeviceChannel;
import com.yunji.hygiene.entity.domain.DeviceException;
import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.enums.DeviceErrorEnum;
import com.yunji.hygiene.entity.enums.TransEnum;
import com.yunji.hygiene.handler.strategy.jt808.AbsChannelReadHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ChannelManager：统一管理设备连接通道
 *
 * 核心目标：
 * - 维护 imei -> Channel 的一对一关系（同一 IMEI 只保留最新连接）
 * - 支持快速判断在线、获取通道、断线清理
 * - 支持服务关闭前广播通知设备（例如 PING/下线通知）
 *
 * 设计要点：
 * - ChannelGroup：保存当前所有连接，便于遍历广播、统一关闭、按 ChannelId 查找
 * - channelMap：保存 imei -> DeviceChannel（包含 imei 与 Channel 引用）映射
 * - TERMINAL_IMEI：把 imei 绑定到 channel.attr 上，断开时可反查 imei
 * - lock：保证 add/remove 的原子性，避免并发下映射不一致或误删
 * TODO    1、AttributeKey<String> test= AttributeKey.valueOf("imei");
 *         2、连接关闭时 没有 removeByChannel()，channelMap 不会删日志也写反了：imei 不为空也会打“IMEI为空”的 error
 *         3、channelGroup 会自动剔除关闭 channel
 */
@Slf4j
public class ChannelManager {

    /** Channel Attribute：每条连接绑定一个 imei（设备唯一标识） */
    public static final AttributeKey<String> TERMINAL_IMEI = AttributeKey.newInstance("imei");


    /** 全部连接集合：用于广播、遍历、按 channelId 查找 */
    private final static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /** imei -> DeviceChannel 映射（保证一对一：同 IMEI 只保留最新 channel） */
    private final static Map<String, DeviceChannel> channelMap = new ConcurrentHashMap<>();

    /** add/remove 互斥锁：保证映射一致性（channelGroup + channelMap 原子更新） */
    private final static ReentrantLock lock = new ReentrantLock();

    /** 标记旧连接：用于避免 remove 时误删新连接 */
    public static final String OLD_CHANNEL_SUFFIX = "_OLD";


    /**
     * closeFuture 监听器：
     * - 连接关闭后通常需要清理 channelMap / channelGroup
     * - 注意：你这里 listener 目前只打印日志，没有调用 removeByChannel 做真正清理
     * //TODO会有问题
     */
    public static final ChannelFutureListener remover = future -> {
        String imei = future.channel().attr(TERMINAL_IMEI).get();
        log.error("移除channel过程中发生错误, IMEI为空: {}", imei);
    };

    /** 从 channel 属性中取 imei（连接已绑定后才有值） */
    public static String getImei(Channel channel) {
        return channel.attr(TERMINAL_IMEI).get();
    }

    /**
     * 添加设备连接（同 IMEI 只保留最新连接）
     * - 如果发现 imei 已存在旧连接：关闭旧连接，替换为新连接
     * - 新连接写入 channelGroup + channelMap，并在 channel.attr 里绑定 imei
     */
    public static boolean add(String imei, Channel channel) {
        lock.lock();
        try {
            boolean added = channelGroup.add(channel);

            // 如果 imei 已存在，说明同设备重复连接：关闭旧连接
            DeviceChannel existing = channelMap.get(imei);
            if (existing != null) {
                Channel old = existing.getChannel();
                if (old != null && old != channel) {
                    log.info("检测到重复IMEI: {}, 关闭旧channel, oldId={}, newId={}", imei, old.id(), channel.id());

                    // 移除旧连接的关闭监听（避免旧连接关闭时触发误处理）
                    old.closeFuture().removeListener(remover);

                    // 从 group 里移除并关闭
                    channelGroup.remove(old);

                    // 标记旧连接 imei，防止 removeByChannel 时误删新映射
                    old.attr(TERMINAL_IMEI).set(imei + OLD_CHANNEL_SUFFIX);

                    old.close();
                }
            }

            // 绑定 imei 到新连接，便于断开时反查
            channel.attr(TERMINAL_IMEI).set(imei);

            // 给新连接加关闭监听（用于关闭时处理清理逻辑）
            channel.closeFuture().addListener(remover);

            // 更新映射：imei -> 最新 channel
            channelMap.put(imei, new DeviceChannel(imei, channel));

            log.info("IMEI:{} channel添加成功, 当前在线数: {}", imei, channelMap.size());
            return added;
        } finally {
            lock.unlock();
        }
    }

    /** 判断设备在线：map 有值且 group 中能找到该 channel */
    public static boolean online(String imei) {
        boolean isOnline = channelMap.containsKey(imei) && getChannel(imei) != null;
        log.info("设备IMEI:{} 在线状态: {}", imei, isOnline ? "在线" : "离线");
        return isOnline;
    }

    /** 离线则抛异常（业务层常用：发指令前校验） */
    public static void onlineException(String imei) {
        if (!online(imei)) {
            log.error("设备IMEI:{} 离线，抛出异常", imei);
            throw new DeviceException(DeviceErrorEnum.CHECKED_202504, imei);
        }
    }

    /** 按 imei 移除连接（内部转为按 channel 移除） */
    public static void removeByImei(String imei) {
        removeByChannel(getChannel(imei));
    }

    /**
     * 按 channel 移除连接（幂等 + 防误删）
     * - 如果该 channel 的 imei 为空或已标记为 OLD：跳过
     * - 只在“map 中保存的 channel == 当前 channel”时才真正删除，避免误删新连接
     */
    public static void removeByChannel(Channel channel) {
        lock.lock();
        try {
            if (channel != null) {
                String imei = channel.attr(TERMINAL_IMEI).get();
                if (imei == null || imei.endsWith(OLD_CHANNEL_SUFFIX)) {
                    log.info("跳过清理: 该channel已过时或未绑定imei, id={}", channel.id());
                    return;
                }

                DeviceChannel deviceChannel = channelMap.get(imei);
                if (deviceChannel != null && deviceChannel.getChannel() == channel) {
                    channelGroup.remove(channel);
                    channelMap.remove(imei);
                    channel.attr(TERMINAL_IMEI).set(null);
                    log.info("已移除IMEI:{} 的channel, 当前在线数: {}", imei, channelMap.size());
                }
            }
        } catch (Exception e) {
            log.error("移除channel过程中发生错误", e);
        } finally {
            lock.unlock();
        }
    }

    /** 按 imei 获取 Channel：先从 map 找，再从 group 按 id 校验存在性 */
    public static Channel getChannel(String imei) {
        DeviceChannel deviceChannel = channelMap.get(imei);
        if (deviceChannel == null) {
            log.warn("IMEI:{} 对应的channel未找到", imei);
            return null;
        }
        return channelGroup.find(deviceChannel.getChannel().id());
    }

    /**
     * 获取所有在线设备映射（返回 imei -> Channel）
     * - 过滤掉 group 中已不存在的 channel
     */
    public static Map<String, Channel> getChannelMap() {
        Map<String, Channel> maps = new HashMap<>();
        Set<Map.Entry<String, DeviceChannel>> entries = channelMap.entrySet();
        for (Map.Entry<String, DeviceChannel> entry : entries) {
            Channel channel = getChannel(entry.getKey());
            if (channel != null) {
                maps.put(entry.getKey(), channel);
            }
        }
        log.info("当前在线设备数:{} , IMEI列表: {}", maps.size(), maps.keySet());
        return maps;
    }

    /**
     * 服务关闭前通知所有设备（广播）
     * - 这里构造一个 PING/下线通知类透传包，逐个 writeAndFlush
     */
    public static void notifyAllDevicesBeforeShutdown() {
        EmptyTransMsg pingMsg = new EmptyTransMsg();
        pingMsg.setEventId(-1);
        pingMsg.setMessageType(TransEnum.PING.getIssueType());

        for (Channel channel : channelGroup) {
            String imei = channel.attr(TERMINAL_IMEI).get();
            DataPacket packet = AbsChannelReadHandler.convertTransMsg(pingMsg.getEventId(), channel, imei, pingMsg);
            channel.writeAndFlush(packet);
            log.info("已向设备通知: imei={}", imei);
        }
    }
}
