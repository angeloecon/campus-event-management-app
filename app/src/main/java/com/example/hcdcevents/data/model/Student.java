package com.example.hcdcevents.data.model;

public class Student {
    String email, name, academicDivision;
    boolean isAdmin;
    public Student() {

    }

    public Student(String email, String name, String academicDivision, boolean isAdmin) {
        this.email = email;
        this.name = name;
        this.academicDivision = academicDivision;
        this.isAdmin = isAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicDivision() {
        return academicDivision;
    }

    public void setAcademicDivision(String program) {
        this.academicDivision = program;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

}

