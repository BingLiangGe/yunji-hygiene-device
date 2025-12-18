package com.yunji.hygiene.handler.strategy.tran;

import com.yunji.hygiene.entity.domain.req.jt808.TransMsg;
import com.yunji.hygiene.entity.domain.req.trans.PurchaseTransMsg;
import com.yunji.hygiene.entity.dto.EnterCommandDTO;
import com.yunji.hygiene.entity.enums.TransEnum;

/**
 * OpenShippingStrategy：开出货 / 购买出货 指令策略
 *
 * 作用：
 * - 根据业务侧传入的“出货明细”(dtoList) 组装成 PurchaseTransMsg
 * - 设置透传消息类型为 SHOPPING（设备端一般理解为：执行出货/开出货仓/电机转动等动作）
 * - 返回给上层统一封装为 JT808 8900 下发报文
 *
 * 典型场景：
 * - 用户下单支付成功后，后台下发出货指令（包含：格子号/商品数量/出货组合等）
 * - 工厂测试：指定某几个格子执行出货动作
 *
 * 注意：
 * - 该策略只负责“构造透传消息体”，真正的 JT808 头、流水号、校验、转义由后续 Encoder 完成
 */
public class OpenShippingStrategy implements ITransMsgStrategy {

    /**
     * 构造“出货/购买”透传消息
     *
     * @param cmd 命令入参（包含 dtoList：出货对/出货明细）
     * @return PurchaseTransMsg（继承 TransMsg），后续会被统一封装并通过 TCP 下发
     */
    @Override
    public TransMsg strategyTranMsg(EnterCommandDTO cmd) {

        // 1）构造购买/出货消息体（该消息体通常带有“出货对列表/商品对列表”）
        PurchaseTransMsg rs = new PurchaseTransMsg();

        // 2）设置透传类型：SHOPPING（具体字节值由枚举 TransEnum 定义，设备端据此识别命令类型）
        rs.setMessageType(TransEnum.SHOPPING.getIssueType());

        // 3）设置出货明细列表（例如：格子号/数量/组合对等，具体结构由 cmd.getDtoList() 决定）
        // 设备端会根据 pairList 逐项执行出货动作
        rs.setPairList(cmd.getDtoList());

        // 4）返回透传消息体
        return rs;
    }
}
