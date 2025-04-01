package com.yunji.hygiene.handler.strategy.report;

import com.yunji.hygiene.entity.enums.TransReportEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : peter-zhu
 * @date : 2025/2/13 13:56
 * @description : TODO
 **/
@Service
@Slf4j
public class TransReportFactory {
    private static final Map<String, ITranReportMsg> strategies = new HashMap<>();


    @Resource
    private VersionInfoReport versionInfoReport;
    @Resource
    private WetWipeInfoReport wetWipeInfoReport;
    @Resource
    private OtaReadyReport otaReadyAck;
    @Resource
    private OtaDataReceiveReport otaDataReceiveAck;
    @Resource
    private OtaDataEopReport otaDataEopAck;
    @Resource
    private HygieneInfoReport hygieneInfoReport;


    @PostConstruct
    public void init() {
        strategies.put(TransReportEnum.VERSION_INFO.getCmd(), versionInfoReport);
        strategies.put(TransReportEnum.WET_WIPE_INFO.getCmd(), wetWipeInfoReport);
        strategies.put(TransReportEnum.HYGIENE_INFO.getCmd(), hygieneInfoReport);
        strategies.put(TransReportEnum.OTA_READY.getCmd(), otaReadyAck);
        strategies.put(TransReportEnum.OTA_DATA_RECEIVE.getCmd(), otaDataReceiveAck);
        strategies.put(TransReportEnum.OTA_DATA_EOP.getCmd(), otaDataEopAck);
    }

    public static ITranReportMsg getStrategy(byte messageType) {
        String command = TransReportEnum.getCmdByType(messageType);
        return strategies.get(command);
    }
}
