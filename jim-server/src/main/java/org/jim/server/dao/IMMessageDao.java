package org.jim.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Group;
import org.jim.common.packets.UserMessageData;
import org.jim.server.util.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMMessageDao {

	private Logger log = LoggerFactory.getLogger(IMMessageDao.class);

	private static IMMessageDao instance = null;

	private IMMessageDao() {
	}

	public static IMMessageDao getInstance() {
		if (null == instance)
			instance = new IMMessageDao();

		return instance;
	}

	public void saveChatBody(String type, ChatBody chatBody) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "";
		long now = System.currentTimeMillis();

		try {
			sql = "insert into im_message (chattype, msgtype, content, from_uid, group_id, to_uid, sendtime, is_arrive, arrive_time) "
					+ "values  (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			conn = ConnectionFactory.getInstance().makeConnection();
			pstmt = conn.prepareStatement(sql);
			int i = 1;
			pstmt.setInt(i++, chatBody.getChatType());
			pstmt.setInt(i++, chatBody.getMsgType());
			pstmt.setString(i++, chatBody.getContent());
			pstmt.setString(i++, chatBody.getFrom());
			pstmt.setString(i++, chatBody.getGroup_id());
			pstmt.setString(i++, (null == chatBody.getTo() ? "" : chatBody.getTo()));
			pstmt.setTimestamp(i++, new Timestamp(now));

			if ("push".equals(type)) {
				// push消息处理 群或离线消息
				pstmt.setInt(i++, 0);
				pstmt.setTimestamp(i++, null);
			} else if ("store".equals(type)) {
				// 点对点
				pstmt.setInt(i++, 1);
				pstmt.setTimestamp(i++, new Timestamp(now));
			}

			pstmt.execute();
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public List<ChatBody> loadUnArriveMsg(Connection conn, String userid, List<Integer> unArriveIds) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<ChatBody> results = null;

		try {
			String sql = "select * from im_message where chattype = 2 and to_uid = ? and is_arrive = 0";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (null == results)
					results = new ArrayList<ChatBody>();

				ChatBody chatBody = new ChatBody();
				chatBody.setFrom(rs.getString("from_uid"));
				chatBody.setTo(rs.getString("to_uid"));
				chatBody.setMsgType(rs.getInt("msgtype"));
				chatBody.setChatType(rs.getInt("chattype"));
				chatBody.setContent(rs.getString("content"));
				chatBody.setGroup_id(rs.getString("group_id"));
				chatBody.setCreateTime(rs.getTimestamp("sendtime").getTime());
				results.add(chatBody);
				unArriveIds.add(rs.getInt("id"));
			}

			return results;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
		}
		return null;
	}

	public List<ChatBody> loadUnArriveMsgByFrom(Connection conn, String userid, String from_userid,
			List<Integer> unArriveIds) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<ChatBody> results = null;

		try {
			String sql = "select * from im_message where chattype = 2 and to_uid = ? and from_uid = ? and is_arrive = 0";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			pstmt.setString(2, from_userid);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (null == results)
					results = new ArrayList<ChatBody>();

				ChatBody chatBody = new ChatBody();
				chatBody.setFrom(rs.getString("from_uid"));
				chatBody.setTo(rs.getString("to_uid"));
				chatBody.setMsgType(rs.getInt("msgtype"));
				chatBody.setChatType(rs.getInt("chattype"));
				chatBody.setContent(rs.getString("content"));
				chatBody.setGroup_id(rs.getString("group_id"));
				chatBody.setCreateTime(rs.getTimestamp("sendtime").getTime());
				results.add(chatBody);
				unArriveIds.add(rs.getInt("id"));
			}

			return results;
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
		}
		return null;
	}

	public UserMessageData loadUnArriveGroupMsg(Connection conn, String userid, String group_id,
			List<Integer> unArriveIds) {
		UserMessageData groupMessageData = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			if (conn == null)
				conn = ConnectionFactory.getInstance().makeConnection();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		try {
			String sql = "select * from im_message where chattype = 1 and to_uid = ? and group_id = ? and is_arrive = 0";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			pstmt.setString(2, group_id);
			rs = pstmt.executeQuery();

			List<ChatBody> messages = null;

			while (rs.next()) {
				if (null == groupMessageData)
					groupMessageData = new UserMessageData(userid);

				if (null == messages)
					messages = new ArrayList<ChatBody>();

				if (null == unArriveIds)
					unArriveIds = new ArrayList<Integer>();

				ChatBody chatBody = new ChatBody();
				chatBody.setFrom(rs.getString("from_uid"));
				chatBody.setTo(rs.getString("to_uid"));
				chatBody.setMsgType(rs.getInt("msgtype"));
				chatBody.setChatType(rs.getInt("chattype"));
				chatBody.setContent(rs.getString("content"));
				chatBody.setGroup_id(rs.getString("group_id"));
				chatBody.setCreateTime(rs.getTimestamp("sendtime").getTime());
				messages.add(chatBody);

				unArriveIds.add(rs.getInt("id"));
			}

			if (messages != null && !messages.isEmpty()) {
				putGroupMessage(groupMessageData, messages);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
		}

		return groupMessageData;
	}

	public UserMessageData getFriendsOfflineMessage(String userid, String from_userid) {
		UserMessageData messageData = null;
		Connection conn = null;
		try {
			conn = ConnectionFactory.getInstance().makeConnection();

			List<Integer> unArriveIds = new ArrayList<Integer>();

			List<ChatBody> messages = loadUnArriveMsgByFrom(conn, userid, from_userid, unArriveIds);

			if (messages != null && !messages.isEmpty()) {
				messageData = new UserMessageData(userid);

				putFriendsMessage(messageData, messages);

				updateArriveStatus(conn, unArriveIds);
			}

		} catch (Exception e) {
			log.error("getFriendsOfflineMessage error: " + e.toString());
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
				e2.printStackTrace();
			}

		}

		return messageData;
	}

	public UserMessageData getFriendsOfflineMessage(String userid) {
		Connection conn = null;

		List<Integer> unArriveMsgIds = new ArrayList<Integer>();

		try {
			// 加载私聊信息
			conn = ConnectionFactory.getInstance().makeConnection();

			UserMessageData messageData = new UserMessageData(userid);
			List<ChatBody> results = loadUnArriveMsg(conn, userid, unArriveMsgIds);

			putFriendsMessage(messageData, results);

			// 加载用户所属群
			List<Group> groups = UserDao.getInstance().loadGroups(conn, userid);

			if (groups != null && !groups.isEmpty()) {
				for (Group group : groups) {
					UserMessageData groupMessageData = loadUnArriveGroupMsg(conn, userid, group.getId(),
							unArriveMsgIds);
					if (groupMessageData != null) {
						putGroupMessage(messageData, groupMessageData.getGroups().get(group.getId()));
					}
				}
			}

			// 修改离线消息发送状态
			updateArriveStatus(conn, unArriveMsgIds);

			return messageData;
		} catch (Exception e) {
			log.error("getGroupUsers error: " + e.toString());
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}

		return null;
	}

	public void updateArriveStatus(Connection conn, List<Integer> unArriveMsgIds) {
		long now = System.currentTimeMillis();

		if (null == unArriveMsgIds || unArriveMsgIds.isEmpty())
			return;

		PreparedStatement pstmt = null;

		try {
			int i = 0;
			String sql = "update im_message set is_arrive = ?, arrive_time = ? where id = ?";
			pstmt = conn.prepareStatement(sql);

			for (Integer id : unArriveMsgIds) {
				i++;
				pstmt.setInt(1, 1);
				pstmt.setTimestamp(2, new Timestamp(now));
				pstmt.setInt(3, id);
				pstmt.addBatch();
				if (i % 100 == 0) {
					pstmt.executeBatch();
				}
			}

			pstmt.executeBatch();

		} catch (Exception e) {
			log.error("updateArriveStatus error: " + e.toString());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e2) {
				log.error(e2.toString());
			}
		}

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

	public UserMessageData getGroupOfflineMessage(String userid, String group_id) {
		Connection conn = null;
		List<Integer> ids = null;
		try {
			conn = ConnectionFactory.getInstance().makeConnection();

			UserMessageData messageData = loadUnArriveGroupMsg(conn, userid, group_id, ids);

			updateArriveStatus(conn, ids);

			return messageData;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e2) {
				log.error(e2.getMessage());
				e2.printStackTrace();
			}
		}
		return null;
	}

}
