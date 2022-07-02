package com.hvl.dragonteam.Model;

import android.location.Location;

public class TrainingLocationView {

    private int id;
    private String time;
    private LocationModel location;
    private String teamId;

    public TrainingLocationView() {
    }

	public TrainingLocationView(int id, String time, LocationModel location, String teamId) {
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

	public LocationModel getLocation() {
		return location;
	}

	public void setLocation(LocationModel location) {
		this.location = location;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}
	

}
