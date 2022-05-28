package com.hvl.dragonteam.Model;

public class PersonTeamView {

    private String personId;
    private String personName;
    private String phone;
    private int height;
    private int weight;
    private int side;
    private String profilePictureUrl;
    
    private String teamId;
    private String teamName;
	private String creatorPersonId;
    
    private int role;

    public PersonTeamView()  {}    
    
	public PersonTeamView(String personId, String personName, String phone, int height, int weight, int side,
			String profilePictureUrl, String teamId, String teamName, String creatorPersonId, int role) {
		super();
		this.personId = personId;
		this.personName = personName;
		this.phone = phone;
		this.height = height;
		this.weight = weight;
		this.side = side;
		this.profilePictureUrl = profilePictureUrl;
		this.teamId = teamId;
		this.teamName = teamName;
		this.creatorPersonId = creatorPersonId;
		this.role = role;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
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
	
	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getCreatorPersonId() {
		return creatorPersonId;
	}

	public void setCreatorPersonId(String creatorPersonId) {
		this.creatorPersonId = creatorPersonId;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}
    
    
	
}
