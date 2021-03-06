package org.jim.server.command.handler;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.Const;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.tio.core.ChannelContext;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.ChatType;
import org.jim.common.packets.Command;
import org.jim.common.packets.RespBody;
import org.jim.common.packets.User;
import org.jim.common.session.id.impl.UUIDSessionIdGenerator;
import org.jim.common.utils.ChatKit;
import org.jim.server.command.AbCmdHandler;
import org.jim.server.command.handler.processor.chat.MsgQueueRunnable;
import org.jim.server.notice.NoticeServiceFactory;
import org.jim.server.util.limit.MessageLimitUtil;
/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年9月22日 下午2:58:59
 */
public class ChatReqHandler extends AbCmdHandler {
	
	@Override
	public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
		if (packet.getBody() == null) {
			throw new Exception("body is null");
		}
		ChatBody chatBody = ChatKit.toChatBody(packet.getBody(), channelContext);
		if(chatBody == null || chatBody.getChatType() == null){//聊天数据格式不正确
			ImPacket respChatPacket = ChatKit.dataInCorrectRespPacket(channelContext);
			return respChatPacket;
		}
		if(ChatType.forNumber(chatBody.getChatType()) != null){//异步调用业务处理消息接口
			MsgQueueRunnable msgQueueRunnable = (MsgQueueRunnable)channelContext.getAttribute(Const.CHAT_QUEUE);
			msgQueueRunnable.addMsg(packet);
			msgQueueRunnable.getExecutor().execute(msgQueueRunnable);
		}
		ImPacket chatPacket = new ImPacket(Command.COMMAND_CHAT_REQ,new RespBody(Command.COMMAND_CHAT_REQ,chatBody).toByte());
		chatPacket.setSynSeq(packet.getSynSeq());//设置同步序列号;
		if(ChatType.CHAT_TYPE_PRIVATE.getNumber() == chatBody.getChatType()){//私聊
		    if(!MessageLimitUtil.limit(channelContext, chatBody.getFrom())) {
			String toId = chatBody.getTo();
//			if(ChatKit.isOnline(toId)){
//				ImAio.sendToUser(toId, chatPacket);
//				System.out.println("聊天用户在线响应包");
//				return ChatKit.sendSuccessRespPacket(channelContext);//发送成功响应包
//			}else{
//				System.out.println("聊天用户不在线响应包");
//				return ChatKit.offlineRespPacket(channelContext);//用户不在线响应包
//			}
			chatBody.setId(UUIDSessionIdGenerator.instance.sessionId(null));
			chatPacket.setBody(new RespBody(Command.COMMAND_CHAT_REQ,chatBody).toByte());
			ImAio.sendToUser(toId, chatPacket);
			NoticeServiceFactory.getInstance().afterRecieveMsg(channelContext, chatBody);
		    }else {
			System.out.println(chatBody.getFrom()+"已被拉入黑名单");
		    }
		    return ChatKit.sendSuccessRespPacketNew(channelContext,chatBody.getId(),chatBody.getOrderId());
		}else if(ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()){//群聊
			String group_id = chatBody.getGroup_id();
			ImAio.sendToGroup(group_id, chatPacket);
			return ChatKit.sendSuccessRespPacket(channelContext);//发送成功响应包
		}
		return null;
	}
	
	@Override
	public Command command() {
		return Command.COMMAND_CHAT_REQ;
	}
}
