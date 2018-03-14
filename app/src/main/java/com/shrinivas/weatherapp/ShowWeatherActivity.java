package com.shrinivas.weatherapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class ShowWeatherActivity extends AppCompatActivity {

    ImageView weatherIcon;
    TextView cityNameTxtView, temperatureTxtView, weatherTxtView, descriptionTxtView, humidityCloudCoverageTxtView, minMaxTempTxtView;
    Runnable runnable;
    PlaceAutocompleteFragment autocompleteFragment;
    private static final String TAG = "MainActivity";
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    GeoDataClient mGeoDataClient;
    ImageView cityImage;
    LinearLayout linearLayout1, linearLayout2;
    EditText fragmentPlace;

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
        cityImage = findViewById(R.id.cityImage);
        linearLayout1 = findViewById(R.id.linearLayout1);
        linearLayout2=findViewById(R.id.linearLayout2);


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Enter City Name");
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                call_api(place.getName().toString());
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });


    }

    private void getPhotos(final String placeId) {

        //final String placeId = "ChIJa147K9HX3IAR-lwiGIQv9i4";
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
                        cityImage.setImageBitmap(bitmap);

                    }
                });
            }
        });
    }

    public void call_api(final String cityName) {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeatherResponse(cityName);
            }
        };

        Thread thread = new Thread(null, runnable, "background");
        thread.start();
    }

    public void getWeatherResponse(final String cityName) {

        final String url = WeatherConstants.WEATHER_URL;
        String urlWithBase = url.concat(TextUtils.isEmpty(cityName) ? "Halifax" : cityName);
        urlWithBase = urlWithBase.concat("&");
        urlWithBase = urlWithBase.concat(WeatherConstants.APPID_Key + WeatherConstants.APPID_Value);
        urlWithBase = urlWithBase.concat("&");
        urlWithBase = urlWithBase.concat(WeatherConstants.UNITS_Key + WeatherConstants.UNITS_Value);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlWithBase, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show();

                try {

                    linearLayout1.setBackgroundColor(getColor(R.color.colorWhite));

                    JSONObject weatherResponseOnject = (JSONObject) response.get("main");
                    JSONObject res = (JSONObject) response.getJSONArray("weather").get(0);

                    String degree = (char) 0x00B0 + "C";
                    String displayCityName = response.get("name").toString();
                    String temp = weatherResponseOnject.get("temp").toString() + degree;
                    String min = "Min. " + weatherResponseOnject.get("temp_min").toString() + degree;
                    String max = "Max. " + weatherResponseOnject.get("temp_max").toString() + degree;

                    String cloudCoverage = "Clouds\t: " + response.getJSONObject("clouds").get("all").toString() + "%";
                    String  humidity= "Humidity\t: "+ weatherResponseOnject.get("humidity").toString() + "%";

                     cityNameTxtView.setText(displayCityName);
                    temperatureTxtView.setText(temp);

                    minMaxTempTxtView.setText(String.format("%s \t\t %s", min, max));
                    weatherTxtView.setText(res.get("main").toString());
                    descriptionTxtView.setText(StringUtils.capitalize(res.get("description").toString()));
                    humidityCloudCoverageTxtView.setText(String.format("%s\n%s",humidity,cloudCoverage));

                    URL url = new URL(WeatherConstants.WEATHER_ICON_URL + res.get("icon") + ".png");
                    Glide.with(getApplicationContext()).load(url).into(weatherIcon);


                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                Toast.makeText(getApplicationContext(), WeatherConstants.ERROR_MSG, Toast.LENGTH_SHORT).show();
            }
        }
        );

        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

}
