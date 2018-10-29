package org.jim.server.command.handler.processor.chat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.Const;
import org.jim.common.ImConfig;
import org.jim.common.ImPacket;
import org.jim.common.message.IMesssageHelper;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.ChatType;
import org.jim.common.utils.ChatKit;
import org.tio.core.ChannelContext;

/**
 * @author WChao
 * @date 2018年4月3日 下午1:13:32
 */
public abstract class AbstractChatProcessor implements ChatProcessorIntf, Const {

	public static final String BASE_CHAT_PROCESSOR = "base_chat_processor";
	private IMesssageHelper messsageHelper = ImConfig.getMessageHelper();

	public abstract void doHandler(ChatBody chatBody, ChannelContext channelContext);

	@Override
	public boolean isProtocol(ChannelContext channelContext) {
		return true;
	}

	@Override
	public String name() {
		return BASE_CHAT_PROCESSOR;
	}

	@Override
	public void handler(ImPacket chatPacket, ChannelContext channelContext) throws Exception {
		ChatBody chatBody = ChatKit.toChatBody(chatPacket.getBody(), channelContext);
		if (ON.equals(ImConfig.isStore)) {// 开启持久化
			if (ChatType.CHAT_TYPE_PUBLIC.getNumber() == chatBody.getChatType()) {// 存储群聊消息;
				pushGroupMessages(PUSH, STORE, chatBody);
			} else {
				String from = chatBody.getFrom();
				String to = chatBody.getTo();
				String sessionId = ChatKit.sessionId(from, to);
				//writeMessage(STORE, USER + ":" + sessionId, chatBody);
				boolean isOnline = ChatKit.isOnline(to);
				if (!isOnline) {
					writeMessage(PUSH, USER + ":" + to + ":" + from, chatBody);
				} else {
					writeMessage(STORE, USER + ":" + sessionId, chatBody);
				}
			}
		}
		doHandler(chatBody, channelContext);
	}

	/**
	 * 推送持久化群组消息
	 * 
	 * @param pushTable
	 * @param storeTable
	 * @param group_id
	 */
	private void pushGroupMessages(String pushTable, String storeTable, ChatBody chatBody) {
		String group_id = chatBody.getGroup_id();
		List<String> users = messsageHelper.getGroupUsers(group_id);
		if(null == users || users.isEmpty())
			return;
		
		// 先将群消息持久化到存储Timeline;
		writeMessage(storeTable, GROUP + ":" + group_id, chatBody);
		// 通过写扩散模式将群消息同步到所有的群成员
		for (String userid : users) {
			boolean isOnline = ChatKit.isOnline(userid);
			if (!isOnline) {
				if (StringUtils.isEmpty(chatBody.getTo()))
					chatBody.setTo(userid);
				writeMessage(pushTable, GROUP + ":" + group_id + ":" + userid, chatBody);
			}
		}
	}

	private void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
		messsageHelper.writeMessage(timelineTable, timelineId, chatBody);
	}
}
