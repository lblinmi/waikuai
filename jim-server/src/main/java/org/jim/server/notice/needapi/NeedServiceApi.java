package org.jim.server.notice.needapi;

import org.jim.common.packets.ChatBody;
import org.tio.core.ChannelContext;

/**
 * 需要被实现的接口
 * @author 徐政涛
 *
 * 2018年10月30日
 */
public interface NeedServiceApi {
    
    /**
     * 发送消息接口
     * @param channel
     * @param msg
     * @param status 是否正常消息（可根据此值组装成不同的消息结构）
     */
    public void sendMsg(ChannelContext channel,ChatBody chatBody, boolean status);
    
    /**
     * 更新消息状态
     */
    public void updateMsgStatus(String msgId) ;
    
    public void saveMessage(ChatBody chatBody,boolean isOnline);
}
