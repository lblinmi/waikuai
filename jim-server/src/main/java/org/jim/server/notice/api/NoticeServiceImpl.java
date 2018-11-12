package org.jim.server.notice.api;

import org.jim.common.packets.ChatBody;
import org.jim.server.notice.NoticeCenterToClient;
import org.tio.core.ChannelContext;

public class NoticeServiceImpl implements NoticeService {
    
    private NoticeCenterToClient noticeClient;
    
    public NoticeServiceImpl() {
	noticeClient = new NoticeCenterToClient();
    }

    @Override
    public void afterRecieveMsg(ChannelContext context, ChatBody chatBody) {
	noticeClient.addNoticeToUser(chatBody, context);
    }

    @Override
    public void beforeSendMsg(ChatBody chatBody) {
	noticeClient.dealWithUserOnlineStatus(chatBody);
    }

}
