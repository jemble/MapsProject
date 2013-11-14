package uk.ac.brookes.bourgein.mapsproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import uk.ac.brookes.bourgein.mapsproject.dummy.DummyContent;

/**
 * A fragment representing a single Route detail screen. This fragment is either
 * contained in a {@link RouteListActivity} in two-pane mode (on tablets) or a
 * {@link RouteDetailActivity} on handsets.
 */
public class RouteDetailFragment extends Fragment {
	
	private GoogleMap map;
	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Route route;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public RouteDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
//			route = new Route("Test Route Blah Blah",1,new Date());
			
			ReaderTask readerTask = new ReaderTask();
	    	try {
				URL url = new URL("http://http://161.73.92.170:44365/");
				readerTask.execute(url);

			} catch (MalformedURLException e) {
				Log.v("JEM","Bad URL");
			}		
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_route_detail,
				container, false);
		

		// Show the dummy content as text in a TextView.
		if (route != null) {
			map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(route.getLocation(0).getLatitude(), route.getLocation(0).getLongitude()),21);
			map.animateCamera(cameraUpdate);
			drawRoute();
			
		}
		return rootView;
	}
	
	 private void drawRoute(){
		  PolylineOptions polyOps = new PolylineOptions().width(5).color(Color.RED);
			Log.i("JEM","route lcoation array size: "+Integer.toString(route.getLocations().size()));
	  		for(Location loc:route.getLocations()){

	  			LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
	  			polyOps.add(ll);
	  		}
	  		map.addPolyline(polyOps);
	  }
	 
	 private class ReaderTask extends AsyncTask<URL, Void, Route>{
		private XmlPullParser parser;
		 
		@Override
		protected Route doInBackground(URL... params) {
			Log.i("JEM","doInBackground called");
			Route result = new Route();
			try{
//				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				parser = Xml.newPullParser();
				
//				parser.setInput(params[0].openStream(), "UTF-8");
				
				/*
				 * Attempt to do sockets
				 * 
				 */
//				InetAddress serverAddr = InetAddress.getByName("10.0.2.2");
				Socket socket = new Socket("192.168.1.77", 44363);
				PrintStream out = new PrintStream(socket.getOutputStream(),true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				out.write(("SEARCH 2").getBytes());
				//out.close();
				
				String st = "Line: " +input.readLine();
				socket.close();
				Log.i("JEM", st);
				
				
//				while(!isEndTag(parser, "brookesml") && !isEndDoc(parser)){
////					Log.i("JEM",parser.toString());
//					if(isStartTag(parser, "name")){
//						result.setName(getTagText(parser, "name"));
//					}
////					else if(isStartTag(parser, "date")){
////						String sDate = getTagText(parser, "date");
////						Date date = new Date(sDate);
////						result.setDate(date);				
////					}
//					else if (isStartTag(parser,"trkpt")){
//						Location newLoc = new Location("A");
//						Double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
//						Double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
//						newLoc.setLatitude(lat);
//						newLoc.setLongitude(lon);
//						result.addLocation(newLoc);
//					}
//					parser.next();
//				}
				
				
			}
			catch(IOException e){
				Log.v("JEM","IOException reading feed: "+e.getMessage());
			}
//			catch(XmlPullParserException e){
//				Log.v("JEM","XMLPullParser Exception: "+e.getMessage());
//			}
//			Log.i("JEM","result name: "+result.getName());
			return result;
		}
		
		private String getTagText(XmlPullParser parser, String name) throws XmlPullParserException, IOException{
			String result = null;
			while(!isEndTag(parser,name) && !isEndDoc(parser)){
				if(parser.getEventType() == XmlPullParser.TEXT){
					result = parser.getText();
				}
				parser.next();
			}
			return result;
		}
		
		private boolean isStartTag(XmlPullParser parser, String name) throws XmlPullParserException{
			return (parser.getEventType() == XmlPullParser.START_TAG && parser.getName().equalsIgnoreCase(name));
		}
		
		private boolean isEndTag(XmlPullParser parser, String name) throws XmlPullParserException{
			return (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase(name));
		}
		
		private boolean isEndDoc(XmlPullParser parser) throws XmlPullParserException {
			return (parser.getEventType() == XmlPullParser.END_DOCUMENT);
		}
		
		@Override
		protected void onPostExecute(Route result){
			Log.i("JEM","onPostExecute called" + " result: "+ result.getName());
			route = result;
			map = ((SupportMapFragment)getFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(route.getLocation(0).getLatitude(), route.getLocation(0).getLongitude()),5);
			map.animateCamera(cameraUpdate);
			drawRoute();
			
		}
		 
	 }
}
