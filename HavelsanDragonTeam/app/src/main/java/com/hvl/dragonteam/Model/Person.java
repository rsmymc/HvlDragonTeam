package com.hvl.dragonteam.Model;

public class Person {

    private String id;
    private String name;
    private String phone;
    private int height;
    private int weight;
    private int side;
    private String profilePictureUrl;
    
    public Person() {}
    
    public Person(String id, String name, String phone, int height, int weight, int side, String profilePictureUrl) {
		super();
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.height = height;
		this.weight = weight;
		this.side = side;
		this.profilePictureUrl = profilePictureUrl;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
	
}
