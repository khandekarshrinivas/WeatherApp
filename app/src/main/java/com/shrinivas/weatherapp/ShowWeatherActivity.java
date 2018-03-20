package com.shrinivas.weatherapp;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ShowWeatherActivity extends AppCompatActivity {

    ImageView weatherIcon;
    TextView cityNameTxtView, temperatureTxtView, weatherTxtView, descriptionTxtView, humidityCloudCoverageTxtView, minMaxTempTxtView;
    Runnable runnable;
    PlaceAutocompleteFragment autocompleteFragment;
    private static final String TAG = "MainActivity";
    GeoDataClient mGeoDataClient;
    RelativeLayout relativeLayout;
    LinearLayout linearLayout1, linearLayout2;
    BitmapDrawable bitmapDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);

        weatherIcon = findViewById(R.id.imgClimate);
        cityNameTxtView = findViewById(R.id.cityName);
        temperatureTxtView = findViewById(R.id.temp);
        weatherTxtView = findViewById(R.id.weather);
        minMaxTempTxtView = findViewById(R.id.minMaxTemp);
        descriptionTxtView = findViewById(R.id.desc);
        humidityCloudCoverageTxtView = findViewById(R.id.humidityCloudCoverage);
        linearLayout1 = findViewById(R.id.linearLayout1);
        linearLayout2 = findViewById(R.id.linearLayout2);
        relativeLayout = findViewById(R.id.relativeLayout1);

        //Place autocomplete fragment which returns place predictions to the user
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Enter City Name");

        //filter for autocomplete fragment to filter the results and get only cities
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        //On Place selected, It executes the metthods to get weather details and get photo for the selected city
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                getWeatherDetailsFromCityName(place.getName().toString());
                getPhotoFromCityName(place.getId());
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * Method to get the photos for the placeId
     * @param placeId {@link String}
     */
    private void getPhotos(String placeId) {
        mGeoDataClient = Places.getGeoDataClient(this, null);
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.
                PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);
                // Get the attribution text.
                CharSequence attribution = photoMetadata.getAttributions();
                // Get a full-size bitmap for the photo.
                Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                        PlacePhotoResponse photo = task.getResult();
                        Bitmap bitmap = photo.getBitmap();
                        bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                        relativeLayout.setBackground(bitmapDrawable);
                    }
                });
            }
        });
    }

    /**
     * Method which runs the thread to execute OpenWeather api
     * @param cityName {@link String}
     */
    public void getWeatherDetailsFromCityName(final String cityName) {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeatherResponse(cityName);

            }
        };
        Thread thread = new Thread(null, runnable, "background");
        thread.start();
    }

    /**
     * Method which runs the thread to fetch the images for the place
     * @param placeId {@link String}
     */
    public void getPhotoFromCityName(final String placeId) {
        runnable = new Runnable() {
            @Override
            public void run() {
                getPhotos(placeId);

            }
        };
        Thread thread = new Thread(null, runnable, "background");
        thread.start();
    }

    /**
     * Method to execute OpenWeather api to get the weather details of the city, which is passed as a parameter
     * @param cityName {@link String}
     */
    public void getWeatherResponse(final String cityName) {

        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put(WeatherConstants.KEY_CITY_NAME, cityName);
        queryParameters.put(WeatherConstants.APPID_Key, WeatherConstants.APPID_Value);
        queryParameters.put(WeatherConstants.UNITS_Key, WeatherConstants.UNITS_Value);
        String url = buildUrl(queryParameters);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Toast.makeText(getApplicationContext(), WeatherConstants.SUCCESS_MSG, Toast.LENGTH_SHORT).show();

                try {
                    linearLayout1.setBackgroundColor(getColor(R.color.colorWhite));
                    linearLayout2.setBackgroundColor(getColor(R.color.colorWhite));

                    JSONObject weatherResponseOnject = (JSONObject) response.get("main");
                    JSONObject res = (JSONObject) response.getJSONArray("weather").get(0);
                    String degree = (char) 0x00B0 + "C";
                    String displayCityName = response.get("name").toString();
                    String temp = weatherResponseOnject.get("temp").toString() + degree;
                    String min = "Min. " + weatherResponseOnject.get("temp_min").toString() + degree;
                    String max = "Max. " + weatherResponseOnject.get("temp_max").toString() + degree;

                    String cloudCoverage = "Clouds\t: " + response.getJSONObject("clouds").get("all").toString() + "%";
                    String humidity = "Humidity\t: " + weatherResponseOnject.get("humidity").toString() + "%";

                    cityNameTxtView.setText(displayCityName);
                    temperatureTxtView.setText(temp);

                    minMaxTempTxtView.setText(String.format("%s \t\t %s", min, max));
                    weatherTxtView.setText(res.get("main").toString());
                    descriptionTxtView.setText(StringUtils.capitalize(res.get("description").toString()));
                    humidityCloudCoverageTxtView.setText(String.format("%s\n%s", humidity, cloudCoverage));

                    URL url = new URL(WeatherConstants.WEATHER_ICON_URL + res.get("icon") + ".png");
                    Glide.with(getApplicationContext()).load(url).into(weatherIcon);


                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), WeatherConstants.ERROR_MSG, Toast.LENGTH_SHORT).show();
            }
        }
        );
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    /**
     * Method to build url when query paramters are passed in HaspMap in a Key-Value pair
     * @param queryParameters {@link HashMap}
     * @return url {@link String}
     */
    public String buildUrl(HashMap queryParameters){
        String urlParameters = "";

        for ( Object key :queryParameters.keySet() ){
            String val = key.toString() + "=" + queryParameters.get(key);
            if (!urlParameters.isEmpty()){
                urlParameters = urlParameters + "&";
            }
            urlParameters = urlParameters + val;
        }
        return (WeatherConstants.WEATHER_URL + "?" +urlParameters );
    }
}
