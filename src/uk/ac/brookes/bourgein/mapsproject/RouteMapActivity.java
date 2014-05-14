package uk.ac.brookes.bourgein.mapsproject;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

public class RouteMapActivity extends SharedMenuActivity {
	
	GoogleMap mMap;
	MapFragment mMapFragment;
	String routeDetails;
	Route route;
	Display display;
	Point size;
	int screenWidth;
	int screenHeight;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_map);
		
		mMapFragment = ((MapFragment) getFragmentManager().findFragmentById(
				R.id.routeMapFragment));
		mMap = this.mMapFragment.getMap();
		Bundle bundle = getIntent().getExtras();
		int routeId = bundle.getInt(RoutesListActivity.ROUTE_EXTRA);
//		Toast.makeText(getApplicationContext(), Integer.toString(routeId), Toast.LENGTH_LONG).show();
		ContentResolver cr = this.getContentResolver();
		Cursor c = cr.query(RoutesContentProvider.CONTENT_URI, null, RoutesContentProvider.KEY_ID+" = "+routeId, null, null);
		if(c.moveToFirst()){
			routeDetails = c.getString(RoutesContentProvider.ROUTEDETAILS_COLUMN);
			Log.i("JEM","routeDetails: "+routeDetails);
		}
		ParserHelper helper = new ParserHelper(routeDetails);
		route = helper.getRoute(); 
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for(Location l:route.getLocations()){
			LatLng tempLtLng = new LatLng(l.getLatitude(), l.getLongitude());
			builder.include(tempLtLng);
		}
		getScreenDimens();
		CameraUpdate camUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), screenWidth,screenHeight,70);
//		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
		mMap.animateCamera(camUpdate);
		drawRoute();
		
	}
	
	private void getScreenDimens(){
		display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
	}
	 private void drawRoute(){
		  PolylineOptions polyOps = new PolylineOptions().width(5).color(getResources().getColor(R.color.fluro_pink));
//			Log.i("JEM","route lcoation array size: "+Integer.toString(route.getLocations().size()));
	  		for(Location loc:route.getLocations()){

	  			LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
	  			polyOps.add(ll);
	  		}
	  		mMap.addPolyline(polyOps);
	  }
}
