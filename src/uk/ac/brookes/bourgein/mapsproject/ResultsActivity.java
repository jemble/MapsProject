package uk.ac.brookes.bourgein.mapsproject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ResultsActivity extends SharedMenuActivity implements ResultListener{
	
	TextView maxSpeedText;
	TextView avgSpeedText;
	TextView distTravelText;
	Button btnUpload;
	SharedPreferences settings;
	boolean isAutoUpload;
	ResultListener curActivity;
	Route route;
	private long insertedId;
	boolean isConnected;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		curActivity = this;
		isConnected = false;
		maxSpeedText = (TextView)findViewById(R.id.maxSpeedText);
		avgSpeedText = (TextView)findViewById(R.id.avgSpeedText);
		distTravelText = (TextView)findViewById(R.id.distTravelText);
		btnUpload = (Button)findViewById(R.id.actResultUploadButton);
		settings = getSharedPreferences(PrefsActivity.PREFS_NAME, 0);
		isAutoUpload = settings.getBoolean(PrefsActivity.AUTOUPLOAD_PREF, false);
		//get our parceled up route object
		Bundle bundle = getIntent().getExtras();
		route = bundle.getParcelable("ROUTE");
		insertedId = bundle.getLong("DB_ID");
		
		if(isAutoUpload){
			btnUpload.setVisibility(View.INVISIBLE);
		}
		else{
			btnUpload.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					checkInternetConn();
					if(isConnected){
						new ServerHelper(curActivity).store(route.generateXML());
					}
					else{
						Toast.makeText(getApplicationContext(), "No internet connection. Try again later.", Toast.LENGTH_LONG).show();
					}
					
				}
			});
		}
		
		maxSpeedText.setText("max speed\n"+Float.toString(route.getMaxSpeed())+" metres/sec");
		avgSpeedText.setText("avg speed\n"+Float.toString(route.calcAvgSpeed())+" metres/sec");
		distTravelText.setText("distance\n"+Float.toString(route.calcDistTravelled())+" metres");	
	}
	
	private void checkInternetConn(){
		ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	private void updateDb(long id){
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(RoutesContentProvider.KEY_UPLOADED, 1);
		cr.update(RoutesContentProvider.CONTENT_URI, cv, RoutesContentProvider.KEY_ID+"="+Long.toString(id), null);
	}
	
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
			updateDb(insertedId);
		}
		else{
			NotificationBuilder.showNotification("Upload failed","The run was not uploaded to the server",getApplicationContext());
		}
		
	}

}
