package com.ren.tutornearme.model;

import com.google.firebase.database.PropertyName;

import java.util.HashMap;
import java.util.Map;

public class TutorSubject {
    private String id;
    private TutorInfo tutorInfo;
    private SubjectInfo subjectInfo;
    private String status;
    private String credential;
    private int sessionHours;
    private boolean isAvailable;
    private long updatedDate;
    private long createdDate;

    public TutorSubject() {
    }

    public TutorSubject(String id, TutorInfo tutorInfo, SubjectInfo subjectInfo, String status,
                        String credential, int sessionHours, boolean isAvailable, long updatedDate, long createdDate) {
        this.id = id;
        this.tutorInfo = tutorInfo;
        this.subjectInfo = subjectInfo;
        this.status = status;
        this.credential = credential;
        this.sessionHours = sessionHours;
        this.isAvailable = isAvailable;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TutorInfo getTutorInfo() {
        return tutorInfo;
    }

    public void setTutorInfo(TutorInfo tutorInfo) {
        this.tutorInfo = tutorInfo;
    }

    public SubjectInfo getSubjectInfo() {
        return subjectInfo;
    }

    public void setSubjectInfo(SubjectInfo subjectInfo) {
        this.subjectInfo = subjectInfo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public int getSessionHours() {
        return sessionHours;
    }

    public void setSessionHours(int sessionHours) {
        this.sessionHours = sessionHours;
    }

    @PropertyName("isAvailable")
    public boolean isAvailable() {
        return isAvailable;
    }

    @PropertyName("isAvailable")
    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public TutorSubject copyWith(String id, TutorInfo tutorInfo, SubjectInfo subjectInfo,
                                 String status, String credential, Integer sessionHours, Boolean isAvailable, Long updatedDate, Long createdDate) {
        return new TutorSubject(
                (id != null) ? id : this.id,
                (tutorInfo != null) ? tutorInfo : this.tutorInfo,
                (subjectInfo != null) ? subjectInfo : this.subjectInfo,
                (status != null) ? status : this.status,
                (credential != null) ? credential : this.credential,
                (sessionHours != null) ? sessionHours : this.sessionHours,
                (isAvailable != null) ? isAvailable : this.isAvailable,
                (updatedDate != null) ? updatedDate : this.updatedDate,
                (createdDate !=  null) ? createdDate : this.createdDate);
    }

    public TutorSubject clone() {
        return copyWith(id, tutorInfo, subjectInfo, status,  credential, sessionHours, isAvailable, updatedDate, createdDate);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("updatedDate", updatedDate);

        return result;
    }

    @Override
    public String toString() {
        return "TutorSubject{" +
                "id='" + id + '\'' +
                ", tutorInfo=" + tutorInfo +
                ", subjectInfo=" + subjectInfo +
                ", status='" + status + '\'' +
                ", credential='" + credential + '\'' +
                ", sessionHours =" + sessionHours +
                ", isAvailable =" + isAvailable +
                ", updatedDate=" + updatedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
