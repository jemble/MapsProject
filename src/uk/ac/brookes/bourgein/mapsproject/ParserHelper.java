package uk.ac.brookes.bourgein.mapsproject;

import java.io.IOException;
import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.location.Location;
import android.util.Log;

public class ParserHelper {
	private XmlPullParser parser;
	String routeString;
	String[] splitStr;
	public ParserHelper(String routeString){
		this.routeString = routeString;
		 splitStr = routeString.split("<?xml version=\"1.0\" encoding=\"UTF-8\">");
		try{
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			parser = factory.newPullParser();
			parser.setInput(new StringReader(splitStr[1]));
		}
		catch(XmlPullParserException ex){
			Log.i("JEM",ex.getMessage());
		}
		
	}
	
	public Route getRoute(){
		Route result = new Route();
		
		try {
			while(!isEndTag(parser, "brookesml") && !isEndDoc(parser)){
//				Log.i("JEM","while started");
				if(isStartTag(parser, "name")){
//					Log.i("JEM","is starttag name");
					result.setName(getTagText(parser, "name"));
					
				}

				else if (isStartTag(parser,"trkpt")){
//					Log.i("JEM","is startag trkpt");
					Location newLoc = new Location("A");
					Double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
					Double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
					newLoc.setLatitude(lat);
					newLoc.setLongitude(lon);
					result.addLocation(newLoc);
				}
				parser.next();
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
}
