package uk.ac.brookes.bourgein.mapsproject;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RoutesListActivity extends SharedMenuActivity {
	
	public static final String ROUTE_EXTRA = "routeId";
	ArrayAdapter<Route> routesAdapter;
	ArrayList<Route> routesList;
	ContentResolver cr;
	ListView lView;
	private String userName;
	private SharedPreferences settings;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes_list);
		lView = (ListView)findViewById(R.id.routesListView);
		cr = getContentResolver();
		routesList = new ArrayList<Route>();
		settings = getSharedPreferences(PrefsActivity.PREFS_NAME, 0);
		userName = settings.getString(PrefsActivity.USERNAME_PREF, null);
//		cr.delete(RoutesContentProvider.CONTENT_URI, null, null);
		Cursor c = cr.query(RoutesContentProvider.CONTENT_URI, null, RoutesContentProvider.KEY_USERID + " = '"+userName+"'", null, null);
		if(c.moveToFirst()){
			do{
				String details = c.getString(RoutesContentProvider.ROUTEDETAILS_COLUMN);
				int id = c.getInt(RoutesContentProvider.ID_COLUMN);
//				Log.i("JEM",details);
				String[] name = details.split("<name>");
				String [] endName = name[1].split("</name>");
//				for(String s:endName){
//					Log.i("JEM",s);
//				}
				Route tRoute = new Route();
				tRoute.setID(id);
				tRoute.setName(endName[0]);
				routesList.add(tRoute);
				
			}
			while(c.moveToNext());
		}
		
		routesAdapter = new ArrayAdapter<Route>(getApplicationContext(), R.layout.pink_list_item,routesList);
		lView.setAdapter(routesAdapter);
		lView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
//				Toast toast = Toast.makeText(getApplicationContext(), "id: "+Long.toString(id) + " pos: "+Integer.toString(pos) +" routeID: "+Integer.toString(routesList.get(pos).getID()), Toast.LENGTH_LONG);
//				toast.show();
				Intent mapIntent = new Intent(getApplicationContext(),RouteMapActivity.class);
				mapIntent.putExtra(ROUTE_EXTRA, routesList.get(pos).getID());
				startActivity(mapIntent);
				
			}
			
		});
		
	}
}
