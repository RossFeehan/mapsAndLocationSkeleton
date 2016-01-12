package com.ross.feehan.mapsandlocationskeleton;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ross Feehan on 12/01/2016.
 * Copyright Ross Feehan
 */
public class GetAddress {

    private Context ctx;
    private GetAddressInterface callbackClass;

    //Constructor
    public GetAddress(Context ctx){
        this.ctx = ctx;
    }

    public void getAddress(Location location, GetAddressInterface callbackClass){
        this.callbackClass = callbackClass;
        if(checkInternetConnection()){
            GetAddressThread getAddress = new GetAddressThread();
            getAddress.execute(location);
        }
        else{
            callbackClass.noInternet();
        }
    }

    private class GetAddressThread extends AsyncTask<Location, Void, Void> {

        private List<Address> geoCoderAddress = null;
        private Location location;

        @Override
        protected Void doInBackground(Location... locations) {

            location = locations[0];
            Geocoder geoCoder = new Geocoder(ctx, Locale.getDefault());
            try {
                geoCoderAddress = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            StringBuilder addressBuilder = new StringBuilder();

            //If Geocoder returned an address
            if (geoCoderAddress != null && geoCoderAddress.size() > 0) {
                Address addressReturnedFromGoogle = geoCoderAddress.get(0);
                if (addressReturnedFromGoogle == null) {
                    Toast.makeText(ctx, R.string.addressInfoToast, Toast.LENGTH_LONG).show();
                }
                //get address information
                else {
                    addressBuilder.append(addressReturnedFromGoogle.getAddressLine(0) + ", "
                            + addressReturnedFromGoogle.getAddressLine(1));
                }

                if(!isCancelled()){
                    callbackClass.addressFromLatAndLong(addressBuilder.toString());
                }

            }
        }
    }

    private boolean checkInternetConnection(){
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        else{
            return false;
        }
    }
}
