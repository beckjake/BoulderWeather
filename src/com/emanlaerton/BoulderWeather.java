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
import android.os.SystemClock;


public class BoulderWeather extends Activity {
	private final static long MINUTE = 1000*60;
	private final static long REFRESH_TIMEOUT = 5 * MINUTE; //sounds about right
	private final static String ATOC_URL = "http://foehn.colorado.edu/weather/atoc1/"; 
	TextView date = null;
	TextView viewText = null;
	private static long lastRefresh=0;
	private static final String prefix = "BoulderWeather";
	private volatile static WeatherMap weatherMap = null; //A Map of the weather, get it? I kill me.
	private Integer view = WeatherMap.CURRENT; //default
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        date = (TextView) findViewById(R.id.weather_date);
        setContentView(R.layout.main);
        Button refresh = (Button) findViewById(R.id.refresh_button);
        Button svone = (Button) findViewById(R.id.svone);
        Button svtwo = (Button) findViewById(R.id.svtwo);
        Button svthree = (Button) findViewById(R.id.svthree);
        viewText = (TextView) findViewById(R.id.weather_mode);
        viewText.setText(getView(view)+" Weather");
	    refresh.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				lastRefresh=SystemClock.elapsedRealtime();
				weatherMap = getWeather(ATOC_URL);
				drawScreen(weatherMap);
			}
	    });
	    svone.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		switchView(1);
	    		refreshIfTimerElapsed();
	    	}
	    });
	    svtwo.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		switchView(2);
	    		refreshIfTimerElapsed();
	    	}
	    });
	    svthree.setOnClickListener(new View.OnClickListener(){
	    	public void onClick(View v) {
	    		switchView(3);
	    		refreshIfTimerElapsed();
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
	        String[] from = new String[] { "Type", getView(view) };
	        int[] to = new int[] { R.id.Key, R.id.Value};
	        List<HashMap<String, String>> filledMaps = new ArrayList<HashMap<String, String>>();
	        String ps = getResources().getStringArray(R.array.prepStrings)[view];
	        //prep the list
        	//Log.i(prefix, "view: "+getView(view));
	        for(String s : wm.keys()){
				//Log.i(prefix,"Stat: "+s);
				//Log.i(prefix, "Value: "+wm.getValues(s)[view]);
	        	
	        	
	    		HashMap<String, String> prepMap = new HashMap<String,String>();
	    		prepMap.put("Type", s);
	    		String[] rowString = wm.getValues(s);
	    		prepMap.put(ps,rowString[view]);
	    		filledMaps.add(prepMap);
	        }
	        SimpleAdapter adapter = new SimpleAdapter(this, filledMaps, R.layout.simple, from, to);
	        lv.setAdapter(adapter);
			date.setText("Date: "+wm.getDate());
		}
    	return true;
    }
    
    private String getView(Integer view) {
    	String ps[] = getResources().getStringArray(R.array.prepStrings);
    	if(view < ps.length){
    		return ps[view];
    	}
    	
		return null;
	}
    private boolean switchView(Integer button){
    	Integer newView;
    	String ps[] = getResources().getStringArray(R.array.prepStrings);
    	//View is 0: Button n -> view n
    	//View is 1: Button 1 -> view 0, 2->2, 3->3 ((v == b) ? b-1 : b) 
    	//View is 2: 1->0, 2->1, 3->3 ((v <= b
    	//View is 3: 1->0, 2->1, 3->2
    	newView = ((button > view) ? button : button-1);
    	Button sv[] = new Button[3];
        sv[0] = (Button) findViewById(R.id.svone);
        sv[1] = (Button) findViewById(R.id.svtwo);
        sv[2] = (Button) findViewById(R.id.svthree);
        for(Integer i=0; i<3; i++){
        	String text = (i < newView) ? ps[i] : ps[i+1];
        	sv[i].setText(text);
        }
        view = newView;
        if(viewText!=null){
        	viewText.setText(getView(view)+" Weather");
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
            	//Log.w(prefix, "Switching to details view");
            	Intent i = new Intent(this, BWDetails.class);
            	this.startActivity(i);
            	break;
        }
	        return true;
	    }
    
}