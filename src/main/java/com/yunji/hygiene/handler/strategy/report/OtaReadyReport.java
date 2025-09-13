package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.constant.DeviceConstant;
import com.yunji.hygiene.entity.domain.req.trans.OtaTransMsg;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.UpgradeResp;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.enums.TransEnum;
import com.yunji.hygiene.entity.po.UpgradeFilePO;
import com.yunji.hygiene.service.DeviceFileCache;
import com.yunji.hygiene.service.DeviceService;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;


/**
 * @author : peter-zhu
 * @date : 2025/2/13 13:51
 * @description : TODO
 **/
@Service
@Slf4j
public class OtaReadyReport extends AbsTranReportMsg {

    @Resource
    private DeviceService deviceService;

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        UpgradeResp resp = (UpgradeResp) msg;
        if (DeviceConstant.SUCCESS.equals(resp.getResult())) {

            OtaTransMsg otaTransMsg = new OtaTransMsg();
            otaTransMsg.setEventId(-1);
            log.info("OtaReadyReport handleReport msg :{}", JsonUtil.toJsonString(msg));
            UpGradeFileDTO upGradeFileData = DeviceFileCache.getInfo(msg.getHeader().getImei());
            log.info("OtaReadyReport handleReport success :{}", upGradeFileData);
            UpgradeFilePO file = deviceService.getFile(upGradeFileData.getFileId());
            deviceService.startUpgrade(upGradeFileData.getInfoId());
            byte[] fileBytes = file.getPack();
            otaTransMsg.setMessageType(TransEnum.OTA_DATA_RECEIVE.getIssueType());
            otaTransMsg.setMessageLength(fileBytes.length);
            //otaTransMsg.setPackageLength((short) PACKET_SIZE);
            otaTransMsg.setOtaId((byte) 1);
            byte[] chunk = Arrays.copyOfRange(fileBytes, 0, PACKET_SIZE);
            otaTransMsg.setChunk(chunk);
            return new TransReportDTO(true, true, otaTransMsg);
        } else
            log.error("OtaReadyReport handleReport fail upgrade data:{}", JsonUtil.toJsonString(resp));
        return null;
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new UpgradeResp(byteBuf);
    }
}
