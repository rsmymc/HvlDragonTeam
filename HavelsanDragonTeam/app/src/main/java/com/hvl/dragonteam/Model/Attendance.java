package com.hvl.dragonteam.Model;

public class Attendance {

    private String personId;
    private int trainingId;

    public Attendance() {
    }

    public Attendance(String personId, int trainingId) {
		super();
		this.personId = personId;
		this.trainingId = trainingId;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public int getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}

}
