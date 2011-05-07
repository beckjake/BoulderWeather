package com.emanlaerton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class BoulderWeather extends Activity {
	private static final String prefix = "BoulderWeather";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    Button refresh = (Button) findViewById(R.id.refresh_button);
    //final TextView 
    final TextView date = (TextView) findViewById(R.id.weather_date);
	    refresh.setOnClickListener(new View.OnClickListener(){
	
			public void onClick(View v) {
				getWeather(date,"http://foehn.colorado.edu/weather/atoc1/");
				//getWeather(date,"testing");
			}
	    	
	    });
    }
    public boolean getWeather(TextView date, String url){
		final int CURRENT = 0;
		final int MIN = 1;
		final int MAX = 2;
		final int AVERAGE = 3;
		Map<String,List<String>> map = null;
    	if(url == "testing"){
    		Log.i(prefix,"Testing mode");
	    	try{
	    		File f = new File(Environment.getExternalStorageDirectory()+"/atoc1.html");
	    		if(!f.exists())
	    			Log.w(prefix,"File not found");
	    		FileInputStream fileIS = new FileInputStream(f);
	    		BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS));
	            StringBuilder str = new StringBuilder();
	            String line = null;
	            while((line = buf.readLine()) != null){
	                str.append(line + "\n");
	            }
	            fileIS.close();
	            String result = str.toString();
	    		map = WeatherHelper.parseSavedAtoc(result);
	    	}catch (Exception e){
	    		date.setText("Failed to get data:"+e.toString());
	    		return false;
	    	}
    	}else{
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
			try{
				HttpResponse response = client.execute(request);
				map = WeatherHelper.request(response);
			}catch(Exception ex){
				date.setText("Failed to get data:"+ex.toString());
				return false;
			}
    	}
		//TODO: replace this and "delims" in WeatherHelper.java with a proper thing in strings.xml
		if(map==null){
			Log.w(prefix,"Warning, Map was not returned properly!");
			date.setText("No results!");
		}else{
			ListView lv= (ListView)findViewById(R.id.listview);
	        String[] from = new String[] { "Type", "Current", "Minimum", "Maximum", "Average" };
	        int[] to = new int[] { R.id.Value, R.id.Current_val, R.id.Minimum_val, R.id.Maximum_val, R.id.Average_val };
	        List<HashMap<String, String>> filledMaps = new ArrayList<HashMap<String, String>>();
	        //prep the list
	        for(String s: map.keySet()){
	        	Log.i(prefix,"Stat: "+s);
	        	Log.i(prefix, "Value: "+map.get(s));
	        	if(map.get(s).size()==4){
	        		HashMap<String, String> prepMap = new HashMap<String, String>();
		        	prepMap.put("Type", s);
		        	List<String> rowString = map.get(s);
		        	prepMap.put("Current",rowString.get(CURRENT));
		        	prepMap.put("Minimum", rowString.get(MIN));
		        	prepMap.put("Maximum", rowString.get(MAX));
		        	prepMap.put("Average", rowString.get(AVERAGE));
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