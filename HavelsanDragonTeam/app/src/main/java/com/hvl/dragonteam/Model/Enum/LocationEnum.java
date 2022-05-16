package com.hvl.dragonteam.Model.Enum;

public enum LocationEnum {
	MALTEPE(0, 40.938813, 29.107086), TUZLA(1,40.824621, 29.286416), NONE (101,0,0);

	int value;
	double lat;
	double lon;

	private LocationEnum(int value, double lat, double lon) {
		this.value = value;
		this.lat = lat;
		this.lon = lon;
	}

	public int getValue() {
		return this.value;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public static LocationEnum toLocationEnum(int value) {
		// retrieve status from status code
		if (value == TUZLA.value) {
			return TUZLA;
		} else if (value == MALTEPE.value) {
			return MALTEPE;
		} else {
			return NONE;
		}
	}
}
