package com.example.clima;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CityInformation implements Serializable {
    @SerializedName("main")
    private String main;
    @SerializedName("name")
    private String name;
    @SerializedName("weather")
    List<weather> weather = new ArrayList<>();

    public CityInformation(String main, String name, List<com.example.clima.weather> weather) {
        this.main = main;
        this.name = name;
        this.weather = weather;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<com.example.clima.weather> getWeather() {
        return weather;
    }

    public void setWeather(List<com.example.clima.weather> weather) {
        this.weather = weather;
    }

}
