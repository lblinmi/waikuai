package org.jim.server.command.handler;

import org.apache.log4j.Logger;
import org.jim.common.ImPacket;
import org.jim.common.packets.Command;
import org.jim.server.command.AbCmdHandler;
import org.jim.server.command.handler.processor.ProcessorIntf;
import org.jim.server.command.handler.processor.handshake.HandshakeProcessorIntf;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;

public class HandshakeReqHandler extends AbCmdHandler {

	private static final Logger log = Logger.getLogger(HandshakeReqHandler.class);

	@Override
	public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
		ProcessorIntf proCmdHandler = this.getProcessor(channelContext);
		if (proCmdHandler == null) {
			log.error("没有对应的握手协议处理器HandshakeProCmd...");
			Aio.remove(channelContext, "没有对应的握手协议处理器HandshakeProCmd...");
			return null;
		}
		HandshakeProcessorIntf handShakeProCmdHandler = (HandshakeProcessorIntf) proCmdHandler;
		ImPacket handShakePacket = handShakeProCmdHandler.handshake(packet, channelContext);
		if (handShakePacket == null) {
			log.error("业务层不同意握手...");
			Aio.remove(channelContext, "业务层不同意握手");
		}
		return handShakePacket;
	}

	@Override
	public Command command() {
		return Command.COMMAND_HANDSHAKE_REQ;
	}
}
