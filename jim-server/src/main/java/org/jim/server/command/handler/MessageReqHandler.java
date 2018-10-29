package org.jim.server.command.handler;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.Const;
import org.jim.common.ImConfig;
import org.jim.common.ImPacket;
import org.jim.common.ImStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.jim.common.message.IMesssageHelper;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Command;
import org.jim.common.packets.RespBody;
import org.jim.common.packets.UserMessageData;
import org.jim.common.packets.MessageReqBody;
import org.jim.common.utils.ImKit;
import org.jim.common.utils.JsonKit;
import org.jim.server.command.AbCmdHandler;

/**
 * 获取聊天消息命令处理器
 * @author WChao
 * @date 2018年4月10日 下午2:40:07
 */
public class MessageReqHandler extends AbCmdHandler {
	private Logger LOG = LoggerFactory.getLogger(getClass());
	
	@Override
	public Command command() {
		
		return Command.COMMAND_GET_MESSAGE_REQ;
	}

	@Override
	public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
		LOG.info("ImPacket - handler......");
		RespBody resPacket = null;
		MessageReqBody messageReqBody = null;
		try{
			messageReqBody = JsonKit.toBean(packet.getBody(),MessageReqBody.class);
		}catch (Exception e) {//用户消息格式不正确
			return getMessageFailedPacket(channelContext);
		}
		UserMessageData messageData = null;
		IMesssageHelper messageHelper = ImConfig.getMessageHelper();
		String groupId = messageReqBody.getGroupId();//群组ID;
		String userId = messageReqBody.getUserId();//当前用户ID;
		String fromUserId = messageReqBody.getFromUserId();//消息来源用户ID;
		Double beginTime = messageReqBody.getBeginTime();//消息区间开始时间;
		Double endTime = messageReqBody.getEndTime();//消息区间结束时间;
		Integer offset = messageReqBody.getOffset();//分页偏移量;
		Integer count = messageReqBody.getCount();//分页数量;
		int type = messageReqBody.getType();//消息类型;
		if(StringUtils.isEmpty(userId) || (0 != type && 1 != type) || !Const.ON.equals(ImConfig.isStore)){//如果用户ID为空或者type格式不正确，获取消息失败;
			return getMessageFailedPacket(channelContext);
		}
		if(type == 0){
			resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP,ImStatus.C10016);
		}else{
			resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP,ImStatus.C10018);
		}
		if(!StringUtils.isEmpty(groupId)){//群组ID不为空获取用户该群组消息;
			if(0 == type){//离线消息;
				messageData = messageHelper.getGroupOfflineMessage(userId,groupId);
			}else if(1 == type){//历史消息;
				messageData = messageHelper.getGroupHistoryMessage(userId, groupId,beginTime,endTime,offset,count);
			}
		}else if(StringUtils.isEmpty(fromUserId)){
			if(0 == type){//获取用户所有离线消息(好友+群组);
				System.out.println("开始获取所有用户以及群组离线消息");
				messageData = messageHelper.getFriendsOfflineMessage(userId);
				Map<String, List<ChatBody>> map = messageData.getFriends();
				Map<String, List<ChatBody>> map1 = messageData.getGroups();
				System.out.println("好友离线消息大小==》"+map.size()+"群组离线消息大小===>"+map1.size());
				Set<Entry<String, List<ChatBody>>> entrySet = map.entrySet();
				for (Entry<String, List<ChatBody>> entry : entrySet) {
					System.out.println("离线消息=====>"+entry.getKey());
					List<ChatBody> value = entry.getValue();
					for(int i=0;i<value.size();i++) {
						System.out.println("离线消息内容====>"+value.get(i).getContent()+"离线消息接收者===>"+value.get(i).getTo());
					}
				}
			}else{
				return getMessageFailedPacket(channelContext);
			}
		}else{
			if(0 == type){//获取与指定用户离线消息;
				messageData = messageHelper.getFriendsOfflineMessage(userId, fromUserId);
			}else if(1 == type){//获取与指定用户历史消息;
				messageData = messageHelper.getFriendHistoryMessage(userId, fromUserId,beginTime,endTime,offset,count);
			}
		}
		LOG.info("响应消息=====>"+messageData.getUserid());
		resPacket.setData(messageData);
		return ImKit.ConvertRespPacket(resPacket, channelContext);
	}
	/**
	 * 获取用户消息失败响应包;
	 * @param channelContext
	 * @return
	 */
	public ImPacket getMessageFailedPacket(ChannelContext channelContext){
		RespBody resPacket = new RespBody(Command.COMMAND_GET_MESSAGE_RESP,ImStatus.C10015);
		return ImKit.ConvertRespPacket(resPacket, channelContext);
	}
}
