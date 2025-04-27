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
 * @Author: peter
 * @Date: 2025-01-16
 * @Description: 使用ChannelGroup管理Channel, 维护imei->ChannelId->Channel 一对一映射关系
 * @Version: 1.0
 */
@Slf4j
public class ChannelManager {
    public static final AttributeKey<String> TERMINAL_IMEI = AttributeKey.newInstance("imei");
    private final static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final static Map<String, DeviceChannel> channelIdMap = new ConcurrentHashMap<>();
    private final static ReentrantLock lock = new ReentrantLock();

    //public static final AttributeKey<UpGradeFileData> FILE_DATA_KEY = AttributeKey.valueOf("fileData");

    public static final ChannelFutureListener remover = future -> {
        try {
//            String imei = future.channel().attr(TERMINAL_IMEI).get();
//            DeviceChannel deviceChannel = channelIdMap.get(imei);
//            if (deviceChannel != null && deviceChannel.getChannelId() == future.channel().id()) {
//                Channel channel = channelGroup.find(deviceChannel.getChannelId());
//    //            byte[] byteBuf = channel.attr(ITranReportMsg.FILE_DATA_KEY).get();
//    //            if (byteBuf != null)
//    //                channel.attr(ITranReportMsg.FILE_DATA_KEY).set(null);
//                channelGroup.remove(channel);
//                channelIdMap.remove(imei);
//            }
            String imei = future.channel().attr(TERMINAL_IMEI).get();
            DeviceChannel deviceChannel = channelIdMap.get(imei);
            if (deviceChannel != null && deviceChannel.getChannelId() == future.channel().id()) {
                Channel channel = channelGroup.find(deviceChannel.getChannelId());
                if (channel != null) {
                    channelGroup.remove(channel);
                    channel.attr(TERMINAL_IMEI).set(null); // <-- 关键，加上
                }
                channelIdMap.remove(imei);
            }
        } catch (Exception e) {
            log.error("Error during channel removal", e);
        }
    };

    public static String getImei(Channel channel) {
        return channel.attr(TERMINAL_IMEI).get();
    }

    public static boolean add(String imei, Channel channel) {
        lock.lock();
        try {
            boolean added = channelGroup.add(channel);
            if (added) {
                if (channelIdMap.containsKey(imei)) {
                    Channel old = getChannel(imei);
                    if (old != null) {
                        old.closeFuture().removeListener(remover);
                        old.close();
                    }
                }
                channel.attr(TERMINAL_IMEI).set(imei);
                channel.closeFuture().addListener(remover);
                channelIdMap.put(imei, new DeviceChannel(imei, channel.id()));
            }
            return added;
        } finally {
            lock.unlock();
        }
    }

    public static boolean online(String imei) {
        return channelIdMap.containsKey(imei) && getChannel(imei) != null;
    }

    public static void onlineException(String imei) {
        if (!online(imei))
            throw new DeviceException(DeviceErrorEnum.CHECKED_202504, imei);
    }

    public static void remove(String imei) {
        lock.lock();
        try {
            Channel channel = getChannel(imei);
            if (channel != null) {
                channel.attr(TERMINAL_IMEI).set(null);
                channel.close();
            }
        } finally {
            lock.unlock();
        }
    }

    public static Channel getChannel(String imei) {
        DeviceChannel deviceChannel = channelIdMap.get(imei);
        if (deviceChannel == null)
            return null;
        return channelGroup.find(deviceChannel.getChannelId());
    }

    public static Map<String, Channel> getChannelMap() {
        Map<String, Channel> maps = new HashMap<>();
        Set<Map.Entry<String, DeviceChannel>> entries = channelIdMap.entrySet();
        for (Map.Entry<String, DeviceChannel> entry : entries) {
            Channel channel = getChannel(entry.getKey());
            maps.put(entry.getKey(), channel);
        }
        return maps;
    }

    public static void notifyAllDevicesBeforeShutdown() {
        EmptyTransMsg pingMsg = new EmptyTransMsg();
        pingMsg.setEventId(-1);
        pingMsg.setMessageType(TransEnum.PING.getIssueType());
        for (Channel channel : channelGroup) {
            String imei = channel.attr(TERMINAL_IMEI).get();
            DataPacket packet = AbsChannelReadHandler.convertTransMsg(pingMsg.getEventId(), channel, imei, pingMsg);
            channel.writeAndFlush(packet);
            log.info("notify success imei:{}", imei);
        }
    }
}
