package hr.tel.fer.cellid;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class FindingLocation extends Service implements LocationListener {
	
	final Context mContext;
	  
    //flags for status
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
 
    Location location;
    double latitude = 0;
    double longitude = 0;
    double lat;
    double longi;
    String result = "";
    ArrayList<String> locationsList = new ArrayList<String>() ;

 
    // Minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
 
    // Minimum time between updates in milliseconds
    long minTimeBetweenUpdates = 1000 * 60 * 10; //  10 min
 
    // Declaring a Location Manager
    protected LocationManager locationManager;
 
    public FindingLocation(Context context, long timeBetween) {
        this.mContext = context;
        minTimeBetweenUpdates = timeBetween * 60 * 1000;
        startLocationService();
    }
    
    public Location startLocationService() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
  
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
            if (!isNetworkEnabled) {
                // no network provider is enabled
            	Toast.makeText(getApplicationContext(),
    					"Network Provider Disabled", Toast.LENGTH_SHORT).show();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            minTimeBetweenUpdates,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return location;
    }
    
    
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }
    
    
    @Override
    public void onLocationChanged(Location location) {
    	lat = location.getLatitude();
		longi = location.getLongitude();
		locationsList.add(""+lat);
		locationsList.add(""+longi);
		getAddress();
    }
 
    @Override
    public void onProviderDisabled(String provider) {
    	Toast.makeText(getApplicationContext(),
				"Network Provider Disabled", Toast.LENGTH_SHORT).show();
    }
 
    @Override
    public void onProviderEnabled(String provider) {
    }
 
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
 
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    
    public boolean canGetLocation() {
        return this.canGetLocation;
    }
     
    
    // Stop using network location listener
    public ArrayList<String> stopUsingNetworkLocation(){
        if(locationManager != null){
            locationManager.removeUpdates(FindingLocation.this);
            canGetLocation = false;
        }
		return locationsList;       
    }
    
    //get address from latitude and longitude
    void getAddress() {
    	Geocoder gcd = new Geocoder(this.mContext, Locale.getDefault());
    	List<Address> addresses = null;
    	try {
    		addresses = gcd.getFromLocation(lat, longi, 1);
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (NullPointerException e){
    		e.printStackTrace();
    	}
    	if (addresses.size() > 0) {
    		locationsList.add(addresses.get(0).getAddressLine(0).toString());
    		System.out.println(addresses.get(0).getAddressLine(0).toString());
    	}
    } 
    
    
    //Function to show settings alert dialog 
    //for enable network location services
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        alertDialog.setTitle("Location in settings");
        alertDialog.setMessage("Network location services are not enabled. Do you want to go to settings menu?");
    
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
  
        // On pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        alertDialog.show();
    }  
	
	}
