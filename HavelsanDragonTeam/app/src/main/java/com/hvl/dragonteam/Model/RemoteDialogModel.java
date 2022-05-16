package com.hvl.dragonteam.Model;

public class RemoteDialogModel {

	private int id;
	private int type;
	private String tr;
	private String en;
	private boolean isForceUpdate;
	private String version;

	public RemoteDialogModel() {}

	public RemoteDialogModel(int id, int type, String tr, String en, boolean isForceUpdate, String version) {
		this.id = id;
		this.type = type;
		this.tr = tr;
		this.en = en;
		this.isForceUpdate = isForceUpdate;
		this.version = version;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTr() {
		return tr;
	}

	public void setTr(String tr) {
		this.tr = tr;
	}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public boolean isForceUpdate() {
		return isForceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		isForceUpdate = forceUpdate;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
