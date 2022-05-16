package com.hvl.dragonteam.Redis;

import android.text.format.DateFormat;

import com.google.gson.Gson;
import com.hvl.dragonteam.Model.Enum.MessageTypeEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChatHistory {
	private String channel;
	private ConcurrentSkipListSet<ChatMessage> orderedChatMessageList;
	private Gson gson = new Gson();
	
	public ChatHistory(String channel) {
		this.orderedChatMessageList = new ConcurrentSkipListSet<>();
		this.channel = channel;
	}

	public String getChannel() {
		return channel;
	}

	public boolean add(List<String> chats) {
		int lastIndex = chats.size() - 1;
		boolean isChanged = false;
		for (int i = lastIndex; i > -1; i--) {
			ChatMessage cm = gson.fromJson(chats.get(i), ChatMessage.class);
			if(this.orderedChatMessageList.add(cm)) {
				isChanged = true;
			}else {
				break;
			}
		}
		return isChanged;
	}
	
	public ConcurrentSkipListSet<ChatMessage> getOrderedChatMessageList() {
		return orderedChatMessageList;
	}

	public static String getChats(Collection<ChatMessage> chatList){
		StringBuilder builder = new StringBuilder();
		for (ChatMessage chatMessage : chatList) {
			builder.append(chatMessage);
			builder.append("\n");
		}
		return builder.toString();
	}

	public ArrayList<ChatMessage> getChatsArrayList(){
		int index = 0;
		ArrayList<ChatMessage> chatList = new ArrayList<>(orderedChatMessageList);
		ArrayList<ChatMessage> chatList2 = new ArrayList<>();
		for (ChatMessage chatMessage : chatList) {
			if(index == 0){
				ChatMessage cm = ChatMessageFactory.getInstance().getChatMessage(
						chatMessage.getContent(),
						chatMessage.getSenderId(),
						chatMessage.getSenderName(),
						MessageTypeEnum.DATE);
				cm.setTime(chatMessage.getTime());
				chatList2.add(0,cm);
			} else {
				boolean isSameDate = checkDateDifference(chatList.get(index).getTime(), chatList.get(index-1).getTime());

				if(!isSameDate){
					ChatMessage cm = ChatMessageFactory.getInstance().getChatMessage(
							chatMessage.getContent(),
							chatMessage.getSenderId(),
							chatMessage.getSenderName(),
							MessageTypeEnum.DATE);
					cm.setTime(chatMessage.getTime());
					chatList2.add(cm);
				}
			}

			chatList2.add(chatMessage);

			index++;
		}
		return chatList2;
	}

	private boolean checkDateDifference(long time1, long time2) {

		String dateFormatted1 = DateFormat.format("dd.MM.yyyy",time1).toString();
		String dateFormatted2 =  DateFormat.format("dd.MM.yyyy",time2).toString();

		return  dateFormatted1.equals(dateFormatted2);
	}
}
