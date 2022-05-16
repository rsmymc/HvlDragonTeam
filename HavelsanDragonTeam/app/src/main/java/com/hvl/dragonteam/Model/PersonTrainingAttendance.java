package com.hvl.dragonteam.Model;

public class PersonTrainingAttendance {

	//person Table
    private String personId;
    private String name;
    private String phone;
    private int height;
    private int weight;
    private int side;
    private int role;
    
    //from Training
    private int trainingId;
    private String time;
    private int location;
    
    //from Attendance
    private boolean attend;

    public PersonTrainingAttendance() {
    }
    
    public PersonTrainingAttendance(String personId, String name, String phone, int height, int weight, int side,
			int role, int trainingId, String time, int location, boolean isAttend) {
		super();
		this.personId = personId;
		this.name = name;
		this.phone = phone;
		this.height = height;
		this.weight = weight;
		this.side = side;
		this.role = role;
		this.trainingId = trainingId;
		this.time = time;
		this.location = location;
		this.attend = isAttend;
	}

	public PersonTrainingAttendance(String personId, int trainingId, String time, int location, boolean isAttend) {
		super();
		this.personId = personId;
		this.trainingId = trainingId;
		this.time = time;
		this.location = location;
		this.attend = isAttend;
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

	public boolean isAttend() {
		return attend;
	}

	public void setAttend(boolean isAttend) {
		this.attend = isAttend;
	}

	@Override
	public String toString(){
		return  this.getName();
	}
}
