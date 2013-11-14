	package uk.ac.brookes.bourgein.mapsproject;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("NewApi") public class Home_Activity extends Activity {
	GoogleMap mMap;
	Button btnPlot;
	Button btnStart;
	Button btnStop;
	Button btnChange;
	TextView editStatus;
	LocationManager locMan;
	ArrayList<LatLng> locList;
	Long startTime, endTime;

    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);
        populateDB();
        btnPlot = (Button)findViewById(R.id.btnPlot);
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStop = (Button)findViewById(R.id.btnEnd);
        btnChange = (Button)findViewById(R.id.btnChange);
        editStatus = (TextView)findViewById(R.id.editStatus);
        
        btnStop.setEnabled(false);
        btnPlot.setEnabled(false);
        
        mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
        locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 1, mLocListener);
        
        locList = new ArrayList<LatLng>();
        
        btnChange.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),RouteListActivity.class);
				startActivity(intent);
				
				
			}
		});
        
        btnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startTracking();
				startTime = System.currentTimeMillis();
				editStatus.setText("tracking started at: " + Long.toString(startTime)) ;
				btnStart.setEnabled(false);
				btnStop.setEnabled(true);
			}
		});
        
        btnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				endTime = System.currentTimeMillis();
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				if(locList.size()>0){
					btnPlot.setEnabled(true);
				}
				endTracking();
				Long finalTime = endTime - startTime;
				CharSequence s  = DateFormat.format("h:mm:ss", finalTime);
				editStatus.setText("not tracking: " + s);			
			}
		});
        
        btnPlot.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(locList.get(0), 5);
				 mMap.animateCamera(cameraUpdate);
				 if(locList.size() > 0){
					 drawMap();
				 }
			}
		});      
    }
  
    private void startTracking(){    	
    	locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 1, mLocListener);
    }
    
    private void endTracking(){   	
    	locMan.removeUpdates(mLocListener);
    }
    
    private final LocationListener mLocListener = new LocationListener(){

		@Override
		public void onProviderDisabled(String provider) {
			editStatus.setText("no gps");
			Toast noGPSToast = Toast.makeText(getApplicationContext(), "No GPS enabled", Toast.LENGTH_LONG);
			noGPSToast.show();	
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onLocationChanged(Location location) {
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			
			locList.add(latLng);
		}
  	
  };
  
  private void drawMap(){
	  PolylineOptions polyOps = new PolylineOptions().width(5).color(Color.RED);
  		for(LatLng ll:locList){
  			polyOps.add(ll);
  		}
  		mMap.addPolyline(polyOps);
  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_, menu);
        return true;
    }
    
    private void populateDB(){
    	
		String[] details = {"test one","test two"};
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		for (int i=0;i<details.length;i++){
			cv.put(RoutesContentProvider.KEY_ROUTEDETAILS, details[i]);
			cr.insert(RoutesContentProvider.CONTENT_URI, cv);
		}
	}
    
}
