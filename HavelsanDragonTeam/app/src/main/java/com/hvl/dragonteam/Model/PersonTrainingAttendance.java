package com.hvl.dragonteam.Model;

import com.google.gson.annotations.SerializedName;

public class PersonTrainingAttendance {

	//from personTeam
    private String personId;
    private String name;
    private String phone;
    private int height;
    private int weight;
    private int side;
    private int role;
    private String profilePictureUrl;
    
    //from Training
    private int trainingId;
    private String time;
    private int location;
    private String teamId;
    
    //from Attendance
	@SerializedName(value = "attend", alternate = {"isAttend"})
    private boolean isAttend;
    
    public PersonTrainingAttendance() {}
    
    public PersonTrainingAttendance(String personId, String name, String phone, int height, int weight, int side,
			int role, String profilePictureUrl, int trainingId, String time, int location, String teamId,
			boolean isAttend) {
		super();
		this.personId = personId;
		this.name = name;
		this.phone = phone;
		this.height = height;
		this.weight = weight;
		this.side = side;
		this.role = role;
		this.profilePictureUrl = profilePictureUrl;
		this.trainingId = trainingId;
		this.time = time;
		this.location = location;
		this.teamId = teamId;
		this.isAttend = isAttend;
	}

    public PersonTrainingAttendance(String personId, String name, String phone, int height, int weight, int side, String profilePictureUrl,
			int role, int trainingId, boolean isAttend) {
		super();
		this.personId = personId;
		this.name = name;
		this.phone = phone;
		this.height = height;
		this.weight = weight;
		this.side = side;
		this.profilePictureUrl = profilePictureUrl;
		this.role = role;
		this.trainingId = trainingId;
		this.isAttend = isAttend;
	}

	public PersonTrainingAttendance(String personId, int trainingId, String time, int location, boolean isAttend) {
		super();
		this.personId = personId;
		this.trainingId = trainingId;
		this.time = time;
		this.location = location;
		this.isAttend = isAttend;
	}

	public String getPersonId() {
        return personId;
    }

    public void setPersonId(String id) {
        this.personId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
    
    public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}
	
	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public int getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}
	
	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public boolean isAttend() {
		return isAttend;
	}

	public void setAttend(boolean isAttend) {
		this.isAttend = isAttend;
	}
	
}
