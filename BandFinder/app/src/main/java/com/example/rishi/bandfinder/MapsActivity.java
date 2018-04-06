package com.example.rishi.bandfinder;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.rishi.bandfinder.helpers.SessionManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;
    private SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //changing style of map
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        //asking for permissions here!
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Asking for permissions");
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},1);
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }
    }

    private void clearMarkers(){
        mMap.clear();
    }

    private String retrieveCloseMarkersAndDraw(final double currentLatitude, final double currentLongitude){
        String nearby = "";
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_RETRIEVE_GPS,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response:", response);
                        try {
                            JSONArray jsonReply = new JSONArray(response.toString());
                            JSONObject obj;
                            for (int i=0; i < jsonReply.length(); i++){
                                obj = jsonReply.getJSONObject(i);
                                //parsing the request
                                String email = obj.getString("email");
                                Double latitude = obj.getDouble("latitude");
                                Double longitude = obj.getDouble("longitude");
                                //draw each marker returned
                                drawMarker(email,latitude,longitude);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response:", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                //creating the request and drawing from the session manager
                session = new SessionManager(getApplicationContext());
                Map<String, String>  params = new HashMap<>();
                params.put("lat", Double.toString(currentLatitude) );
                params.put("long",Double.toString(currentLongitude));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"req_nearby");
        return nearby;
    }

    private void drawMarker(String email, Double latitude,Double longitude){
        //draws the markers
        LatLng latLng = new LatLng(latitude,longitude);
        mMap.setOnMarkerClickListener(this);
        MarkerOptions options = new MarkerOptions().position(latLng).title(email);
        mMap.addMarker(options);
    }

    private void handleNewLocation(Location location) {
        //the main loop
        clearMarkers();
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        sendLocationToServer(currentLatitude,currentLongitude);
        retrieveCloseMarkersAndDraw(currentLatitude,currentLongitude);

        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(10));


    }

    private void sendLocationToServer(final double currentLatitude, final double currentLongitude) {
        //update location with the server
        StringRequest postRequest = new StringRequest(Request.Method.POST, AppConfig.URL_SEND_GPS,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response: ", response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response:", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                //creating the request
                session = new SessionManager(getApplicationContext());
                Map<String, String>  params = new HashMap<>();
                params.put("email",session.getEmail());
                params.put("lat", Double.toString(currentLatitude) );
                params.put("long",Double.toString(currentLongitude));
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(postRequest,"req_gps");
    }

    @Override
    public boolean onMarkerClick(final Marker marker){
        //on click listener, pass along the email of the clicked user
        Log.d(TAG,marker.getTitle());
        Intent intent = new Intent(MapsActivity.this, ProfileActivity.class);
        Log.d("Popcorn",marker.getTitle().toString());
        intent.putExtra("email",marker.getTitle());
        startActivity(intent);
        return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //oh no, throw an error and print it, API is probably down
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onBackPressed() {
        //go back to main menu
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}


