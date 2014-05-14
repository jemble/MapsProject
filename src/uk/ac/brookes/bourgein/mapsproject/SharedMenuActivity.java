package uk.ac.brookes.bourgein.mapsproject;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
/*
 * This activity is purely so we can have the same menu on every other activity
 * note that it only overrides onCreateOptionsMenu and onOptionsItemSelected
 * All other activities inherit from this
 */
public class SharedMenuActivity extends Activity {
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.all_menus, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.action_home:
			Intent homeIntent = new Intent(getApplicationContext(),Home_Activity.class);
			startActivity(homeIntent);
			return true;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(getApplicationContext(),PrefsActivity.class);
			startActivity(settingsIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
				
		}
		
	}

}
