package com.hvl.dragonteam.Model.Enum;

public enum LineupEnum {
	L1(0), R1(1), L2(2), R2(3),
	L3(4), R3(5), L4(6), R4(7),
	L5(8), R5(9), L6(10), R6(11),
	L7(12), R7(13), L8(14), R8(15);

	int value;

	private LineupEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static LineupEnum toLineupEnum(int value) {
		// retrieve status from status code
		if (value == L1.value) {
			return L1;
		} else if (value == R1.value) {
			return R1;
		} else if (value == L2.value) {
			return L2;
		} else if (value == R2.value) {
			return R2;
		} else if (value == L3.value) {
			return L3;
		} else if (value == R3.value) {
			return R3;
		} else if (value == L4.value) {
			return L4;
		} else if (value == R4.value) {
			return R4;
		} else if (value == L5.value) {
			return L5;
		} else if (value == R5.value) {
			return R5;
		} else if (value == L6.value) {
			return L6;
		} else if (value == R6.value) {
			return R6;
		} else if (value == L7.value) {
			return L7;
		} else if (value == R7.value) {
			return R7;
		} else if (value == L8.value) {
			return L8;
		} else if (value == R8.value) {
			return R8;
		} else {
			return null;
		}
	}
}
