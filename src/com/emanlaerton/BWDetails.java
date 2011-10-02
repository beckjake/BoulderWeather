package com.emanlaerton;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class BWDetails extends Activity {
	private final static long MINUTE = 1000*60;
	private final static long REFRESH_TIMEOUT = 5 * MINUTE; //sounds about right
	private final static String ATOC_URL = "http://foehn.colorado.edu/weather/atoc1/"; 

	TextView date = null;
	private static long lastRefresh=0;
	private static final String prefix = "BoulderWeather";
	private volatile static WeatherMap weatherMap = null; //A Map of the weather, get it? I kill me.
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        date = (TextView) findViewById(R.id.weather_date);
        setContentView(R.layout.details);
        Button refresh = (Button) findViewById(R.id.refresh_button);
	    refresh.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				lastRefresh=SystemClock.elapsedRealtime();
				weatherMap = getWeather(ATOC_URL);
				drawScreen(weatherMap);
			}
	    	
	    });
    }
    /** Called when the activity is started **/
    @Override
    public void onStart(){
    	super.onStart();
    	if(date!=null){ //just in case
    		return;
    	}
    	refreshIfTimerElapsed();
    	
    }
    
    public void refreshIfTimerElapsed(){
    	long currentTime=SystemClock.elapsedRealtime();
    	if (lastRefresh==0){
    		lastRefresh=currentTime;
    		weatherMap = getWeather(ATOC_URL);
    		drawScreen(weatherMap);
    		//Log.i(prefix,"lastRefresh=0, just opened");
    	} else if((currentTime-lastRefresh) > REFRESH_TIMEOUT){
    		//Log.i(prefix,"currentTime="+currentTime+" lastRefresh="+lastRefresh+" difference="+(currentTime-lastRefresh));
    		lastRefresh=currentTime;
    		weatherMap = getWeather(ATOC_URL);
    		drawScreen(weatherMap);
    	} //else don't refresh again, but do the screen draw
    	else {
    		//Log.i(prefix,"else statement hit!");
    		//if we've got the weather stored, use that. otherwise, we'll have to get it again.
    		if(weatherMap==null){ //this indicates Dalvik went and destroyed my activity. Time to rebuild the map.
    			//Log.i(prefix,"Getting the weather even though it should be stored");
    			lastRefresh=currentTime;
    			weatherMap = getWeather(ATOC_URL);
    		}
    		drawScreen(weatherMap);
    	}
    }
    
    /* make a WeatherHelper and use it request our nice map */
    private WeatherMap getWeather(String url){
    	WeatherMap weatherMap = null; //local weatherMap while we build
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		try{
			HttpResponse response = client.execute(request);
			weatherMap = WeatherHelper.read(response);
		}catch(UnknownHostException ex){
			ex.printStackTrace();
			//Log.w(prefix,"Probably couldn't connect to the internet");
			Toast.makeText(getApplicationContext(),"Could not connect! Is your internet connection down?", Toast.LENGTH_SHORT).show();
			return null;
    	}catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		
		return weatherMap;
    }
    
    /* Draw a screen based on a nice map. If it's not a nice map, probably crash in some unpleasant and unexpected way. 
     */
    private boolean drawScreen(WeatherMap wm){
        //final TextView 
        final TextView date = (TextView) findViewById(R.id.weather_date);
		if(wm==null){
			//Log.w(prefix,"Warning, Map was not returned properly!");
			Toast.makeText(getApplicationContext(),"Weather data could not be found!", Toast.LENGTH_SHORT).show();
			date.setText("No results! Check your internet connection.");
		}else{
			ListView lv= (ListView)findViewById(R.id.listview);
	        String[] from = new String[] { "Type", "Current", "Minimum", "Maximum", "Average" };
	        int[] to = new int[] { R.id.Value, R.id.Current_val, R.id.Minimum_val, R.id.Maximum_val, R.id.Average_val };
	        List<HashMap<String, String>> filledMaps = new ArrayList<HashMap<String, String>>();
	        String [] ps = getResources().getStringArray(R.array.prepStrings);
	        //prep the list
	        for(String s: wm.keys()){
	        	//Log.i(prefix,"Stat: "+s);
	        	//Log.i(prefix, "Value: "+wm.getValues(s));
	        	
	        	//This is pretty brittle
	    		HashMap<String, String> prepMap = new HashMap<String,String>();
	    		prepMap.put("Type", s);
	    		String[] rowString = wm.getValues(s);
	    		for(int i=0;i<wm.getValues(s).length;i++){
	    			prepMap.put(ps[i],rowString[i]);
	    		}
	    		filledMaps.add(prepMap);
	        }
	        SimpleAdapter adapter = new SimpleAdapter(this, filledMaps, R.layout.results, from, to);
	        lv.setAdapter(adapter);
			date.setText("Date: "+wm.getDate());
		}
    	return true;
    }
    
    /* Make menu stuff happen */
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	//Load Menu
	       	MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.menu, menu);
	        return true;
	    }
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.Quit:
	            	//Log.w(prefix,"Moving to back");
	            	//we shouldn't quit, so instead we'll fake it
	            	this.moveTaskToBack(true);
	            	break;
	            case R.id.Refresh:
	            	//Log.w(prefix,"Refreshing...");
					lastRefresh=SystemClock.elapsedRealtime();
					weatherMap = getWeather(ATOC_URL);
					drawScreen(weatherMap);
					break;
	            case R.id.Switch:
	            	//Log.w(prefix, "Switching to simple view");
	            	Intent i = new Intent(this, BoulderWeather.class);
	            	this.startActivity(i);
	            	break;
	        }
	        return true;
	    }
    
}