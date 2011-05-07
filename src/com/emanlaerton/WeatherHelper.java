package com.emanlaerton;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import android.util.Log;

public class WeatherHelper {
	private final static String prefix = "WeatherHelper";
    public static Map<String,List<String>> request(HttpResponse response){
        String result = "";
        try{
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        }catch(Exception ex){
        	result = "Error";
        }
        return parseAtoc(result);
        //return result;
	}
    public static Map<String,List<String>> parseSavedAtoc(String pageHTML){
    	String date="";
    	String labelLine = "<tr bgcolor=\"#\"> <th align=left>"; 
    	String currentLine = "</th> <td align=center>";
    	String minLine = "</td> <td align=right>";
    	String maxLine = "</td> <td align=right>";
    	String avgLine = "</td> <td align=center>";
    	String endLine = "</td> </tr>";
    	String delims[] = {currentLine,minLine,maxLine,avgLine,endLine};
    	String Label;
    	List<String> values = new ArrayList<String>();
    	Map<String,List<String>> map = new LinkedHashMap<String,List<String>>();
    	Log.i(prefix,"pageHTML size: "+pageHTML.length());
    	try {
    		//fix quote bug
    		pageHTML = pageHTML.replaceAll("\"left\"", "left");
    		pageHTML = pageHTML.replaceAll("\"right\"", "right");
    		pageHTML = pageHTML.replaceAll("\"center\"", "center");
    		
    		int startCut = pageHTML.indexOf("<table frame");
    		int endCut = pageHTML.indexOf("</table>");
    		//cut to table
    		pageHTML = pageHTML.substring(startCut, endCut);
    		
    		//retrieve the date
    		startCut = pageHTML.indexOf("<tr> <th align=center>");
    		endCut = pageHTML.indexOf("</th>");
    		date = pageHTML.substring(startCut,endCut).replaceFirst("<tr> <th align=center>", "").trim();
    		Log.i(prefix,"Date: "+date);
    		
    		//clean page of annoying differences in table
    		pageHTML = pageHTML.replaceAll("ffff[48][48]", "");
    		//fix the stupid <sup></sup>
    		pageHTML = pageHTML.replaceAll("m<sup>2</sup>","m^2");
    		
    		
    		//cut to next line
    		startCut = pageHTML.indexOf(labelLine);
    		pageHTML = pageHTML.substring(startCut+labelLine.length());
    		Log.i(prefix,"Label line is: "+labelLine.length());
    		values.add(date);
    		map.put("Date", values);
    		Log.i(prefix,"Date: "+date);
    		
    		for(int i=0;i<7;i++){
    			values = new ArrayList<String>();
    			endCut = pageHTML.indexOf(delims[0]);
    			Label = pageHTML.substring(0, endCut).trim();
    			pageHTML = pageHTML.substring(endCut+delims[0].length());
    			for(int j=0;j<(delims.length-1);j++){
    				endCut = pageHTML.indexOf(delims[j+1]);
    				//String addval = pageHTML.substring(0,endCut).trim();
    				String addval = pageHTML.substring(0,endCut).replaceAll("[ \t]","");
    				values.add(addval);
    				Log.i(prefix,addval);
    				pageHTML = pageHTML.substring(endCut+delims[j+1].length());
    			}
        		startCut = pageHTML.indexOf(labelLine);
        		pageHTML = pageHTML.substring(startCut+labelLine.length());
    			if(map.containsValue(values))
    				Log.w(prefix,"Already in map, you done goofed");
    			map.put(Label, values);
    		}
    		
		} catch (Exception e) {
			pageHTML = "Error, things went wrong somewhere in parsing";
			e.printStackTrace();
		}
		Log.i(prefix,"Result: "+pageHTML);
    	return map;
    }
    public static Map<String,List<String>> parseAtoc(String pageHTML){
    	String date="";
    	String labelLine = "<tr bgcolor=#> <th align=left>"; 
    	String currentLine = "</th> <td align=center>";
    	String minLine = "</td> <td align=right>";
    	String maxLine = "</td> <td align=right>";
    	String avgLine = "</td> <td align=center>";
    	String endLine = "</td> </tr>";
    	String delims[] = {currentLine,minLine,maxLine,avgLine,endLine};
    	String Label;
    	List<String> values = new ArrayList<String>();
    	Map<String,List<String>> map = new LinkedHashMap<String,List<String>>();
    	Log.i(prefix,"pageHTML size: "+pageHTML.length());
    	try {
    		int startCut = pageHTML.indexOf("<table border");
    		int endCut = pageHTML.indexOf("</table>");
    		//cut to table
    		pageHTML = pageHTML.substring(startCut, endCut);
    		
    		//retrieve the date
    		startCut = pageHTML.indexOf("<tr> <th align=center>");
    		endCut = pageHTML.indexOf("</th>");
    		date = pageHTML.substring(startCut,endCut).replaceFirst("<tr> <th align=center>", "").trim();
    		Log.i(prefix,"Date: "+date);
    		
    		//clean page of annoying differences in table
    		pageHTML = pageHTML.replaceAll("ffff[48][48]", "");
    		//fix the stupid <sup></sup>
    		pageHTML = pageHTML.replaceAll("m<sup>2</sup>","m^2");
    		
    		//cut to next line
    		startCut = pageHTML.indexOf(labelLine);
    		pageHTML = pageHTML.substring(startCut+labelLine.length());
    		
    		values.add(date);
    		map.put("Date", values);
    		
    		
    		for(int i=0;i<7;i++){
    			values = new ArrayList<String>();
    			endCut = pageHTML.indexOf(delims[0]);
    			Label = pageHTML.substring(0, endCut).trim();
    			pageHTML = pageHTML.substring(endCut+delims[0].length());
    			for(int j=0;j<(delims.length-1);j++){
    				endCut = pageHTML.indexOf(delims[j+1]);
    				String addval = pageHTML.substring(0,endCut).replaceAll("[ \t]",""); 
    				values.add(addval);
    				Log.i(prefix,addval);
    				pageHTML = pageHTML.substring(endCut+delims[j+1].length());
    			}
        		startCut = pageHTML.indexOf(labelLine);
        		pageHTML = pageHTML.substring(startCut+labelLine.length());
    			if(map.containsValue(values))
    				Log.w(prefix,"Already in map, you done goofed");
    			map.put(Label, values);
    		}
    		
		} catch (Exception e) {
			pageHTML = "Error, things went wrong somewhere in parsing";
			e.printStackTrace();
		}
		//Log.i(prefix,"Result: "+pageHTML);
    	return map;
    }
} 
