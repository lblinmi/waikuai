package org.jim.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;
import org.jim.server.util.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {

	private Logger log = LoggerFactory.getLogger(UserDao.class);

	static final int USER_TYPE_DEFAULT = 0;
	static final int USER_TYPE_MANAGE = 1;

	private static UserDao instance = null;

	private UserDao() {
	}

	public static UserDao getInstance() {
		if (null == instance) {
			instance = new UserDao();
		}
		return instance;
	}

	public User login(String username, String password, String token) throws Exception {
		if (StringUtils.isEmpty(username) && StringUtils.isEmpty(password) && StringUtils.isEmpty(token))
			throw new IllegalArgumentException();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = "";
		User user = null;
		int utype = 0;
		String cid = "", creator = "";

		if (!StringUtils.isEmpty(token)) {
			sql = "select a.uid, b.name, b.type, b.cid, b.creator from user_token a left join user_info b on a.uid = b.uid where a.token = ?";
			conn = ConnectionFactory.getInstance().makeConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, token);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				user = new User();
				user.setId(rs.getString("uid"));
				user.setNick(rs.getString("name"));
				utype = rs.getInt("type");
				cid = rs.getString("cid");
				creator = rs.getString("creator");
			}

			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (pstmt != null)
					pstmt.close();
			} catch (Exception e) {
			}

			if (user != null) {
				// 加载好友
				List<Group> friends = loadFriends(conn, creator, cid, utype);
				user.setFriends(friends);

				// 加载群组
				List<Group> groups = loadGroups(conn, user.getId());
				user.setGroups(groups);
			}

			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}

		}

		return user;
	}

	public List<Group> loadGroups(Connection conn, String userid) {
		List<Group> groups = new ArrayList<Group>();

		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			if (null == conn)
				conn = ConnectionFactory.getInstance().makeConnection();

			sql = "select a.group_id, b.group_name, b.avatar from im_group_user a left join im_group b on a.group_id = b.group_id where a.uid = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				Group group = new Group(rs.getString("group_id"), rs.getString("group_name"));
				groups.add(group);
			}

			return groups;
		} catch (Exception e) {
			// TODO: handle exception
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

	public List<Group> loadFriends(Connection conn, String creator, String cid, int userType) {
		List<Group> friends = new ArrayList<Group>();
		Group myFriend = new Group("1", "我的好友");
		List<User> myFriendGroupUsers = null;

		String sql = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			if (null == conn)
				conn = ConnectionFactory.getInstance().makeConnection();

			if (USER_TYPE_DEFAULT == userType) {
				sql = "select uid, name, type from user_info where uid = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, creator);

			} else if (USER_TYPE_MANAGE == userType) {
				sql = "select uid, name, type from user_info where cid = ?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, cid);
			}

			if (pstmt != null) {
				rs = pstmt.executeQuery();
				while (rs.next()) {
					if (null == myFriendGroupUsers)
						myFriendGroupUsers = new ArrayList<User>();

					User tmp = new User();
					tmp.setId(rs.getString("uid"));
					tmp.setNick(rs.getString("name"));

					myFriendGroupUsers.add(tmp);
				}
			}

			myFriend.setUsers(myFriendGroupUsers);
			friends.add(myFriend);

			return friends;
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

	public List<String> getGroupUsers(String group_id) {
		List<String> users = null;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String sql = "select uid from im_group_user where group_id = ?";
			conn = ConnectionFactory.getInstance().makeConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, group_id);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (null == users) {
					users = new ArrayList<String>();
				}
				users.add(rs.getString("uid"));
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
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

		return users;
	}
}
