package uk.ac.brookes.bourgein.mapsproject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/*
 * This now implements Parcelable to allow for passing route objects between
 * activities.
 * 
 */
public class Route implements Parcelable{
	private ArrayList<Location> locList;
	private String userName;
	private String name;
	private int ID;
	private String dateString;
	
	public Route(String name, int ID, String dateString, String userName){
		this.setName(name);
		this.ID = ID;
		this.dateString = dateString;
		this.userName = userName;
		locList = new ArrayList<Location>();
	}
	
	public Route(){
		locList = new ArrayList<Location>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setUserName(String userName){
		this.userName = userName;
	}
	
	public String getUserName(){
		return this.userName;
	}
	
	public int getID(){
		return ID;
	}
	
	public void setID(int id){
		this.ID = id;
	}
	
	public String getDateString(){
		return dateString;
	}
	
	public void setDateString(String dateString){
		this.dateString = dateString;
	}
	
	public void addLocation(Location loc){
		locList.add(loc);
	}
	public void addLocation(int position, Location loc){
		locList.add(position, loc);
	}
	
	public ArrayList<Location> getLocations(){
		return locList;
	}
	
	public Location getLocation(int position){
		return locList.get(position);
	}
	
	public String toString(){
		return name;
	}
	
	public float calcAvgSpeed(){
		float avgSpeed = 0;
		for(Location l:locList){
			avgSpeed += l.getSpeed();
		}
		return avgSpeed/locList.size();	
	}
	
	public float getMaxSpeed(){
		ArrayList<Float> speeds = new ArrayList<Float>();
		for(Location l:locList){
			speeds.add(l.getSpeed());
		}
		return Collections.max(speeds);
	}
	
	public float calcDistTravelled(){
		float distTravelled = 0;
		for(int i=locList.size()-1;i>0;i--){
			distTravelled += locList.get(i).distanceTo(locList.get(i-1));
		}
		return distTravelled;
	}
	
	public String generateXML(){
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		dateFormatter.setTimeZone(tz);
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"><brookesml>";
		xml += "<user-id>"+this.userName+"</user-id>";
		xml += "<date>"+dateString+"</date>";
		xml += "<trk>";
		xml += "<name>"+name+"</name>";
		xml += "<trkseg>";
		for(Location l:locList){
			xml += "<trkpt lat=\""+Double.toString(l.getLatitude())+"\" lon=\""+Double.toString(l.getLongitude())+"\">";
			xml += "<ele>"+Double.toString(l.getAltitude())+"</ele>";
			xml += "<time>"+dateFormatter.format(new Date(l.getTime()))+"</time>";
			xml += "</trkpt>";
		}
		xml += "</trkseg></trk></brookesml>";
		Log.i("JEM","Generated xml: "+xml);
		return xml;
		
	}
	
	public LatLng findSWpoint(){
		double lowLat = locList.get(0).getLatitude();
		double lowLng = locList.get(0).getLongitude();
		
		for(Location l:locList){
			if(l.getLatitude() < lowLat) lowLat = l.getLatitude();
			if(l.getLongitude() < lowLng) lowLat = l.getLongitude();
		}
		return new LatLng(lowLat, lowLng);
	}
	
	public LatLng findNEpoint(){
		double highLat = locList.get(0).getLatitude();
		double highLng = locList.get(0).getLongitude();
		for (Location l:locList){
			if(l.getLatitude() > highLat) highLat = l.getLatitude();
			if(l.getLongitude() > highLng) highLng = l.getLongitude();
		}
		return new LatLng(highLat,highLng);
	}

	//************************************Parcelable stuff****************************************//
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(locList);
		dest.writeString(name);
		dest.writeString(dateString);
		dest.writeString(userName);
		dest.writeInt(ID);
	}
	
	public static final Parcelable.Creator<Route> CREATOR = new Parcelable.Creator<Route>() {
		public Route createFromParcel(Parcel in){
			return new Route(in);
		}

		@Override
		public Route[] newArray(int size) {
			return new Route[size];
		}
	};
	
	private Route(Parcel in) {
		locList = new ArrayList<Location>();
        in.readTypedList(locList, Location.CREATOR); //this must be typedList as we have written with typedList
        name = in.readString();
        dateString = in.readString();
        userName = in.readString();
        ID = in.readInt();
    }
	
//	public Long calcTimeTaken(){
//		return locList.get(locList.size()-1).getTime() - locList.get(0).getTime();
//	}
}
