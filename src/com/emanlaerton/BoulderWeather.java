package com.emanlaerton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.os.SystemClock;



public class BoulderWeather extends Activity {
	final static long MINUTE = 1000*60;
	final static long REFRESH_TIMEOUT = 5 * MINUTE; //sounds about right
	
	File cacheFile = null;
	TextView date = null;
	static long lastRefresh=0;
	private static final String prefix = "BoxulderWeather";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        //set up our cache
        File cacheDir = getCacheDir();
        cacheFile = new File(cacheDir, "bwData.dat");
        if(cacheFile.exists()){
        	if(! cacheFile.delete()){
        		//oh dear. I don't even know what to do here. try again?
        		cacheFile.delete();
        	}
        }
        try{
        	cacheFile.createNewFile();
        }catch(IOException e){
        	Log.w(prefix,"Could not create file");
        	e.printStackTrace();
        }
        
        //final TextView date = (TextView) findViewById(R.id.weather_date);
        date = (TextView) findViewById(R.id.weather_date);
        setContentView(R.layout.main);
        Button refresh = (Button) findViewById(R.id.refresh_button);
	    refresh.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				lastRefresh=SystemClock.elapsedRealtime();
				getWeather(cacheFile,date,"http://foehn.colorado.edu/weather/atoc1/");
			}
	    	
	    });
    }
    /** Called when the activity is started **/
    @Override
    public void onStart(){
    	super.onStart();
    	if(date!=null){
    		return;
    	}
    	long currentTime=SystemClock.elapsedRealtime();
    	File cacheFile = new File(getCacheDir(),"bwData.dat");
    	if (lastRefresh==0){
    		lastRefresh=currentTime;
    		getWeather(cacheFile,date,"http://foehn.colorado.edu/weather/atoc1/");
    		Log.i("BOULDERWEATHER","lastRefresh=0, just opened");
    	} else if((currentTime-lastRefresh) > REFRESH_TIMEOUT){
    		Log.i("BOULDERWEATHER","currentTime="+currentTime+" lastRefresh="+lastRefresh+" difference="+(currentTime-lastRefresh));
    		lastRefresh=currentTime;
    		getWeather(cacheFile,date,"http://foehn.colorado.edu/weather/atoc1/");
    	} //else don't refresh again, but do the screen draw
    	else {
    		Log.i("BOULDERWEATHER","else statement hit!");
    		Map<String,List<String>> map = new LinkedHashMap<String,List<String>>();
    		//build a map from a cache file!
    		String line = null;
    		String[] members = null;
    		String key = null;
    		List<String> values = null;
    		if(cacheFile.exists()){
    			Log.i(prefix,"cache file found");
    			Log.i(prefix,"cache file is: "+cacheFile.length()+" bytes");
    			try{
    				BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
    				while ((line = reader.readLine()) != null){
    					Log.i(prefix,"Entered read loop, file must be non-null");
    					values = new ArrayList<String>();
	    				members = line.split(",");
	    				key = members[0];
	    				for(String member : members){
	    					Log.i(prefix,"(key="+key+"), value="+member+")");
	    					if (! member.equals(key)){
	    						values.add(member);
	    					}
	    				}
	    				map.put(key, values);
    				}
    			}catch(IOException e){
    				Log.w(prefix,"Reading cache file failed!");
    				e.printStackTrace();
    				return; //fuck it, I quit
    			}
    		}else{
    			Log.w(prefix,"Cache file does not exist yet!");
    		}
    		drawScreen(map);
    	}
    	
    }
    private boolean getWeather(File cacheFile,TextView date, String url){
    	Map<String,List<String>> map = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		try{
			HttpResponse response = client.execute(request);
			map = WeatherHelper.request(response);
		}catch(Exception ex){
			ex.printStackTrace();
			map = null;
		}
		//store the map in our cache file
		if(!cacheFile.exists()){
			try {
				cacheFile.createNewFile();
				Log.i(prefix,"cacheFile created");
			}
			catch (IOException e){
				Log.w(prefix,"Warning, cache file does not exist and could not be created");
				e.printStackTrace();
				return drawScreen(map); //cache file is not a requirement
			}
		}
		try {//pretty much anything in here can throw an IOException
			BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile));
			Log.i(prefix,"beginning write");
			for(String key : map.keySet()){
				Log.i(prefix,"writing to file key: "+key);
				writer.write(key);
				for(String value : map.get(key)){
					Log.i(prefix,"writing to file value: "+value);
					writer.write(","+value); //hope this works!
				}
				writer.newLine();
			}
			writer.close();
			Log.i(prefix, "Looks like file write worked!");
			Log.i(prefix,"Cache file is: "+cacheFile.length()+" bytes");
		} catch (IOException e) {
			Log.w(prefix,"Warning, could not write to cacheFile");
			e.printStackTrace();
			return drawScreen(map); //oh well!
		}
		return drawScreen(map);
    }
    private boolean drawScreen(Map<String,List<String>> map){
        //final TextView 
        final TextView date = (TextView) findViewById(R.id.weather_date);
		if(map==null){
			Log.w(prefix,"Warning, Map was not returned properly!");
			date.setText("No results!");
		}else{
			ListView lv= (ListView)findViewById(R.id.listview);
	        String[] from = new String[] { "Type", "Current", "Minimum", "Maximum", "Average" };
	        int[] to = new int[] { R.id.Value, R.id.Current_val, R.id.Minimum_val, R.id.Maximum_val, R.id.Average_val };
	        List<HashMap<String, String>> filledMaps = new ArrayList<HashMap<String, String>>();
	        String [] ps = getResources().getStringArray(R.array.prepStrings);
	        //prep the list
	        for(String s: map.keySet()){
	        	Log.i(prefix,"Stat: "+s);
	        	Log.i(prefix, "Value: "+map.get(s));
	        	if(map.get(s).size()==4){
	        		HashMap<String, String> prepMap = new HashMap<String, String>();
		        	prepMap.put("Type", s);
		        	List<String> rowString = map.get(s);
		        	for(int i=0;i<4;i++){
			        	prepMap.put(ps[i],rowString.get(i));
		        	}
		        	filledMaps.add(prepMap);	
	        	}
	        }
	        SimpleAdapter adapter = new SimpleAdapter(this, filledMaps, R.layout.results, from, to);
	        lv.setAdapter(adapter);
			date.setText("Date: "+map.get("Date").get(0));
		}
    	return true;
    }
}