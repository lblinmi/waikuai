package org.jim.server.demo.notice.api;

import org.jim.server.demo.notice.CenterNoticeToClient;
import org.tio.core.ChannelContext;

public class NoticeServiceImpl implements NoticeService {
    
    private CenterNoticeToClient noticeClient;
    
    public NoticeServiceImpl() {
	noticeClient = new CenterNoticeToClient();
    }

    @Override
    public void afterRecieveMsg(ChannelContext context, String msgId) {
	noticeClient.addNoticeToUser(msgId, context);
    }

    @Override
    public void beforeSendMsg(ChannelContext context, String msgId, String msg) {
	noticeClient.dealWithUserOnlineStatus(context, msgId, msg);
    }

}
