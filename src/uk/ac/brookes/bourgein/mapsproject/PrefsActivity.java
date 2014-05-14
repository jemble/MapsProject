package uk.ac.brookes.bourgein.mapsproject;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class PrefsActivity extends SharedMenuActivity implements ResultListener{
	
	public static final String PREFS_NAME = "RuntPrefsFile";
	public static final String USERNAME_PREF = "usernamePref";
	public static final String AUTOUPLOAD_PREF = "autoUploadPref";

	private EditText editUserName;
	private Switch switchUpload;
	private Button btnSave;
	private Button btnSyncTo;
	private Button btnSyncFrom;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
	private String uName;
	private String[] routesId;
	private ArrayList<Route> routesList;
	private boolean isAutoUpload;
	private int currentRouteInd;
	private ResultListener curActivity;
	final Context context = this; //needed for teh AlertDialog
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_prefs);
		editUserName = (EditText)findViewById(R.id.prefsUnameEdit);
		switchUpload = (Switch)findViewById(R.id.prefsAutoUlSwitch);
		btnSave = (Button)findViewById(R.id.prefsSaveButton);
		btnSyncTo = (Button)findViewById(R.id.prefsSyncToButton);
		btnSyncFrom = (Button)findViewById(R.id.prefsSyncFromButton);
		settings = getSharedPreferences(PREFS_NAME, 0);
		editor = settings.edit();
		uName = settings.getString(USERNAME_PREF, null);
		routesList = new ArrayList<Route>();
		isAutoUpload = settings.getBoolean(AUTOUPLOAD_PREF, false);
		curActivity = this;
		
		editUserName.setText(uName);
		switchUpload.setChecked(isAutoUpload);
		
		btnSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uName = editUserName.getText().toString();
				if (uName!=null){
					editor.putString(USERNAME_PREF,editUserName.getText().toString());
				}
				editor.putBoolean(AUTOUPLOAD_PREF,switchUpload.isChecked());
				editor.commit();
				Toast.makeText(getApplicationContext(), "Preferences saved", Toast.LENGTH_LONG).show();
			}
		});
		
		btnSyncTo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getNonUploadedRoutesFromDatabase();
				
			}
		});
		
		btnSyncFrom.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
				alertBuilder.setMessage("This will wipe all the runs stored on your device and replace them with ones from the server. \nAre you sure?!");
				alertBuilder.setTitle("Replace device runs with server runs");
				alertBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						uName = settings.getString(USERNAME_PREF, null);
						new ServerHelper(curActivity).search(uName);
					}
				});
				alertBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						
					}
				});
				alertBuilder.create().show();
				
			}
		});

		
	}
		
	private void getNonUploadedRoutesFromDatabase(){
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(RoutesContentProvider.CONTENT_URI, null, RoutesContentProvider.KEY_UPLOADED+" = 0 AND "+RoutesContentProvider.KEY_USERID + " = '"+uName+"'", null, null);
		if(c.moveToFirst()){
			do{
				String routeDetails = c.getString(RoutesContentProvider.ROUTEDETAILS_COLUMN);
				Log.i("JEM","rotueDetails from db: "+routeDetails);
				new ServerHelper(curActivity).store(routeDetails);
				Long rowId = c.getLong(RoutesContentProvider.ID_COLUMN);
				updateDB(rowId);
			}
			while(c.moveToNext());
		}
		else{
			Toast.makeText(getApplicationContext(), "No routes to upload", Toast.LENGTH_LONG).show();
		}
	}
	
	private void updateDB(Long rowId){
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(RoutesContentProvider.KEY_UPLOADED, 1);
		cr.update(RoutesContentProvider.CONTENT_URI, cv, RoutesContentProvider.KEY_ID+"="+Long.toString(rowId), null);	
	}
	
	private void setRoutesId(String routeList){
		routesId = routeList.split(",");
	}

	private void clearDatabase(){
		uName = settings.getString(USERNAME_PREF, null);
		ContentResolver cr = getContentResolver();
		cr.delete(RoutesContentProvider.CONTENT_URI, RoutesContentProvider.KEY_USERID + " = '"+uName+"'", null);
	}
	
	private void addOneRouteToDB(Route route){
		String xml = route.generateXML();
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(RoutesContentProvider.KEY_ROUTEDETAILS, xml);
		cv.put(RoutesContentProvider.KEY_USERID, uName);
		cv.put(RoutesContentProvider.KEY_UPLOADED, 1);
		cr.insert(RoutesContentProvider.CONTENT_URI, cv);
	}

	@Override
	public void onRestrieveResult(String result) {
		if(currentRouteInd>=0){
			//add them to a list so that we can check we have them all before deleting the current database			
			routesList.add(new ParserHelper(result).getRoute());
			new ServerHelper(curActivity).retrieve(routesId[currentRouteInd]);
			Log.i("JEM","CurrentRouteInd ="+Integer.toString(currentRouteInd));
			currentRouteInd--;
		}
		else{
			clearDatabase();
			for(Route r:routesList){
				addOneRouteToDB(r);
			}
			NotificationBuilder.showNotification("Sync complete", "The sync from server has completed", getApplicationContext());
		}
		
	}

	@Override
	public void onSearchResult(String result) {
		if((result == null) || (result.contains("No routes found."))){
			Toast.makeText(getApplicationContext(), "No routes found on the server", Toast.LENGTH_LONG).show();
		}
		else {
			setRoutesId(result);
			currentRouteInd = routesId.length-1;
			new ServerHelper(this).retrieve(routesId[currentRouteInd]);	
		}
		
	}

	@Override
	public void onStoreResult(Boolean result) {
		if (result){
			NotificationBuilder.showNotification("Upload complete", "Route was succussfully uploaded", getApplicationContext());
		}
		else{
			NotificationBuilder.showNotification("Upload failed", "Route did not upload", getApplicationContext());
		}
	}
}
