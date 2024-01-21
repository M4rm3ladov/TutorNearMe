package com.ren.tutornearme.model;

public class TutorRequestInfo {
    private String sessionKey;
    private StudentGeo studentLocation;
    private StudentInfo studentInfo;
    private TutorSubject tutorSubject;

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public StudentGeo getStudentLocation() {
        return studentLocation;
    }

    public void setStudentLocation(StudentGeo studentLocation) {
        this.studentLocation = studentLocation;
    }

    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentInfo studentInfo) {
        this.studentInfo = studentInfo;
    }

    public TutorSubject getTutorSubject() {
        return tutorSubject;
    }

    public void setTutorSubject(TutorSubject tutorSubject) {
        this.tutorSubject = tutorSubject;
    }
}
