package com.example.myapp;

public class User {
    public String FirstName;
    public String GetFirstName()
    {
        return FirstName;
    }
    public void SetFirstName(String value)
    {
        FirstName = value;
    }
    public User(String firstName) {
        FirstName = firstName;
    }
    public User() { }
}
