package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.domain.req.trans.EmptyTransMsg;
import com.yunji.hygiene.entity.domain.req.trans.OtaTransMsg;
import com.yunji.hygiene.entity.domain.resp.report.ReportMsg;
import com.yunji.hygiene.entity.domain.resp.report.UpgradeResp;
import com.yunji.hygiene.entity.dto.TransReportDTO;
import com.yunji.hygiene.entity.dto.UpGradeFileDTO;
import com.yunji.hygiene.entity.enums.TransEnum;
import com.yunji.hygiene.entity.po.UpgradeFilePO;
import com.yunji.hygiene.service.DeviceFileCache;
import com.yunji.hygiene.util.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author : peter-zhu
 * @date : 2025/2/13 13:53
 * @description : TODO
 **/
@Service
@Slf4j
public class OtaDataReceiveReport extends AbsTranReportMsg {

    @Override
    public TransReportDTO handleReport(ChannelHandlerContext ctx, ReportMsg msg) {
        UpgradeResp r = (UpgradeResp) msg;
        log.info("OtaDataReceiveReport UpgradeResp :{}", JsonUtil.toJsonString(r));
        UpGradeFileDTO upGradeFileData = DeviceFileCache.getInfo(msg.getHeader().getImei());
        log.info("OtaDataReceiveReport handleReport :{}", upGradeFileData);
        if (upGradeFileData != null) {
            UpgradeFilePO file = deviceService.getFile(upGradeFileData.getFileId());
            byte[] fileBytes = file.getPack();
            OtaTransMsg otaTransMsg = new OtaTransMsg();
            otaTransMsg.setEventId(-1);
            otaTransMsg.setOtaId((byte) (r.getOtaId() + 1));
            otaTransMsg.setMessageType(TransEnum.OTA_DATA_RECEIVE.getIssueType());
            otaTransMsg.setMessageLength(fileBytes.length);
            int otaId = r.getOtaId();
            int startIndex = otaId * PACKET_SIZE; // 每个 OTA_ID 对应的起始位置
            int chunkSize = startIndex + PACKET_SIZE;
            int endIndex = Math.min(chunkSize, fileBytes.length); // 最后一个片段可能小于 segmentSize
            byte[] segment;
            if (endIndex < chunkSize) {
                segment = new byte[PACKET_SIZE];
                int remainingBytes = fileBytes.length - startIndex;  // 剩余的实际字节数
                // 小于0代表结束
                if (remainingBytes < 0) {
                    EmptyTransMsg otaEop = new EmptyTransMsg();
                    otaEop.setEventId(-1);
                    otaEop.setMessageType(TransEnum.OTA_DATA_EOP.getIssueType());
                    deviceService.finishTask(upGradeFileData.getImei());
                    return new TransReportDTO(true, true, otaEop);
                }
                log.info("OtaDataReceiveReport final startIndex:{},chunkSize:{}, fileBytes length:{},remainingBytes{}",
                        startIndex, chunkSize, fileBytes.length, remainingBytes);
                System.arraycopy(fileBytes, startIndex, segment, 0, remainingBytes);
                deviceService.finishTask(upGradeFileData.getImei());
            } else {
                // 完整的片段，直接截取
                segment = Arrays.copyOfRange(fileBytes, startIndex, endIndex);
            }
            otaTransMsg.setPackageLength((short) (endIndex - startIndex));
            otaTransMsg.setChunk(segment);
            return new TransReportDTO(true, true, otaTransMsg);
        }
        return null;
    }

    @Override
    public ReportMsg getMsg(ByteBuf byteBuf) {
        return new UpgradeResp(byteBuf);
    }
}
