package com.hvl.dragonteam.Model;

public class Announcement {

    private int id;
    private String teamId;
    private String context;
    private String time;

    public Announcement() {
    }
    
	public Announcement(int id, String teamId, String context, String time) {
		super();
		this.id = id;
		this.teamId = teamId;
		this.context = context;
		this.time = time;
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
