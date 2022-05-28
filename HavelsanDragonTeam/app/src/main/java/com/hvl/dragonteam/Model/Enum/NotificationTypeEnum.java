package com.hvl.dragonteam.Model.Enum;

public enum NotificationTypeEnum {
	ANNOUNCEMENT_NOTIFICATION(0),
	LINEUP_NOTIFICATION(1),
	CHAT_MESSAGE_NOTIFICATION(2),
	NONE(9);

	int value;

	private NotificationTypeEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static NotificationTypeEnum toMessageTypeEnum(int value) {
		// retrieve status from status code
		if (value == ANNOUNCEMENT_NOTIFICATION.value) {
			return ANNOUNCEMENT_NOTIFICATION;
		} else if (value == LINEUP_NOTIFICATION.value) {
			return LINEUP_NOTIFICATION;
		} else if (value == CHAT_MESSAGE_NOTIFICATION.value) {
			return CHAT_MESSAGE_NOTIFICATION;
		}else {
			return NONE;
		}
	}
}
