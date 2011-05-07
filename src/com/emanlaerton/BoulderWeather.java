package com.emanlaerton;

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
        //final TextView 
        final TextView date = (TextView) findViewById(R.id.weather_date);
        setContentView(R.layout.main);
        Button refresh = (Button) findViewById(R.id.refresh_button);
	    refresh.setOnClickListener(new View.OnClickListener(){
	
			public void onClick(View v) {
				getWeather(date,"http://foehn.colorado.edu/weather/atoc1/");
			}
	    	
	    });
    }
    private boolean getWeather(TextView date, String url){
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