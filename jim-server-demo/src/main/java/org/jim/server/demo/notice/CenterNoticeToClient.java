package org.jim.server.demo.notice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.tio.core.ChannelContext;

/**
 * 
 * @author 徐政涛 客户端通知中心 2018年10月29日
 */
public class CenterNoticeToClient {
    private static Lock lock = new ReentrantLock();
    /**
     * 超时时间
     */
    private static long timeout = 10 * 1000;

    private static final Logger log = Logger.getLogger(CenterNoticeToClient.class);

    /**
     * 用户瞬时状态 key : msgId (唯一消息ID) 由userId和消息编号组成
     */
    private static Map<String, UserOnlineStatus> userContext = new ConcurrentHashMap<>();
    /**
     * 延迟队列
     */
    private static DelayQueue<DelayNotice> queue = new DelayQueue<DelayNotice>();

    /**
     * 给 msgId 添加一个延迟通知任务
     * @param msgId
     */
    public static void addNoticeToUser(String msgId,ChannelContext context) {
	DelayNotice notice = new DelayNotice(System.currentTimeMillis() + timeout,msgId,context);
	queue.add(notice);
	//当前消息接收方的状态为未知
	userContext.put(msgId, UserOnlineStatus.NONE);
    }

    static enum UserOnlineStatus {
	ONLINE, OFFLINE, NONE
    }

    /**
     * 延迟通知任务
     * 
     * @author 徐政涛
     *
     *         2018年10月29日
     */
    static class DelayNotice implements Delayed {
	/**
	 * 最长等待时间
	 */
	public long waitTime;
	/**
	 * msgId
	 */
	public String msgId;
	/**
	 * 通道
	 */
	public ChannelContext context;

	public DelayNotice(long waitTime, String msgId, ChannelContext context) {
	    this.waitTime = waitTime;
	    this.msgId = msgId;
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
    }

    class AutoNoticeThread extends Thread {

	@Override
	public void run() {
	    while (true) {
		try {
		    log.info("等待获取延迟通知对象");
		    DelayNotice dd = queue.take();
		    lock.lock();
		    
		    log.info("获取延迟通知对象引用：" + dd.toString());

		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    lock.unlock();
		}
	    }
	}
    }
}
