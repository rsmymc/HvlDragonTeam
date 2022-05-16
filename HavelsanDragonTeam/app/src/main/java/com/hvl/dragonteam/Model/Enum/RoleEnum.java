package com.hvl.dragonteam.Model.Enum;

public enum RoleEnum {
	DEFAULT(0), ADMIN(1), NONE (101);

	int value;

	private RoleEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static RoleEnum toRoleEnum(int value) {
		// retrieve status from status code
		if (value == DEFAULT.value) {
			return DEFAULT;
		} else if (value == ADMIN.value) {
			return ADMIN;
		} else {
			return NONE;
		}
	}
}
