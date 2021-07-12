package com.example.myapp;

import android.widget.Button;
import android.widget.Switch;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.sql.BatchUpdateException;
import java.util.ArrayList;

public class RecepieDetails {
    public String ID;
    public String image;
    public String recepieName;
    public String recepieMakingTime;
    public String recepieServings;
    public String url;
    public ArrayList<String> ingredients;

    public String GetID()
    {
        return ID;
    }
    public void SetID(String value)
    {
        ID = value;
    }
    public String GetImage()
    {
        return image;
    }
    public void SetImage(String value)
    {
        image = value;
    }
    public String GetRecepieName()
    {
        return recepieName;
    }
    public void SetRecepieName(String value)
    {
        recepieName = value;
    }
    public String GetRecepieMakingTime()
    {
        return recepieMakingTime;
    }
    public void SetRecepieMakingTime(String value)
    {
        recepieMakingTime = value;
    }
    public String GetRecepieServings()
    {
        return recepieServings;
    }
    public void SetRecepieServings(String value)
    {
        recepieServings = value;
    }
    public String GetBtnUrl()
    {
        return url;
    }
    public void SetBtnUrl(String value)
    {
        url = value;
    }
    public ArrayList<String> GetIngredients() { return ingredients; }
    public void SetIngredients(ArrayList<String> value) { ingredients = value; }
    public RecepieDetails(String image, String recepieName, String recepieMakingTime, String recepieServings, String recepieUrl, ArrayList<String> ingredients) {
        this.image = image;
        this.recepieName = recepieName;
        this.recepieMakingTime = recepieMakingTime;
        this.recepieServings = recepieServings;
        this.url = recepieUrl;
        this.ingredients = ingredients;
    }
    public RecepieDetails() { }
}
