package com.hvl.dragonteam.Model.Enum;

public enum SaveEnum {
	DRAFT(0), PUBLISH(1), NONE (101);

	int value;

	private SaveEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static SaveEnum toSaveEnum(int value) {
		// retrieve status from status code
		if (value == DRAFT.value) {
			return DRAFT;
		} else if (value == PUBLISH.value) {
			return PUBLISH;
		} else {
			return NONE;
		}
	}
}
