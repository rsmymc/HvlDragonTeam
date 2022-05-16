package com.hvl.dragonteam.Model.Enum;

import java.util.Locale;

public enum LanguageEnum {
	ENGLISH(1,"en"), TURKISH(2,"tr");

	int value;
	String locale;

	private LanguageEnum(int value, String locale) {
		this.value = value;
		this.locale = locale;
	}

	public int getValue() {
		return this.value;
	}

	public String getLocale() {
		return this.locale;
	}

	public static LanguageEnum toLanguageEnum(int value) {
		// retrieve status from status code
		if (value == ENGLISH.value) {
			return ENGLISH;
		}else if (value == TURKISH.value) {
			return TURKISH;
		}else {
			return ENGLISH;
		}
	}

	public static int getLocaleEnumValue(){

		String language = Locale.getDefault().getLanguage();

		if (language.contains("tr")) {
			return TURKISH.getValue();
		} else {
			return ENGLISH.getValue();
		}
	}
}
