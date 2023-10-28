package com.ren.tutornearme.model;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class TutorInfo {
    private String uid;
    private String firstName;
    private String lastName;
    private String gender;
    private String phoneNumber;
    private String address;
    private String resume;
    private String validId;
    private String avatar;
    private long createdDate;
    private long updatedDate;

    public TutorInfo() {
    }

    public TutorInfo(String uid, String firstName, String lastName, String gender, String phoneNumber,
                     String address, String resume, String validId, String avatar, long createdDate,
                     long updatedDate) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.resume = resume;
        this.validId = validId;
        this.avatar = avatar;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getValidId() {
        return validId;
    }

    public void setValidId(String validId) {
        this.validId = validId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "TutorInfo{" +
                "uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", address='" + address + '\'' +
                ", resume='" + resume + '\'' +
                ", validId='" + validId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
