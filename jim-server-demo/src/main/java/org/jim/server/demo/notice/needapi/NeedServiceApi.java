package org.jim.server.demo.notice.needapi;

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
    public void sendMsg(ChannelContext channel,String msg, boolean status);
}
