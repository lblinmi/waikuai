package org.jim.server.notice.api;

import org.jim.common.packets.ChatBody;
import org.tio.core.ChannelContext;

/**
 * 对外提供的接口调用
 * @author 徐政涛
 * 2018年10月30日
 */
public interface NoticeService {

    /**
     * 此方法在消息发送者消息接收后触发
     */
    public void afterRecieveMsg(ChannelContext context, ChatBody chatBody);
    
    /**
     *  此方法替代消息接收方的回馈消息
     */
    public void beforeSendMsg(ChatBody chatBody);
}
