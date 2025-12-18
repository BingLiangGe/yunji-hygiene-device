package com.yunji.hygiene.constant;

/**
 * HandleConstant：JT808/透传消息处理器（Handler）在 Spring 容器中的 BeanName 常量
 *
 * 典型用途：
 * - 根据消息类型（msgId / messageType）动态路由到对应 Handler
 * - 避免业务代码里硬编码字符串（"AuthMsgHandler" 之类），统一集中管理
 *
 * 约定：
 * - 常量值必须与 Spring 容器中对应 Handler 的 BeanName 保持一致
 *   例如：@Component("AuthMsgHandler") 或 @Service("AuthMsgHandler")
 */
public class HandleConstant {

    /** 终端鉴权消息处理器 BeanName */
    public static final String AUTH_SERVICE = "AuthMsgHandler";

    /** 终端注册消息处理器 BeanName */
    public static final String REGISTER_SERVICE = "RegisterMsgHandler";

    /** 心跳消息处理器 BeanName */
    public static final String HEART_SERVICE = "HeartBeatMsgHandler";

    /** 终端通用应答消息处理器 BeanName */
    public static final String TERMINAL_RESP_SERVICE = "TerminalRespHandler";

    /** 位置信息消息处理器 BeanName */
    public static final String LOCATION_SERVICE = "LocationMsgHandler";

    /** 透传/上报消息处理器 BeanName */
    public static final String REPORT_SERVICE = "ReportMsgHandler";

    /** 终端注销/登出消息处理器 BeanName */
    public static final String LOG_OUT_SERVICE = "LogOutMsgHandler";

    /** 平台下发/指令处理器 BeanName（处理 Issue/下行指令回执等） */
    public static final String ISSUE_SERVICE = "IssueMsgHandler";
}
