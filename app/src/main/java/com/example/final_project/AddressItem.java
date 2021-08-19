package com.example.final_project;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddressItem {

    private String street;
    private String city;
    private String state;

    public AddressItem(String street, String city, String state){
        this.street = street;
        this.city = city;
        this.state = state;

    }

    public AddressItem() {
        this("","","");
    }

    public String getAddress(){
        return street + ", " + city + "," + state;
    }

    public String getStreet(){
        return street;
    }

    public String getCity(){
        return city;
    }

    public String getState(){
        return state;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static String getGSONStringFromList (List<AddressItem> list)
    {
        Gson gson = new Gson ();
        return gson.toJson (list);
    }

    public static ArrayList<AddressItem> getListFromGSONString (String strList)
    {
        Gson gson = new Gson ();
        Type addressItemType = new TypeToken<ArrayList<AddressItem>>()
        {
        }.getType ();
        return gson.fromJson (strList,addressItemType );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressItem that = (AddressItem) o;
        return street.equals(that.street) &&
                city.equals(that.city) &&
                state.equals(that.state);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(street, city, state);
    }

}
