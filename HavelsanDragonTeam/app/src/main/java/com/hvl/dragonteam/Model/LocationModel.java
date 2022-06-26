package com.hvl.dragonteam.Model;

public class LocationModel {

    private int id;
    private String teamId;
    private String name;
    private double lat;
    private double lon;
    
    public LocationModel() {
    }

    
	public LocationModel(int id, String teamId, String name, double lat, double lon) {
		super();
		this.id = id;
		this.teamId = teamId;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
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


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public double getLat() {
		return lat;
	}


	public void setLat(double lat) {
		this.lat = lat;
	}


	public double getLon() {
		return lon;
	}


	public void setLon(double lon) {
		this.lon = lon;
	}


}
