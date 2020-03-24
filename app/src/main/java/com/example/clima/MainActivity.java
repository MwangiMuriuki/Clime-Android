package com.example.clima;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.example.clima.HelperClasses.PermissionUtils;
import com.example.clima.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.jacksonandroidnetworking.JacksonParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    String API_KEY = "f5a1e9349a1b225338c2fd1e3fd1f16d";
    
    String provider;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
    String cityName;
    String searchCityName;
    String newString;
    String temp;
    
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        AndroidNetworking.initialize(getApplicationContext());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        /*CHECK IF DEVICE HAS BEEN GRANTED LOCATION PERMISSIONS THEN GO AHEAD AND FETHC THE CURRENT DEVICE LOCATION*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            getFusedLocation();

        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }


        /*USER SEARCHING FOR CITY*/
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCityName = binding.cityEditText.getText().toString();

                if (searchCityName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter a City name", Toast.LENGTH_LONG).show();
                }else {

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.cityEditText.getWindowToken(), 0);

                    binding.cityEditText.setText("");

                    getCustomCityWeatherInfo(searchCityName);
                }

            }
        });

        binding.currentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFusedLocation();
            }
        });
    }

    private void getFusedLocation() {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            System.out.println("Provider " + provider + " has been selected.");
                            onLocationChanged(location);
                        }else {

                            binding.cityName.setText("Location Unavailable");
                        }
                    }
                });
    }

    private void onLocationChanged(Location location) {
        double lat = (double) (location.getLatitude());
        double lng = (double) (location.getLongitude());

        Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> addresses = geo.getFromLocation(lat, lng, 1);
            if (!addresses.isEmpty()){
//                binding.cityName.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                cityName = addresses.get(0).getLocality();

                getLocationAsObject(cityName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getLocationAsObject(final String cityName) {
        AndroidNetworking.get(WEATHER_URL)
                .addQueryParameter("q", cityName)
                .addQueryParameter("appid", API_KEY)
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        try {

                            double temp = (double) response.getJSONObject("main").get("temp");

                            String countryCode = response.getJSONObject("sys").get("country").toString();

                            String weatherConditions = response.getJSONArray("weather").getJSONObject(0).get("description").toString();

                            int weatherID = (int) response.getJSONArray("weather").getJSONObject(0).get("id");

                            double tempCelcius = temp - 273.15;
                            binding.temperature.setText(String.format("%s°C", df2.format(tempCelcius)));
                            binding.weatherConditions.setText(weatherConditions);
                            binding.cityName.setText(cityName + ", "+ countryCode);

                            setWeatherIcon(weatherID);

                            System.out.println(weatherConditions);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {

                        System.out.println(anError);

                    }
                });
    }

    private void getCustomCityWeatherInfo(final String searchCityName) {

        AndroidNetworking.get(WEATHER_URL)
                .addQueryParameter("q", searchCityName)
                .addQueryParameter("appid", API_KEY)
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseCode =  response.get("cod").toString();

                            if (responseCode.equals(String.valueOf(200))){
                                double temp = (double) response.getJSONObject("main").get("temp");
                                String countryCode = response.getJSONObject("sys").get("country").toString();
                                String weatherConditions = response.getJSONArray("weather").getJSONObject(0).get("description").toString();
                                int weatherID = (int) response.getJSONArray("weather").getJSONObject(0).get("id");
                                double tempCelcius = temp - 273.15;
                                binding.temperature.setText(String.format("%s°C", df2.format(tempCelcius)));
                                binding.weatherConditions.setText(weatherConditions);
                                binding.cityName.setText(searchCityName + ", "+ countryCode);
                                setWeatherIcon(weatherID);

                                System.out.println(weatherConditions);

                            } else if (responseCode.equals("404")){
                                binding.temperature.setText("");
                                binding.weatherConditions.setText("");
                                binding.cityName.setText("City Not Found");
                                binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dunno));
                            } else {

                                binding.temperature.setText("");
                                binding.weatherConditions.setText("");
                                binding.cityName.setText("Error Fetching data");
                                binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.dunno));

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });
    }

    private void setWeatherIcon(int weatherID) {

        if (weatherID <= 300) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.tstorm_one));
        } else if (weatherID > 300 && weatherID <= 500) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.light_rain));
        } else if (weatherID > 500 && weatherID <= 600) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.shower_three));
        } else if (weatherID > 600 && weatherID <= 700) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.snow_four));
        } else if (weatherID > 700 && weatherID <= 771) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.fog));
        } else if (weatherID > 771 && weatherID <= 799) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.tstorm_three));
        } else if (weatherID == 800) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sunny));
        } else if (weatherID > 800 && weatherID <= 804) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloudy2));
        } else if (weatherID == 900 && weatherID <= 902) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.overcast));
        } else if (weatherID == 903) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.snow5));
        } else if (weatherID == 904) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sunny));
        } else if (weatherID > 904 && weatherID <= 1000) {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.tstorm_three));
        } else {
            binding.weatherIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.dunno));
        }

    }

}
