package com.hvl.dragonteam.Model.Enum;

public enum MessageTypeEnum {
	TEXT(0), IMAGE(1), DATE(2), GPS(3), END_OF_GPS(4), NONE(101);

	int value;

	private MessageTypeEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static MessageTypeEnum toMessageTypeEnum(int value) {
		// retrieve status from status code
		if (value == TEXT.value) {
			return TEXT;
		} else if (value == IMAGE.value) {
			return IMAGE;
		} else if (value == DATE.value) {
			return DATE;
		} else if (value == GPS.value) {
			return GPS;
		} else if (value == END_OF_GPS.value) {
			return END_OF_GPS;
		} else {
			return NONE;
		}
	}
}
