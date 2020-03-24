package com.example.clima;


import java.util.ArrayList;
import java.util.List;

public class CityInfo {
    private String main;
    private String name;
    private List<weather> weather = new ArrayList<>();

    public CityInfo() {
    }

    public CityInfo(String main, String name, List<com.example.clima.weather> weather) {
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
