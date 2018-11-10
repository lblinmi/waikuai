package org.jim.server.notice;

import org.jim.server.notice.api.NoticeService;
import org.jim.server.notice.api.NoticeServiceImpl;

public class NoticeServiceFactory {

	public static NoticeService NS = null;
	
	public static NoticeService getInstance() {
		if(NS==null) {
			synchronized (NoticeServiceFactory.class) {
				if(NS==null) {
					NS = new NoticeServiceImpl();
				}
			}
		}
		return NS;
	}
}
