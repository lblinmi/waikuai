package org.jim.server.notice.needapi;

import org.jim.common.Const;
import org.jim.common.ImAio;
import org.jim.common.ImConfig;
import org.jim.common.ImPacket;
import org.jim.common.message.IMesssageHelper;
import org.jim.common.packets.ChatBody;
import org.jim.common.utils.ChatKit;
import org.tio.core.ChannelContext;

public class NeedServiceApiImpl implements NeedServiceApi,Const{
	
	private IMesssageHelper messsageHelper = ImConfig.getMessageHelper();

    @Override
    public void sendMsg(ChannelContext channel, ChatBody chatBody, boolean status){
	    try {
	    	ImPacket ip = null;
	    	if(status) {
	    		System.out.println("聊天用户在线响应包");
	    		ip = ChatKit.sendSuccessRespPacket(channel);
	    	}else {
	    		System.out.println("聊天用户不在线响应包");
	    		ip = ChatKit.offlineRespPacket(channel);
	    	}
	    	ImAio.send(channel, ip);
	    }catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    private void wirteMessage(ChatBody chatBody,boolean isOnline) {
		if (!isOnline) {
			writeMessage(PUSH, USER + ":" + chatBody.getTo() + ":" + chatBody.getFrom(), chatBody);
		} else {
			writeMessage(STORE, USER + ":" + ChatKit.sessionId(chatBody.getFrom(), chatBody.getTo()), chatBody);
		}
	}
	
	private void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
		messsageHelper.writeMessage(timelineTable, timelineId, chatBody);
	}


	@Override
	public void updateMsgStatus(String msgId) {
		messsageHelper.updateMessageStatus(msgId);
	}


	@Override
	public void saveMessage(ChatBody chatBody, boolean isOnline) {
		wirteMessage(chatBody,isOnline);
	}

}
