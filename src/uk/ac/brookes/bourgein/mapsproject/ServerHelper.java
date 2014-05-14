package uk.ac.brookes.bourgein.mapsproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ServerHelper {
	
	private static final String SERVER_ADD = "161.73.245.41";
	private static final int SERVER_PORT = 44365;
	ResultListener mListener;
	Socket socket;
	PrintWriter out;
	BufferedReader input;
	
	public ServerHelper(ResultListener listener){
		this.mListener = listener;
	}
	
	private void createSocket() throws UnknownHostException, IOException{
		socket = new Socket(SERVER_ADD,SERVER_PORT);
		out = new PrintWriter(socket.getOutputStream(),true);
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public void store(String route){
		new StoreTask().execute(route);
	}
	
	public void retrieve(String routeId){
		new RetrieveTask().execute(routeId);
	}
	
	public void search(String username){
		new SearchTask().execute(username);
	}
	
	private class SearchTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
//			boolean resultOK = false;
			
//			Log.i("JEM",userName);
			String finalRes = "";
			try {
				createSocket();
				out.println("search " + params[0] +" ");
				finalRes = input.readLine();
//				Log.i("JEM","theResult: "+finalRes);
				
			} catch (IOException e) {
				Log.i("JEM", e.getMessage());
			}
			return finalRes;
		}

		@Override
		protected void onPostExecute(String result) {
			mListener.onSearchResult(result);
		}
	}
	
	private class RetrieveTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			String theResult = "";
			try {
				createSocket();
				out.println("retrieve "+params[0]+" ");
				theResult= input.readLine();

			} catch (IOException e) {
				Log.i("JEM", e.getMessage());
			}
			return theResult;
		}
		
		@Override
		protected void onPostExecute(String result){
			mListener.onRestrieveResult(result);
		}	
	}
	
	private class StoreTask extends AsyncTask<String, Void, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = false;
			try {
				createSocket();
				out.println("store "+params[0]+" ");
				result = true;
			} catch (IOException e) {
				Log.i("JEM", e.getMessage());
				result = false;
			}
			return result;

		}
		
		@Override
		protected void onPostExecute(Boolean result){
			mListener.onStoreResult(result);
		}
		
	}
	
}
