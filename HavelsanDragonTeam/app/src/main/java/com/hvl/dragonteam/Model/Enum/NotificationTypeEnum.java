package com.hvl.dragonteam.Model.Enum;

public enum NotificationTypeEnum {
	MANAGEMENT_NOTIFICATION(0),
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
		if (value == MANAGEMENT_NOTIFICATION.value) {
			return MANAGEMENT_NOTIFICATION;
		} else if (value == LINEUP_NOTIFICATION.value) {
			return LINEUP_NOTIFICATION;
		} else if (value == CHAT_MESSAGE_NOTIFICATION.value) {
			return CHAT_MESSAGE_NOTIFICATION;
		}else {
			return NONE;
		}
	}
}
