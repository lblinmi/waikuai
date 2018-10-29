package org.jim.server.helper.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.listener.ImBindListener;
import org.jim.common.message.IMesssageHelper;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.UserMessageData;
import org.jim.server.dao.IMMessageDao;
import org.jim.server.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mysql获取持久化+同步消息助手;
 * 
 * @author WChao
 * @date 2018年4月10日 下午4:06:26
 */
public class MysqlMessageHelper implements IMesssageHelper {

	private Logger log = LoggerFactory.getLogger(MysqlMessageHelper.class);

	@Override
	public ImBindListener getBindListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addGroupUser(String userid, String group_id) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getGroupUsers(String group_id) {
		List<String> uids = null;

		if (StringUtils.isBlank(group_id))
			return null;

		uids = UserDao.getInstance().getGroupUsers(group_id);

		return uids;
	}

	@Override
	public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
		IMMessageDao.getInstance().saveChatBody(timelineTable, chatBody);
	}

	@Override
	public void removeGroupUser(String userid, String group_id) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserMessageData getFriendsOfflineMessage(String userid, String from_userid) {
		System.out.println("来取与指定用户的离线消息了 => userid: " + userid + ", from_userid: " + from_userid);
		return IMMessageDao.getInstance().getFriendsOfflineMessage(userid, from_userid);
	}

	@Override
	public UserMessageData getFriendsOfflineMessage(String userid) {
		System.out.println("来取与所有用户的离线消息了 => userid: " + userid);

		return IMMessageDao.getInstance().getFriendsOfflineMessage(userid);
	}

	/**
	 * 放入用户群组消息;
	 * 
	 * @param userMessage
	 * @param messages
	 */
	public UserMessageData putGroupMessage(UserMessageData userMessage, List<ChatBody> messages) {
		if (userMessage == null || messages == null)
			return null;
		for (ChatBody chatBody : messages) {
			String group = chatBody.getGroup_id();
			if (StringUtils.isEmpty(group))
				continue;
			List<ChatBody> groupMessages = userMessage.getGroups().get(group);
			if (groupMessages == null) {
				groupMessages = new ArrayList<ChatBody>();
				userMessage.getGroups().put(group, groupMessages);
			}
			groupMessages.add(chatBody);
		}
		return userMessage;
	}

	
	/**
	 * 放入用户好友消息;
	 * 
	 * @param userMessage
	 * @param messages
	 */
	public UserMessageData putFriendsMessage(UserMessageData userMessage, List<ChatBody> messages) {
		if (userMessage == null || messages == null)
			return null;
		for (ChatBody chatBody : messages) {
			String fromUserId = chatBody.getFrom();
			if (StringUtils.isEmpty(fromUserId))
				continue;
			List<ChatBody> friendMessages = userMessage.getFriends().get(fromUserId);
			if (friendMessages == null) {
				friendMessages = new ArrayList<ChatBody>();
				userMessage.getFriends().put(fromUserId, friendMessages);
			}
			friendMessages.add(chatBody);
		}
		return userMessage;
	}

	@Override
	public UserMessageData getGroupOfflineMessage(String userid, String groupid) {
		System.out.println("getGroupOfflineMessage 来取与指定群组的离线消息了 => userid = " + userid + ", groupid = " + groupid);
		
		return IMMessageDao.getInstance().getGroupOfflineMessage(userid, groupid);
	}

	@Override
	public UserMessageData getFriendHistoryMessage(String userid, String from_userid, Double beginTime, Double endTime,
			Integer offset, Integer count) {
		System.out.println("getFriendHistoryMessage 来获取与指定群组历史消息了 => userid: " + userid + ", from_userid = "
				+ from_userid + ", beginTime = " + beginTime + ", endTime = " + endTime + ", offset = " + offset
				+ ", count = " + count);
		return null;
	}

	@Override
	public UserMessageData getGroupHistoryMessage(String userid, String groupid, Double beginTime, Double endTime,
			Integer offset, Integer count) {
		System.out.println("getGroupHistoryMessage 来获取与指定群组历史消息 => userid = " + userid + ", groupid = " + groupid
				+ ", beginTime = " + beginTime + ", endTime = " + endTime + ", offset = " + offset + ", count = "
				+ count);
		return null;
	}

}
