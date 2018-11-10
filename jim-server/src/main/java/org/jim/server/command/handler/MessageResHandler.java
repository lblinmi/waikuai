package org.jim.server.command.handler;

import org.jim.common.ImPacket;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Command;
import org.jim.common.utils.ChatKit;
import org.jim.server.command.AbCmdHandler;
import org.jim.server.notice.NoticeServiceFactory;
import org.tio.core.ChannelContext;

/**
   *  消息回应处理器
 * @author Administrator
 *
 */
public class MessageResHandler extends AbCmdHandler{

	@Override
	public Command command() {
		return Command.COMMAND_CHAT_RESP;
	}

	@Override
	public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
		if (packet.getBody() == null) {
			throw new Exception("body is null");
		}
		ChatBody chatBody = ChatKit.toChatBody(packet.getBody(), channelContext);
		NoticeServiceFactory.getInstance()
		.beforeSendMsg(channelContext, chatBody);
		return null;
	}

}
