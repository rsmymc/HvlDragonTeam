package com.hvl.dragonteam.Redis;
import com.hvl.dragonteam.Model.Enum.MessageTypeEnum;

import java.util.Date;
import java.util.UUID;

public class ChatMessageFactory{

	private static ChatMessageFactory instance;
	private long timeDiff;
	
	private ChatMessageFactory() {
	}
	
	synchronized public static ChatMessageFactory getInstance() {
		if(instance == null) {
			instance = new ChatMessageFactory();
			instance.init();
		}
		return instance;
	}
	
	private void init() {
//		long now = new Date().getTime();
//		long realNow = RemoteTime.getRemoteNTPTime();
//		System.out.println("computer date:" + new Date(now));
//		System.out.println("remote date:" + new Date(now));
		this.timeDiff =  0;
	}
	
	public ChatMessage getChatMessage(Object content, String senderId, String senderName, MessageTypeEnum messageType) {
		long realTime = new Date().getTime() + timeDiff;
		String id = getRandomUniqueId();
	    ChatMessage cm = new ChatMessage(id, realTime, senderId, senderName, content, messageType);
		return cm;
	}
	
	public static String getRandomUniqueId() {
		UUID randomUniqId = UUID.randomUUID();
		//f46709a2-f27c-11e8-a2f7-a088699d485b like this one //it is always 36 characters

		return randomUniqId.toString();
	}

	public long getTimeDiff() {
		return timeDiff;
	}
}
