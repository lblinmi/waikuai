/**
 * 
 */
package org.jim.server.demo.listener;

import org.jim.common.ImPacket;
import org.jim.common.ImSessionContext;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;
import org.jim.common.utils.JsonKit;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.listener.ImServerAioListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
/**
 * @author WChao
 *
 */
public class ImDemoAioListener extends ImServerAioListener{
	
	private Logger log = LoggerFactory.getLogger(ImDemoAioListener.class);
	
	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) {
		ImPacket imPacket = (ImPacket)packet;
		if(imPacket.getCommand() == Command.COMMAND_LOGIN_RESP || imPacket.getCommand() == Command.COMMAND_HANDSHAKE_RESP){//首次登陆;
			ImSessionContext imSessionContext = (ImSessionContext)channelContext.getAttribute();
			User user = imSessionContext.getClient().getUser();
			log.info(user.getNick());
			if(user.getGroups() != null){
				for(Group group : user.getGroups()){//绑定群组并发送加入群组通知
					ImPacket groupPacket = new ImPacket(Command.COMMAND_JOIN_GROUP_REQ,JsonKit.toJsonBytes(group));
					try {
						new JoinGroupReqHandler().handler(groupPacket, channelContext);
					} catch (Exception e) {
						log.error(e.toString(),e);
					}
				}
			}
		}
	}
	/**
	 * 建链后触发本方法，注：建链不一定成功，需要关注参数isConnected
	 * @param channelContext
	 * @param isConnected 是否连接成功,true:表示连接成功，false:表示连接失败
	 * @param isReconnect 是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
	 * @throws Exception
	 * @author: WChao
	 */
	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) {
	}
}
