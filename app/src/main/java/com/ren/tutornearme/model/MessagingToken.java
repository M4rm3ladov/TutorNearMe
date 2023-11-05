package com.ren.tutornearme.model;

public class MessagingToken {
    private String token;

    public MessagingToken() {
    }

    public MessagingToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "MessagingToken{" +
                "token='" + token + '\'' +
                '}';
    }
}
