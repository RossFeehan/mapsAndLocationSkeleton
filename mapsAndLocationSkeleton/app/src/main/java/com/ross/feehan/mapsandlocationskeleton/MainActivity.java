package com.ross.feehan.mapsandlocationskeleton;

import android.content.Context;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GetAddressInterface {

    private Context ctx;
    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private GetAddress getAddress;
    @Bind(R.id.pickupAddressTV) TextView pickupAddressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.ctx = this;
        ButterKnife.bind(this);

        getAddress = new GetAddress(ctx);
        setUpMapAndStartLocationFinding();
    }

    //CLASS METHODS
    /*
    *Set up the map
    */
    private void setUpMapAndStartLocationFinding(){
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mainMap);
        map = mapFrag.getMap();
        map.getUiSettings().setZoomControlsEnabled(false);

        //Connect to LocationClient in order to get devices location
        buildGoogleAPILocationClient();
    }

    /*
    *Set up for Finding GPS Location
    */
    private void buildGoogleAPILocationClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /*Method to create a location request to get updates
     *on the users location
     */
    private LocationRequest createLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    private void displayUsersLocationOnMap(Location location){
        if(location != null) {
            //move the map to this location
            //clear all markers from the map
            map.clear();
            LatLng usersLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(usersLatLng, 16));
            //add a marker at the users location
            map.addMarker(new MarkerOptions()
                    .position(usersLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.geo_fence)));
        }
    }

    //INTERFACE METHODS
    //GoogleApiClient.ConnectionCallbacks METHODS
    @Override
    public void onConnected(Bundle bundle) {

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, createLocationRequest(), this);

        displayUsersLocationOnMap(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));

        getAddress.getAddress(LocationServices.FusedLocationApi.getLastLocation(googleApiClient), this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // GoogleApiClient.OnConnectionFailedListener METHODS
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //LocationListener METHOD
    @Override
    public void onLocationChanged(Location location) {
        displayUsersLocationOnMap(location);
    }

    //ANDROID ACTIVITY LIFECYCLE METHODS
    /* onStart is called when the activity is started, when it appears
     * on device screen again
     */
    @Override
    public void onStart(){
        super.onStart();
        //Connect to LocationClient to receive locations
        googleApiClient.connect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        googleApiClient.disconnect();
    }

    //GetAddressInterface METHODS
    @Override
    public void addressFromLatAndLong(String address) {
        pickupAddressTV.setText(address);
    }


    @Override
    public void noInternet() {
        Toast.makeText(ctx, "Sorry, no internet access at the moment", Toast.LENGTH_LONG).show();
    }
}
