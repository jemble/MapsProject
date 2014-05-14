	package uk.ac.brookes.bourgein.mapsproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

@SuppressLint("NewApi") public class Home_Activity extends SharedMenuActivity{
	Button btnRun;
	Button btnMyRuns;
	Button btnSettings;
	
    @SuppressLint("NewApi") @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_);
        btnSettings = (Button)findViewById(R.id.homeSettingsButton);
        btnRun = (Button)findViewById(R.id.homeRunButton);
        btnMyRuns = (Button)findViewById(R.id.homeMyRunsButton);
        
        
        btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),LaunchActivity.class);
				startActivity(intent);				
			}
		});   
        
        btnSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),PrefsActivity.class);
				startActivity(intent);
				
			}
		});
        
        btnMyRuns.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),RoutesListActivity.class);
				startActivity(intent);
				
			}
		});
    }    
}
