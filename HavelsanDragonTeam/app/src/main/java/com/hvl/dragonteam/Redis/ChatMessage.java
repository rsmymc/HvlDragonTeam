package com.hvl.dragonteam.Redis;

import com.hvl.dragonteam.Model.Enum.MessageTypeEnum;

import java.util.Objects;

public class ChatMessage implements Comparable<ChatMessage> {

	private String id;
	private long time;
	private String senderId;
	private String senderName;
	private Object content;
	private MessageTypeEnum messageType;

	public ChatMessage(String id, long time, String senderId, String senderName, Object content, MessageTypeEnum messageType) {
		this.time = time;
		this.senderId = senderId;
		this.senderName = senderName;
		this.content = content;
		this.id = id;
		this.messageType = messageType;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.senderName);
		builder.append(':');
		builder.append(content.toString());
		
		return builder.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof ChatMessage)) {
			return false;
		}
		ChatMessage other = (ChatMessage)obj;
		return this.id.equals(other.id);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.time);
	}
	
	@Override
	public int compareTo(ChatMessage o) {

		if(o == null) {
			throw new RuntimeException("Can not Compare Chat for null object:" + o);
		}else if(!(o instanceof ChatMessage)) {
			throw new RuntimeException("Can not Compare Chat for:" + o.toString());
		}
			
		int result = new Long(this.time).compareTo(o.time);
		if(result == 0) {
			return this.id.compareTo(o.id);
		}
		
		return result;
	}
	
	public String getId() {
		return id;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public String getContent() {
		return content.toString();
	}

	public MessageTypeEnum getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageTypeEnum messageType) {
		this.messageType = messageType;
	}
}
