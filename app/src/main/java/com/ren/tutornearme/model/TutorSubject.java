package com.ren.tutornearme.model;

import java.util.HashMap;
import java.util.Map;

public class TutorSubject {
    private String id;
    private TutorInfo tutorInfo;
    private SubjectInfo subjectInfo;
    private String status;
    private String credential;
    private long updatedDate;
    private long createdDate;

    public TutorSubject() {
    }

    public TutorSubject(String id, TutorInfo tutorInfo, SubjectInfo subjectInfo, String status,
                        String credential, long updatedDate, long createdDate) {
        this.id = id;
        this.tutorInfo = tutorInfo;
        this.subjectInfo = subjectInfo;
        this.status = status;
        this.credential = credential;
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
                                 String status, String credential, Long updatedDate, Long createdDate) {
        return new TutorSubject(
                (id != null) ? id : this.id,
                (tutorInfo != null) ? tutorInfo : this.tutorInfo,
                (subjectInfo != null) ? subjectInfo : this.subjectInfo,
                (status != null) ? status : this.status,
                (credential != null) ? credential : this.credential,
                (updatedDate != null) ? updatedDate : this.updatedDate,
                (createdDate !=  null) ? createdDate : this.createdDate);
    }

    public TutorSubject clone() {
        return copyWith(id, tutorInfo, subjectInfo, status, credential, updatedDate, createdDate);
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
                ", updatedDate=" + updatedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
