package org.jim.server.notice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jim.common.Const;
import org.jim.common.ImConfig;
import org.jim.common.packets.ChatBody;
import org.jim.server.notice.needapi.NeedServiceApi;
import org.jim.server.notice.needapi.NeedServiceApiImpl;
import org.tio.core.ChannelContext;

/**
 * 
 * @author 徐政涛 客户端通知中心 2018年10月29日
 */
public class NoticeCenterToClient implements Const{

	public NoticeCenterToClient() {
		if (ON.equals(ImConfig.isStore)) {// 开启持久化
			// 初始化需要的接口类
			service = new NeedServiceApiImpl();
			// 初始化守护线程
			NoticeDestroyThread t = new NoticeDestroyThread();
			t.setDaemon(true);
			t.start();
		}
	}

	/**
	 * 超时时间
	 */
	private long timeout = 10 * 1000;

	private NeedServiceApi service;

	private final Logger log = Logger.getLogger(NoticeCenterToClient.class);

	/**
	 * 用户瞬时状态 key : msgId (唯一消息ID) 由userId和消息编号组成
	 */
	private Map<String, UserOnlineStatus> userContext = new ConcurrentHashMap<>();
	/**
	 * 延迟队列
	 */
	private DelayQueue<DelayNotice> queue = new DelayQueue<DelayNotice>();
	
	private Map<String,DelayNotice> noticeMap = new ConcurrentHashMap<String,DelayNotice>();

	/**
	 * 给 msgId 添加一个延迟通知任务
	 * 
	 * @param chatBody
	 */
	public void addNoticeToUser(ChatBody chatBody, ChannelContext context) {
		if (userContext.get(chatBody.getId()) == null) {
			DelayNotice notice = new DelayNotice(System.currentTimeMillis() + timeout, chatBody, context);
			queue.add(notice);
			noticeMap.put(chatBody.getId(), notice);
			// 当前消息接收方的状态为未知
			System.out.println("添加一个延迟通知任务"+chatBody.getId());
			userContext.put(chatBody.getId(), UserOnlineStatus.NONE);
		}
	}

	/**
	 * 根据用户在线状态做不同消息的回复
	 * 
	 * @param msgId
	 */
	public void dealWithUserOnlineStatus(ChatBody chatBody) {
		System.out.println("根据用户在线状态做不同消息的回复"+chatBody.getId());
		DelayNotice dn = noticeMap.remove(chatBody.getId());
		ChannelContext context = null;
		if(dn!=null) {
		    context = dn.getContext();
		}
		UserOnlineStatus online = userContext.remove(chatBody.getId());
		// 超时消息
		if (online == null) {
			online = UserOnlineStatus.OFFLINE;
		} else if (online == UserOnlineStatus.NONE) {// 正常消息
			online = UserOnlineStatus.ONLINE;
		}
		sendMsgByStatus(online, context, chatBody);
	}

	private void sendMsgByStatus(UserOnlineStatus online, ChannelContext context, ChatBody chatBody) {
		switch (online) {
		case ONLINE:
			service.sendMsg(context, chatBody, true);
			break;
		case OFFLINE:
			// update 离线消息为在线消息
			service.updateMsgStatus(chatBody.getId());
			// 已经由延迟队列回复了发送者
			break;
		default:
			break;
		}
	}

	/**
	 * @author 徐政涛 用户顺时状态 2018年10月30日
	 */
	enum UserOnlineStatus {
		ONLINE, OFFLINE, NONE
	}

	/**
	 * 延迟通知任务
	 * 
	 * @author 徐政涛
	 *
	 *         2018年10月29日
	 */
	class DelayNotice implements Delayed {
		/**
		 * 最长等待时间
		 */
		public long waitTime;
		/**
		 * 
		 */
		public ChatBody chatBody;
		/**
		 * 通道
		 */
		public ChannelContext context;

		public DelayNotice(long waitTime, ChatBody chatBody, ChannelContext context) {
			this.waitTime = waitTime;
			this.chatBody = chatBody;
			this.context = context;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(waitTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) {
			long diff = getDelay(TimeUnit.SECONDS) - o.getDelay(TimeUnit.SECONDS);
			return diff > 0 ? 1 : diff == 0 ? 0 : -1;
		}

		public long getWaitTime() {
			return waitTime;
		}

		public void setWaitTime(long waitTime) {
			this.waitTime = waitTime;
		}

		public ChannelContext getContext() {
			return context;
		}

		public void setContext(ChannelContext context) {
			this.context = context;
		}

		public ChatBody getChatBody() {
			return chatBody;
		}

		public void setChatBody(ChatBody chatBody) {
			this.chatBody = chatBody;
		}
	}

	class NoticeDestroyThread extends Thread {

		@Override
		public void run() {
			while (true) {
				try {
					DelayNotice dd = queue.take();
					// 移除超时未回应的消息接收方
					UserOnlineStatus online = userContext.remove(dd.getChatBody().getId());
					if (online == null) {// 已经由消息接收者发送消息了
						service.saveMessage(dd.getChatBody(), true);
					} else {// 由于上一条消息没有准时送达,回复消息发送者
						service.sendMsg(dd.getContext(), dd.getChatBody(), false);
						service.saveMessage(dd.getChatBody(), false);
					}
					log.info("销毁超时未处理的消息" + dd.getChatBody().getId());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
