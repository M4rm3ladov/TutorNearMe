package com.ren.tutornearme.model;

import java.util.HashMap;
import java.util.Map;

public class TutorSubject {
    private String tutorId;
    private String subjectId;
    private String name;
    private String description;
    private String status;
    private String credential;
    private long updatedDate;
    private long createdDate;

    public TutorSubject() {
    }

    public TutorSubject(String tutorId, String subjectId, String name, String description, String status,
                        String credential, long updatedDate, long createdDate) {
        this.tutorId = tutorId;
        this.subjectId = subjectId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.credential = credential;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
    }

    public TutorSubject(String status, String credential, long updatedDate, long createdDate) {
        this.status = status;
        this.credential = credential;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public TutorSubject copyWith(String tutorId, String subjectId, String name, String description,
                                 String status, String credential, Long updatedDate, Long createdDate) {
        return new TutorSubject(
                (tutorId != null) ? tutorId : this.tutorId,
                (subjectId != null) ? subjectId : this.subjectId,
                (name != null) ? name : this.name,
                (description != null) ? description : this.description,
                (status != null) ? status : this.status,
                (credential != null) ? credential : this.credential,
                (updatedDate != null) ? updatedDate : this.updatedDate,
                (createdDate !=  null) ? createdDate : this.createdDate);
    }

    public TutorSubject copyWith(String status, String credential, Long updatedDate, Long createdDate) {
        return new TutorSubject(
                (status != null) ? status : this.status,
                (credential != null) ? credential : this.credential,
                (updatedDate != null) ? updatedDate : this.updatedDate,
                (createdDate !=  null) ? createdDate : this.createdDate);
    }

    public TutorSubject clone() {
        return copyWith(tutorId, subjectId, name, description, status, credential, updatedDate, createdDate);
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
                ", tutorId='" + tutorId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", credential='" + credential + '\'' +
                ", updatedDate=" + updatedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
