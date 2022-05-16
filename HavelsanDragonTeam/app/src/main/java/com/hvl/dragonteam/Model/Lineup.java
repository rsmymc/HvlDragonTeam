package com.hvl.dragonteam.Model;

import com.google.gson.annotations.SerializedName;

public class Lineup {

    private int trainingId;
	@SerializedName(value = "l1", alternate = {"L1"})
    private String L1;
	@SerializedName(value = "l2", alternate = {"L2"})
    private String L2;
	@SerializedName(value = "l3", alternate = {"L3"})
	private String L3;
	@SerializedName(value = "l4", alternate = {"L4"})
	private String L4;
	@SerializedName(value = "l5", alternate = {"L5"})
	private String L5;
	@SerializedName(value = "l6", alternate = {"L6"})
	private String L6;
	@SerializedName(value = "l7", alternate = {"L7"})
	private String L7;
	@SerializedName(value = "l8", alternate = {"L8"})
	private String L8;
	@SerializedName(value = "r1", alternate = {"R1"})
    private String R1;
	@SerializedName(value = "r2", alternate = {"R2"})
    private String R2;
	@SerializedName(value = "r3", alternate = {"R3"})
	private String R3;
	@SerializedName(value = "r4", alternate = {"R4"})
	private String R4;
	@SerializedName(value = "r5", alternate = {"R5"})
	private String R5;
	@SerializedName(value = "r6", alternate = {"R6"})
	private String R6;
	@SerializedName(value = "r7", alternate = {"R7"})
	private String R7;
	@SerializedName(value = "r8", alternate = {"R8"})
	private String R8;
    private int state;

	public Lineup() {
    }

	public int getTrainingId() {
		return trainingId;
	}

	public void setTrainingId(int trainingId) {
		this.trainingId = trainingId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getL1() {
		return L1;
	}

	public void setL1(String l1) {
		L1 = l1;
	}

	public String getL2() {
		return L2;
	}

	public void setL2(String l2) {
		L2 = l2;
	}

	public String getL3() {
		return L3;
	}

	public void setL3(String l3) {
		L3 = l3;
	}

	public String getL4() {
		return L4;
	}

	public void setL4(String l4) {
		L4 = l4;
	}

	public String getL5() {
		return L5;
	}

	public void setL5(String l5) {
		L5 = l5;
	}

	public String getL6() {
		return L6;
	}

	public void setL6(String l6) {
		L6 = l6;
	}

	public String getL7() {
		return L7;
	}

	public void setL7(String l7) {
		L7 = l7;
	}

	public String getL8() {
		return L8;
	}

	public void setL8(String l8) {
		L8 = l8;
	}

	public String getR1() {
		return R1;
	}

	public void setR1(String r1) {
		R1 = r1;
	}

	public String getR2() {
		return R2;
	}

	public void setR2(String r2) {
		R2 = r2;
	}

	public String getR3() {
		return R3;
	}

	public void setR3(String r3) {
		R3 = r3;
	}

	public String getR4() {
		return R4;
	}

	public void setR4(String r4) {
		R4 = r4;
	}

	public String getR5() {
		return R5;
	}

	public void setR5(String r5) {
		R5 = r5;
	}

	public String getR6() {
		return R6;
	}

	public void setR6(String r6) {
		R6 = r6;
	}

	public String getR7() {
		return R7;
	}

	public void setR7(String r7) {
		R7 = r7;
	}

	public String getR8() {
		return R8;
	}

	public void setR8(String r8) {
		R8 = r8;
	}
}
