package hr.tel.fer.cellid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CellIDActivity extends Activity {

	Button bShowLocation;
	Button bStopLocation;
	Button bMail;
	TextView tvResLoc;
	TextView tvTimeBet;
	EditText etMin;
	String result;
	double latitude;
	double longitude;
	String minTime = "";
	long minT;
	public boolean buttonStartPressed = false;
	ArrayList<String> locations;
	File file = null;
	String dataString = "";
	
	FindingLocation netLoc;

	@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_cell_id);
	        	         
	        bShowLocation = (Button) findViewById(R.id.bShowLocation);
	        bStopLocation = (Button) findViewById(R.id.bStopLocation);
	        bMail = (Button) findViewById(R.id.bMail);
	        tvResLoc = (TextView) findViewById(R.id.tvResult);
	        tvTimeBet = (TextView) findViewById(R.id.tvTimeBet);
	        etMin = (EditText) findViewById(R.id.etMin);

	        // start searching for location button click event
	        bShowLocation.setOnClickListener(new View.OnClickListener() {
	             
	            @Override
	            public void onClick(View arg0) {  
	            	
	            	minTime = etMin.getText().toString();
	            	if (minTime == "") {
	            		minT = 10;
	            	} else {
	            		try {
	                    minT = Long.parseLong(minTime);
	                    tvTimeBet.setText(minTime + " min");
	                 } catch (NumberFormatException nfe) {
	                	 minT = 10;
	                    System.out.println("NumberFormatException: " + nfe.getMessage());
	                 }
	            		if (minT>120) {
	            			minT = 120; 
	                    	tvTimeBet.setText("120 min");
	            		}
	            	}
	            	
	            	if (!buttonStartPressed) {
	            		buttonStartPressed = true;
	            		tvResLoc.setText("");
	            		netLoc = new FindingLocation(CellIDActivity.this, minT);
	 
            			// can't get location, ask user to enable network in settings
	            		if (!netLoc.canGetLocation) {
	            			buttonStartPressed = false;
	            			netLoc.showSettingsAlert();
	            		}
	            	} else {
            			Toast.makeText(getApplicationContext(), "App is already working", Toast.LENGTH_LONG).show();    
	            	}
	            }
	        });
	        
	        //stop searching for location
	        //call method for creating csv file with locations
	        bStopLocation.setOnClickListener(new View.OnClickListener() {      
	            @Override
	            public void onClick(View arg0) {
	            	buttonStartPressed = false;
	                if(netLoc.canGetLocation()){
	                	 locations = netLoc.stopUsingNetworkLocation();
	                	 getLocationsCSV();
	                }
	            }  
	            });
	        
	        
	        //send CSV file via e-mail
	        bMail.setOnClickListener(new View.OnClickListener() {            
	            @Override
	            public void onClick(View arg0) {
	            	sendEmail();
	            }
	            });        
			}
	  
	//creating csv file with locations found by network service
	void getLocationsCSV() {
		dataString = "";
		String columnString = "" + '"' + "Latitude" + '"' + "," + '"' + "Longitude" + '"' + "," + '"' + "Address" + '"';
		for (int i = 0; i < locations.size(); i=i+3) {
			dataString += locations.get(i) + "," + locations.get(i+1) + "," + locations.get(i+2) +"\n";	
		}
		tvResLoc.setText(dataString);
		String combinedString = columnString + "\n" + dataString;

		File root = Environment.getExternalStorageDirectory();
		if (root.canWrite()){
			File dir = new File (root.getAbsolutePath() + "/Locations");
			dir.mkdirs();
			file = new File(dir, "LocationCellID.csv");
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				out.write(combinedString.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

	//send created csv file as e-mail attachment
	void sendEmail() {
		if (buttonStartPressed == false && dataString != "") {
			Uri u1 = Uri.fromFile(file);
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_SUBJECT, "My locations");
			i.putExtra(Intent.EXTRA_STREAM, u1);
			i.setType("text/html");
			try {
				startActivity(Intent.createChooser(i, "Send mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(CellIDActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			} 
		} else {
			Toast.makeText(CellIDActivity.this, "There is no CSV file", Toast.LENGTH_SHORT).show();
		}  	
	}
	
	

}