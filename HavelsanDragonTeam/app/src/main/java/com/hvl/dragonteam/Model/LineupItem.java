package com.hvl.dragonteam.Model;

public class LineupItem {

    private int id;
    private PersonTrainingAttendance personTrainingAttendance;

    public LineupItem() {
    }

    public LineupItem(int id, PersonTrainingAttendance personTrainingAttendance) {
        this.id = id;
        this.personTrainingAttendance = personTrainingAttendance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PersonTrainingAttendance getPersonTrainingAttendance() {
        return personTrainingAttendance;
    }

    public void setPersonTrainingAttendance(PersonTrainingAttendance personTrainingAttendance) {
        this.personTrainingAttendance = personTrainingAttendance;
    }
}
