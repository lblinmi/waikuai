package org.jim.server.demo.notice.api;

import org.tio.core.ChannelContext;

/**
 * 对外提供的接口调用
 * @author 徐政涛
 * 2018年10月30日
 */
public interface NoticeService {

    /**
     * 收到消息后(必须是发送者的消息，接收者回馈消息不可以调用此方法)
     */
    public void afterRecieveMsg(ChannelContext context,String msgId);
    
    /**
     * 准备发送消息前(必须是接收者回馈的消息，发送者消息转发给接收者时不可以调用此方法)
     */
    public void beforeSendMsg(ChannelContext context,String msgId,String msg);
}
