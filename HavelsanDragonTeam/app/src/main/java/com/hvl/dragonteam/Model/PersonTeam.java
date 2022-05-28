package com.hvl.dragonteam.Model;

public class PersonTeam {

    private String personId;
    private String teamId;
	private int role;
    
    public PersonTeam() {
    }

	public PersonTeam(String personId, String teamId, int role) {
		super();
		this.personId = personId;
		this.teamId = teamId;
		this.role = role;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}
    
    
	
}
