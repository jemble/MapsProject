package uk.ac.brookes.bourgein.mapsproject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class LaunchActivity extends SharedMenuActivity implements ResultListener{
	private Chronometer chrono;
	private boolean chronoStopped;
	private Button endButton;
	private boolean isTracking;
	private Route route;
	private ProgressBar progress;
	private ResultListener curActivity;
	private boolean isGPSFix;
	private Location lastLoc;
	private long lastLocMillis;
	private ArrayList<Location> locList;
	private LocationManager locMan;
	private GoogleMap mMap;
	private MapFragment mMapFragment;
	private PolylineOptions polyOps;
	private long timeSincePaused;
	private TextView txtWaiting;
	private Animation blinkAnim;
	private String userName;
	private boolean isAutoUpload;
	private long insertedId;
	final Context context = this; //needed for teh AlertDialog

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.activity_launch);
		init();
		hideUI();

		// include following two when using debugging in vm
//		 isGPSFix = true;
//		 locAchieved();

		chrono.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (chronoStopped) {
					chrono.setBase(SystemClock.elapsedRealtime()
							+ LaunchActivity.this.timeSincePaused);
					chrono.start();
					chronoStopped = false;
					isTracking = true;
					chrono.clearAnimation();
					return;
				}
				timeSincePaused = (LaunchActivity.this.chrono.getBase() - SystemClock
						.elapsedRealtime());
				chrono.stop();
				isTracking = false;
				chronoStopped = true;
				chrono.startAnimation(blinkAnim);
			}
		});

		endButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finishedRun();
			}
		});
	}

	/*
	 * create the animation to blink the chronometer when paused
	 */
	private void setUpBlinking() {
		blinkAnim.setDuration(500); // in millis
		blinkAnim.setStartOffset(20);
		blinkAnim.setRepeatMode(Animation.REVERSE);
		blinkAnim.setRepeatCount(Animation.INFINITE);
	}

	private void hideUI() {
		getFragmentManager().beginTransaction().hide(mMapFragment).commit();
		chrono.setVisibility(View.INVISIBLE);
		endButton.setVisibility(View.INVISIBLE);
	}

	private void getPrefs() {
		SharedPreferences settings = getSharedPreferences(
				PrefsActivity.PREFS_NAME, 0);
		userName = settings.getString(PrefsActivity.USERNAME_PREF, "runt_user");
		isAutoUpload = settings
				.getBoolean(PrefsActivity.AUTOUPLOAD_PREF, false);
	}

	private void showUI() {
		getFragmentManager().beginTransaction().show(mMapFragment).commit();
		chrono.setVisibility(View.VISIBLE);
		endButton.setVisibility(View.VISIBLE);
	}

	private void init() {
		curActivity = this;
		timeSincePaused = 0;
		locList = new ArrayList<Location>();
		chrono = ((Chronometer) findViewById(R.id.chrono));
		txtWaiting = ((TextView) findViewById(R.id.waitingView));
		endButton = ((Button) findViewById(R.id.endButton));
		progress = (ProgressBar) findViewById(R.id.progressBar);
		polyOps = new PolylineOptions().width(5).color(
				getResources().getColor(R.color.fluro_pink));
		mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.launchMap));
		mMap = this.mMapFragment.getMap();
		chronoStopped = true;
		isTracking = false;
		route = new Route();
		blinkAnim = new AlphaAnimation(0.0f, 1.0f);
		setUpBlinking();
		getPrefs();
		route.setUserName(userName);
		insertedId = -1;

		locMan = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		if(locMan.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0,mLocListener);
		}
		if(locMan.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,4000,0,mLocListener);
		}
		locMan.addGpsStatusListener(gpsListener);
		chrono.setText("start\npause");
	}

	public void locAchieved() {
		txtWaiting.setVisibility(View.INVISIBLE);
		progress.setVisibility(View.INVISIBLE);
		showUI();
	}

	public void finishedRun() {
		stopListening();
		isTracking = false;
		chrono.stop();
  
		if(locList.size() > 0){
			handleRoute();
		}
		else{
			showAlert();
		}
		
	}
	
	private void showAlert(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setMessage("Nothing was tracked so no route was created.");
		alertBuilder.setTitle("Nothing tracked!");
		alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent homeIntent = new Intent(getApplicationContext(), Home_Activity.class);
				startActivity(homeIntent);
			}
		});
		alertBuilder.create().show();
	}
	
	private void stopListening(){
		locMan.removeGpsStatusListener(gpsListener);
		locMan.removeUpdates(mLocListener);
	}
	
	private void handleRoute(){
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		dateFormatter.setTimeZone(tz);
		String dateString = dateFormatter.format(new Date(locList.get(0).getTime()));
		String readableDate = new SimpleDateFormat("dd-MM-yyyy hh:mm").format(new Date(locList.get(0).getTime()));
		route.setName(readableDate);
		route.setDateString(dateString);
		addToDB();
		if (isAutoUpload) {
			uploadToServer();
		}
		
		//parcel up the route and open the results activity
		Intent resultsIntent = new Intent(getApplicationContext(),ResultsActivity.class);
		resultsIntent.putExtra("ROUTE", route);
		resultsIntent.putExtra("DB_ID", insertedId);
		startActivity(resultsIntent);
	}

	private void uploadToServer() {
		new ServerHelper(curActivity).store(route.generateXML());	
	}

	private void addToDB() {
		String xml = route.generateXML();
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(RoutesContentProvider.KEY_ROUTEDETAILS, xml);
		cv.put(RoutesContentProvider.KEY_USERID, userName);
		if(!isAutoUpload){
			cv.put(RoutesContentProvider.KEY_UPLOADED, 0);
		}
		else{
			cv.put(RoutesContentProvider.KEY_UPLOADED, 1);
		}
		Uri myUri = cr.insert(RoutesContentProvider.CONTENT_URI, cv);
		insertedId = ContentUris.parseId(myUri);
	}
	
	private void updateDb(long id){
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(RoutesContentProvider.KEY_UPLOADED, 1);
		cr.update(RoutesContentProvider.CONTENT_URI, cv, RoutesContentProvider.KEY_ID+"="+Long.toString(id), null);
	}

	private final LocationListener mLocListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (location == null)
				return;
			lastLocMillis = SystemClock.elapsedRealtime();
			if (isGPSFix && isTracking) {
				if (locList.size() == 0){
					MarkerOptions markerOps = new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.runt_notification));
					mMap.addMarker(markerOps);
				}
				route.addLocation(location);
				locList.add(location);
				LatLng localLatLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				Log.i("JEM",localLatLng.toString());
				polyOps.add(localLatLng);
				mMap.addPolyline(polyOps);
				CameraUpdate localCameraUpdate = CameraUpdateFactory
						.newLatLngZoom(localLatLng, 19);
				mMap.animateCamera(localCameraUpdate);
			}
			lastLoc = location;
		}

		public void onProviderDisabled(String paramAnonymousString) {
		}

		public void onProviderEnabled(String paramAnonymousString) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private final GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				if (lastLoc != null)
					isGPSFix = (SystemClock.elapsedRealtime() - lastLocMillis) < 3000;

				if (isGPSFix) { // A fix has been acquired.
					locAchieved();
				} else { // The fix has been lost.

				}

				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				isGPSFix = true;
				locAchieved();
				break;
			}
		}
	};

	@Override
	public void onRestrieveResult(String result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSearchResult(String result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStoreResult(Boolean result) {
		if(result){
			Toast.makeText(getApplicationContext(), "sync finished",Toast.LENGTH_LONG).show();
			NotificationBuilder.showNotification("Upload complete", route.getName()+ " uploaded", getApplicationContext());
			if(insertedId != -1){
				updateDb(insertedId);
			}
		}
		else{
			NotificationBuilder.showNotification("Upload failed","The run was not uploaded to the server",getApplicationContext());
		}		
	}
}