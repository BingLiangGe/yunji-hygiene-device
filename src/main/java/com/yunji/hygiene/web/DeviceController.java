package com.yunji.hygiene.web;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.config.HMACAuth;
import com.yunji.hygiene.constant.DeviceCacheCode;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.response.Response;
import com.yunji.hygiene.response.ResponseHelper;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.service.SystemUtil;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.Map;

import static java.util.concurrent.TimeUnit.HOURS;


/**
 * @author : peter-zhu
 * @date : 2024/12/28 16:40
 * @description : TODO
 **/
@RestController
@RequestMapping("/device/hygiene")
@Slf4j
public class DeviceController {

    @Resource
    private DeviceCallService deviceCallService;

    @Resource
    private DeviceService deviceService;

    @HMACAuth
    @GetMapping(value = "/test")
    public Response<?> test() {
        return ResponseHelper.success();
    }

    @HMACAuth
    @PostMapping(value = "/testHmacAuth")
    public Response<?> testHmacAuth(@RequestBody @Valid EnterCommandDTO cmd) {
        System.out.println(cmd.getImei());
        return ResponseHelper.success();
    }

    @HMACAuth
    @PostMapping(value = "/command")
    public Response<String> command(@RequestBody @Valid EnterCommandDTO cmd) {
//        boolean ping = deviceCallService.ping(cmd.getImei(), false);
//        if (ping) {
        boolean command = deviceCallService.command(cmd);
        if (command)
            return ResponseHelper.success();
        //    }
        return ResponseHelper.failure("指令下达失败:" + cmd.getImei());
    }

    @HMACAuth
    @PostMapping(value = "/upgrade")
    public Response<String> upgrade(@RequestBody @Valid UpgradeCommandDTO cmd) {
        log.debug("DeviceController upgrade :{}", JsonUtil.toJsonString(cmd));
        boolean upgradeCache = deviceService.createUpgradeCache(cmd);
        boolean ping = deviceCallService.ping(cmd.getImei(), false);
        if (upgradeCache && ping) {
            boolean command = deviceCallService.command(cmd);
            if (command)
                return ResponseHelper.success();
        }
        return ResponseHelper.failure("升级失败:" + cmd.getImei());
    }

//    @PostMapping(value = "/handle")
//    public Response<String> handle(@RequestBody HygieneHandleDTO handle) {
//        boolean command = deviceCallService.handle(handle);
//        if (command)
//            return ResponseHelper.success();
//        return ResponseHelper.failure("处理失败:" + handle.getImei());
//    }

    @GetMapping(value = "/status/{imei}")
    public boolean status(@PathVariable String imei) {
        boolean online = ChannelManager.online(imei);
        log.info("hygiene device online status imei:{},online:{}", imei, online);
//        if (!online)
//            deviceService.updateStatus(imei, false);
//        if (online) {
            boolean ack = deviceCallService.ping(imei, true);
            log.info("hygiene device ping ack imei:{},online:{}", imei, ack);
            return ack;
//        }
//        return false;
    }

    @HMACAuth
    @GetMapping(value = "/online/{imei}")
    public boolean online(@PathVariable String imei) {
        deviceService.cabinetOnline(imei, true, true);
        return true;
    }

    @HMACAuth
    @GetMapping(value = "/offline/{imei}")
    public boolean offline(@PathVariable String imei) {
        deviceService.cabinetOffline(imei, true, true);
        return true;
    }

    //@HMACAuth
    @GetMapping(value = "/statusList")
    public Map<String, Channel> statusList() {
        return ChannelManager.getChannelMap();
    }

    @GetMapping(value = "/sleepList/{imei}")
    public boolean sleepList(@PathVariable String imei) {
        String[] split = imei.split(",");
        for (String s : split) {
            SystemUtil.redisCache.set(DeviceCacheCode.DEVICE_SLEEP + s, new Date(), 2000, HOURS);
            log.info("{}休眠成功", s);
        }
        return true;
    }

    @GetMapping(value = "/deleteSleep/{imei}")
    public boolean deleteSleep(@PathVariable String imei) {
        String[] split = imei.split(",");
        for (String s : split) {
            SystemUtil.redisCache.delete(DeviceCacheCode.DEVICE_SLEEP + s);
            log.info("{}删除休眠", s);
        }
        return true;
    }
}
