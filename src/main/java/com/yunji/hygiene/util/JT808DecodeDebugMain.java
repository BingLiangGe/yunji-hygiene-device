//package com.yunji.hygiene.util;
//
//import com.yunji.hygiene.entity.domain.resp.report.WetWipeInfoReportResp;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufUtil;
//import io.netty.buffer.Unpooled;
//
//public class JT808DecodeDebugMain {
//
//    public static void main(String[] args) {
//        String hex = "090000144ed43fa20ef3008e070000000d000d000000000100010000440101052f";
//
//        ByteBuf bb = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
//
//        // 这一步模拟：Decoder 校验通过后，已经把 checksum 从 writerIndex 剪掉了
//        // 你这里为了复现，就手动把最后1字节当 checksum 丢掉（不做 XOR 也行）
//        byte checksum = bb.getByte(bb.writerIndex() - 1);
//        bb.writerIndex(bb.writerIndex() - 1);
//
//        WetWipeInfoReportResp msg = new WetWipeInfoReportResp();
//        msg.parse(bb);
//
//        System.out.println("checksum(hex)=" + String.format("%02X", checksum));
//        System.out.println("msgId=" + msg.getHeader().getMsgId());
//        System.out.println("msgBodyProps=" + msg.getHeader().getMsgBodyPropsRaw());
//        System.out.println("imei=" + msg.getHeader().getImei());
//        System.out.println("flowId=" + msg.getHeader().getFlowId());
//
//        System.out.println("messageType=" + msg.getMessageType());
//        System.out.println("messageLength=" + msg.getMessageLength());
//        System.out.println("packageLength=" + msg.getPackageLength());
//        System.out.println("eventId=" + msg.getEventId());
//        System.out.println("sleepMode=" + msg.getSleepMode());
//        System.out.println("outLimitStatus=" + msg.getOutLimitStatus());
//        System.out.println("inLimitStatus=" + msg.getInLimitStatus());
//        System.out.println("distanceMsb=" + msg.getDistanceMsb());
//        System.out.println("distanceLsb=" + msg.getDistanceLsb());
//        System.out.println("battleLevel=" + msg.getBattleLevel());
//        System.out.println("lockStatus=" + msg.getLockStatus());
//        System.out.println("tissueStatus=" + msg.getTissueStatus());
//        System.out.println("rssi=" + msg.getRssi());
//    }
//}
