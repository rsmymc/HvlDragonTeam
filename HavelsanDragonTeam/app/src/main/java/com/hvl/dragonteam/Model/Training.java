package com.hvl.dragonteam.Model;

public class Training {

    private int id;
    private String time;
    private int location;

    public Training() {
    }

    public Training(int id, String time, int location) {
		super();
		this.id = id;
		this.time = time;
		this.location = location;
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
	

}
