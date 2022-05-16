package com.hvl.dragonteam.Model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class NotificationModel {

    private String notificationId;
    private int notificationType;
	private String senderPersonId;
	private String senderPersonName;

	public NotificationModel(String notificationId, int notificationType, String senderPersonId, String senderPersonName) {
		this.notificationId = notificationId;
		this.notificationType = notificationType;
		this.senderPersonId = senderPersonId;
		this.senderPersonName = senderPersonName;
	}

	public String getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}

	public int getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(int notificationType) {
		this.notificationType = notificationType;
	}

	public String getSenderPersonId() {
		return senderPersonId;
	}

	public void setSenderPersonId(String senderPersonId) {
		this.senderPersonId = senderPersonId;
	}

	public String getSenderPersonName() {
		return senderPersonName;
	}

	public void setSenderPersonName(String senderPersonName) {
		this.senderPersonName = senderPersonName;
	}

    @Override
	public boolean equals(Object that) {
		boolean isEqual = EqualsBuilder.reflectionEquals(this, that,  "latitude", "longitude", "dailyTimeDiffInSecs");
		return isEqual;
	}

	@Override
	public int hashCode() {
		int hashCode = HashCodeBuilder.reflectionHashCode(this, "personId", "transportationId", "personRoleType");
		return hashCode;
	}

	@Override
	public String toString() {
		String str = ReflectionToStringBuilder.toString(this);
		return str;
	}
}


