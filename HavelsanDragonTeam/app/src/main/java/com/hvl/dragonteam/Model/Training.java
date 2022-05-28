package com.hvl.dragonteam.Model;

public class Training {

    private int id;
    private String time;
    private int location;
    private String teamId;

    public Training() {
    }

	public Training(int id, String time, int location, String teamId) {
		super();
		this.id = id;
		this.time = time;
		this.location = location;
		this.teamId = teamId;
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
	

}
