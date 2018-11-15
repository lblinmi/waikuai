package org.jim.server.util.limit;

import org.tio.core.ChannelContext;
import org.tio.utils.cache.caffeine.CaffeineCache;

/**
 * 消息频次限制
 * @author 徐政涛
 *
 * 2018年11月15日
 */
public class MessageLimitUtil {

    private final static String LIMIT = "limitQ";
    
    private static CaffeineCache cache = CaffeineCache.register("BLACK_IP", 3600*24l, 0l, null);

    public static boolean limit(ChannelContext context,String userId) {
	
	if(cache.get(userId)==null) {
        	LimitQueue<Long> q = (LimitQueue) context.getAttribute(LIMIT);
        	if(q==null) {
        	    q = new LimitQueue<Long>(100);
        	    context.setAttribute(LIMIT,q);
        	}
        	Long now = System.currentTimeMillis();
        	Long last = q.offer(now);
        	if(last!=null) {
        	    if(now - last <= 10000) {
        		//拉入黑名单24小时
        		cache.put(userId, "disable");
        		return true;
        	    }
        	}
        	return false;
	}else {
	    return true;
	}
    }
    
}
