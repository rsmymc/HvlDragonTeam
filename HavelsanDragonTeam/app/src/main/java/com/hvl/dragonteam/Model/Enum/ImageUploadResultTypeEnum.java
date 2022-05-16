package com.hvl.dragonteam.Model.Enum;

public enum ImageUploadResultTypeEnum {
	OK("ok"), ERROR("error"), NONE("none");

	String value;

	private ImageUploadResultTypeEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static ImageUploadResultTypeEnum toImageUploadResultTypeEnum(String value) {
		// retrieve status from status code
		if (value == OK.value) {
			return OK;
		} else if (value == ERROR.value) {
			return ERROR;
		} else {
			return NONE;
		}
	}
}
