package com.hvl.dragonteam.Model.Enum;

public enum RemoteDialogTypeEnum {
	UPDATE(0), INFO(1), UNDER_CONSTRUCTION(2),NONE(101);

	int value;

	private RemoteDialogTypeEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static RemoteDialogTypeEnum toTransportationTypeEnum(int value) {
		// retrieve status from status code
		if (value == UPDATE.value) {
			return UPDATE;
		} else if (value == INFO.value) {
			return INFO;
		} else if (value == UNDER_CONSTRUCTION.value) {
			return UNDER_CONSTRUCTION;
		} else {
			return NONE;
		}
	}
}
