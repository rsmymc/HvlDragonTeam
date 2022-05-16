package com.hvl.dragonteam.Model.Enum;

public enum SideEnum {
	LEFT(0), RIGHT(1),  BOTH(2), NONE (101);

	int value;

	private SideEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static SideEnum toSideEnum(int value) {
		// retrieve status from status code
		if (value == RIGHT.value) {
			return RIGHT;
		} else if (value == LEFT.value) {
			return LEFT;
		} else if (value == BOTH.value) {
			return BOTH;
		}else {
			return NONE;
		}
	}
}
