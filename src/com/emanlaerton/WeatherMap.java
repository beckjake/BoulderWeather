package com.emanlaerton;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

/*
 * A class to abstract the Map<String,List<String>> currently used and turn it into something a bit more safe
 */
public class WeatherMap {
	private static final String prefix = "WeatherMap";
	private String date;
	private Map<String,List<String>> map; //key = name of the type of weather
	
	WeatherMap(String date){
		this.date = date;
		this.map = new LinkedHashMap<String,List<String>>(); //linked to preserve ordering, I think. But it doesn't really matter as long as it's ordered.
	}  
	
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getDate(){
		if (date==null){
			return new String("");//empty string is better than null...
		}
		return this.date;
	}
	
	public Set<String> keys(){
		if(map==null){ //should never happen
			return null;
		}
		return this.map.keySet();
	}
	
	//add a list of values for a weather attribute
	public void insertValues(String key, List<String> value){
		
		//go through it and remove all the empty strings
		for(String val : value){
			if(val.replaceAll("[ \t]","").length()==0){
				value.remove(val);
			}
		}
		
		//now put our values in the list
		this.map.put(key, value);
	}
	
	//get the list of values for a weather attribute
	public List<String> getValues(String key){
		List<String> values = this.map.get(key);
		if(values!=null){
			return values;
		} else {
			Log.w(prefix,"Warning, no values");
			return null;
		}
	}
	
	public boolean nullDate(){
		return (this.date==null);
	}
	
	//SHOULD NEVER HAPPEN
	public boolean nullMap(){
		return (this.map==null);
	}
	
	
	
}
