package com.hvl.dragonteam.Model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class PersonNotification {

	private String personId;
	
	private int role;

	private String token;

	private boolean loggedIn;
	
	private Integer languageType;

	private boolean notification1;
	
	private boolean notification2;
	
	private boolean notification3;
	
	private boolean notification4;
	
	private boolean notification5;
	
	private boolean notification6;
	
	private boolean notification7;
	
	private boolean notification8;
	
	private boolean notification9;
	
	private boolean notification10;

	public PersonNotification() {}

	public PersonNotification(String token, boolean loggedIn, Integer languageType, boolean notification1, boolean notification2, boolean notification3, boolean notification4
			, boolean notification5, boolean notification6, boolean notification7, boolean notification8, boolean notification9, boolean notification10) {
		this(null, token, loggedIn, languageType, notification1, notification2, notification3, notification4, notification5,
				notification6, notification7, notification8, notification9, notification10);
	}

	public PersonNotification(String personId, String token, boolean loggedIn, Integer languageType, boolean notification1, boolean notification2, boolean notification3, boolean notification4
			, boolean notification5, boolean notification6, boolean notification7, boolean notification8, boolean notification9, boolean notification10) {
		this.personId = personId;
		this.token = token;
		this.loggedIn = loggedIn;
		this.languageType = languageType;
		this.notification1 = notification1;
		this.notification2 = notification2;
		this.notification3 = notification3;
		this.notification4 = notification4;
		this.notification5 = notification5;
		this.notification6 = notification6;
		this.notification7 = notification7;
		this.notification8 = notification8;
		this.notification9 = notification9;
		this.notification10 = notification10;
	}

	public PersonNotification(String personId, String token, boolean isLoggedIn, int languageType) {
		this.personId = personId;
		this.token = token;
		this.loggedIn = isLoggedIn;
		this.languageType = languageType;
		this.notification1 = true;
	}


	public PersonNotification(String personId, String token) {
		this.personId = personId;
		this.token = token;
	}
	
	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}
	
	

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	public Integer getLanguageType() {
		return this.languageType;
	}

	public void setLanguageType(Integer languageType) {
		this.languageType = languageType;
	}
	
	public boolean getNotification1() {
		return notification1;
	}

	public void setNotification1(boolean notification1) {
		this.notification1 = notification1;
	}

	public boolean getNotification2() {
		return notification2;
	}

	public void setNotification2(boolean notification2) {
		this.notification2 = notification2;
	}

	public boolean getNotification3() {
		return notification3;
	}

	public void setNotification3(boolean notification3) {
		this.notification3 = notification3;
	}

	public boolean getNotification4() {
		return notification4;
	}

	public void setNotification4(boolean notification4) {
		this.notification4 = notification4;
	}

	public boolean getNotification5() {
		return notification5;
	}

	public void setNotification5(boolean notification5) {
		this.notification5 = notification5;
	}

	public boolean getNotification6() {
		return notification6;
	}

	public void setNotification6(boolean notification6) {
		this.notification6 = notification6;
	}

	public boolean getNotification7() {
		return notification7;
	}

	public void setNotification7(boolean notification7) {
		this.notification7 = notification7;
	}

	public boolean getNotification8() {
		return notification8;
	}

	public void setNotification8(boolean notification8) {
		this.notification8 = notification8;
	}

	public boolean getNotification9() {
		return notification9;
	}

	public void setNotification9(boolean notification9) {
		this.notification9 = notification9;
	}

	public boolean getNotification10() {
		return notification10;
	}

	public void setNotification10(boolean notification10) {
		this.notification10 = notification10;
	}
	
	@Override
	public boolean equals(Object that) {
		boolean isEqual = EqualsBuilder.reflectionEquals(this, that);
		return isEqual;
	}

	@Override
	public int hashCode() {
		int hashCode = HashCodeBuilder.reflectionHashCode(this, "personId", "token", "languageType", "notification1", "notification2", "notification3", "notification4", "notification5"
				, "notification6", "notification7", "notification8", "notification9", "notification10");
		return hashCode;
	}


	@Override
	public String toString() {
		String str = ReflectionToStringBuilder.toString(this);
		return str;
	}

}
