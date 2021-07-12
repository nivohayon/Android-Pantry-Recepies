package com.example.myapp;

public class Ingredient {
    public String Name;
    public String PicUrl;
    public String GetName()
    {
        return Name;
    }
    public void SetName(String Name)
    {
        this.Name = Name;
    }
    public String GetPicUrl()
    {
        return PicUrl;
    }
    public void SetPicUrl(String PicUrl)
    {
        this.PicUrl = PicUrl;
    }
    public Ingredient(String Name, String PicUrl) {
        this.Name = Name;
        this.PicUrl = PicUrl;
    }
    public Ingredient() { }
}
