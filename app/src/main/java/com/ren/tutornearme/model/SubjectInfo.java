package com.ren.tutornearme.model;

import java.util.HashMap;
import java.util.Map;

public class SubjectInfo {
    private String id;
    private String name;
    private String description;
    private long updatedDate;
    private long createdDate;

    public SubjectInfo() {}

    public SubjectInfo(String id, String name, String description, long updatedDate, long createdDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.updatedDate = updatedDate;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public SubjectInfo copyWith(String id, String name, String description, Long updatedDate, Long createdDate) {
        return new SubjectInfo(
                (id != null) ? id : this.id,
                (name != null) ? name : this.name,
                (description != null) ? description : this.description,
                (updatedDate != null) ? updatedDate : this.updatedDate,
                (createdDate !=  null) ? createdDate : this.createdDate);
    }

    public SubjectInfo clone() {
        return copyWith(id, name, description, updatedDate, createdDate);
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("updatedDate", updatedDate);

        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
