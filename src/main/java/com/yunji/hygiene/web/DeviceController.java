package com.yunji.hygiene.web;

import com.yunji.hygiene.config.ChannelManager;
import com.yunji.hygiene.entity.dto.HygieneCommandDTO;
import com.yunji.hygiene.entity.dto.UpgradeCommandDTO;
import com.yunji.hygiene.entity.po.UpgradeFilePO;
import com.yunji.hygiene.response.Response;
import com.yunji.hygiene.response.ResponseHelper;
import com.yunji.hygiene.service.DeviceCallService;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;


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

    @GetMapping(value = "/test")
    public Response<?> test() {
        UpgradeFilePO file = deviceService.getFile(11L);
        System.out.println(file);
        return ResponseHelper.success();
    }

    @PostMapping(value = "/command")
    public Response<String> command(@RequestBody @Valid HygieneCommandDTO cmd) {
        boolean ping = deviceCallService.ping(cmd.getImei(), false);
        if (ping) {
            boolean command = deviceCallService.command(cmd);
            if (command)
                return ResponseHelper.success();
        }
        return ResponseHelper.failure("指令下达失败:" + cmd.getImei());
    }

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
        log.info("ProxyController status params:{}", imei);
        boolean online = ChannelManager.online(imei);
        if (online)
            return deviceCallService.ping(imei, true);
        return false;
    }

    @GetMapping(value = "/statusList")
    public Map<String, Channel> statusList() {
        return ChannelManager.getChannelMap();
    }
}
