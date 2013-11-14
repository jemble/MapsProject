package uk.ac.brookes.bourgein.mapsproject;

import java.util.ArrayList;
import java.util.Date;

import android.location.Location;


public class Route {
	private ArrayList<Location> locList;
	private String name;
	private int ID;
	private Date date;
	
	public Route(String name, int ID, Date date){
		this.setName(name);
		this.ID = ID;
		this.date = date;
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
	
	public int getID(){
		return ID;
	}
	
	public void setID(int id){
		this.ID = id;
	}
	
	public Date getDate(){
		return date;
	}
	
	public void setDate(Date date){
		this.date = date;
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
	
}
