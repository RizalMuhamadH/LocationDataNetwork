package com.inarts.inarts_user.setlocationdatanetwork;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationActivity extends AppCompatActivity {

    private TextView info;
    private Button btn;
    private TextView address;

    LocationManager locationManager;
    ConnectivityManager connectivityManager;

    private double lat,lng;

    Geocoder geocoder;

    ProgressDialog progressDialog;

    String area;
    String city;
    String state;
    String country;
    String postalCode;
    String knownName;

    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        info = (TextView) findViewById(R.id.info);
        btn = (Button) findViewById(R.id.btn);
        address = (TextView)findViewById(R.id.address);
        geocoder = new Geocoder(this,Locale.getDefault());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(LocationActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                if (isLocationEnabled() == true){
                    locationNetwork();
                }else {
                    alert();
                }

            }
        });
    }
    private void alert() {
        progressDialog.dismiss();
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Alert")
                .setMessage("Your Does Connected to Internet.\nPlease Check Internet Setting ")
//                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
//                        Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
//                        startActivity(myIntent);
//                        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//                        wifiManager.setWifiEnabled(true);
//                    }
//                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

    private void locationNetwork() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 1000, 10, locationListener);
    }
    private final LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lng = location.getLongitude();

            final List<Address> addresses;

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                area = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName();
                if (addresses == null){
                    alert();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText(String.valueOf(lat)+","+String.valueOf(lng));
                    address.setText("Address : "+area+"\n"+
                    "City : "+city+"\n"+
                    "State : "+state+"\n"+
                    "Country : "+country+"\n"+
                    "Code Post : "+postalCode+"\n"+
                    "Known Name : "+knownName);
                    progressDialog.dismiss();
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
