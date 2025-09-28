package com.edtech.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String firstName;
    private String lastName;

    public JwtResponse(String accessToken, String email, String firstName, String lastName) {
        this.token = accessToken;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}