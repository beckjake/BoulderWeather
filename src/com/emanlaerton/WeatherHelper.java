package com.emanlaerton;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
//import android.util.Log;
/*
 * This function scrapes the page it is passed (sure hope it's the atoc1 page) and converts it into a WeatherMap.
 */
public class WeatherHelper {
	//private final static String prefix = "WeatherHelper";
	
	public static WeatherMap read(HttpResponse response){
		return parseAtoc(request(response));
	}
	
    private static String request(HttpResponse response){
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
        return result;
	}
    
    //Ugh.
    private static WeatherMap parseAtoc(String pageHTML){
    	String date="";
    	String labelLine = "<tr bgcolor=#> <th align=left>"; 
    	String currentLine = "</th> <td align=center>";
    	String minLine = "</td> <td align=right>";
    	String maxLine = "</td> <td align=right>";
    	String avgLine = "</td> <td align=center>";
    	String endLine = "</td> </tr>";
    	String weirdLine = "</td> <td>"; //this handles problem lines
    	String delims[] = {currentLine,minLine,maxLine,avgLine,endLine};
    	String Label;
    	String values[];
    	WeatherMap wm = null;
    	try {
    		int startCut = pageHTML.indexOf("<table border");
    		int endCut = pageHTML.indexOf("</table>");
    		//cut to table
    		pageHTML = pageHTML.substring(startCut, endCut);
    		
    		//retrieve the date
    		startCut = pageHTML.indexOf("<tr> <th align=center>");
    		endCut = pageHTML.indexOf("</th>");
    		date = pageHTML.substring(startCut,endCut).replaceFirst("<tr> <th align=center>", "").trim();
    		
    		//clean page of annoying differences in table
    		pageHTML = pageHTML.replaceAll("ffff[48][48]", "");
    		//fix the stupid <sup></sup>
    		pageHTML = pageHTML.replaceAll("m<sup>2</sup>","m^2");
    		
    		//cut to next line
    		startCut = pageHTML.indexOf(labelLine);
    		pageHTML = pageHTML.substring(startCut+labelLine.length());
    		
    		wm = new WeatherMap(date);
    		
    		//we want 9 rows
    		for(int i=0;i<9;i++){
    			values = new String[4];
    			Integer loc=0;
    			//endCut is the character at the start of the first instance of "</th> <td align=center>"
    			endCut = pageHTML.indexOf(delims[0]);
    			//Label is from the start of the the align left statement to the end cut - the label!
    			Label = pageHTML.substring(0, endCut).trim();
    			pageHTML = pageHTML.substring(endCut+delims[0].length());
    			//delims.length=4, or so we hope (cur, min, max, avg)
    			for(int j=0;j<(delims.length-1);j++){
    				endCut = pageHTML.indexOf(delims[j+1]);
    				if(endCut==-1){ //uh-oh, it wasn't there... was it a non-standard row with an extra endline?
    					endCut = pageHTML.indexOf(weirdLine);
    					if(endCut==-1){ //give up and don't do this row any more, and don't add anything to the map
    						continue;
    					} else { //ok, what we've got to the map
    						break;
    					}
    				}
    				values[loc++] = pageHTML.substring(0,endCut).replaceAll("[ \t]",""); 
    				pageHTML = pageHTML.substring(endCut+delims[j+1].length());
    			}
        		startCut = pageHTML.indexOf(labelLine);
        		pageHTML = pageHTML.substring(startCut+labelLine.length());
    			wm.insertValues(Label, values);
    		}
    		
		} catch (Exception e) {
			pageHTML = "Error, things went wrong somewhere in parsing";
			e.printStackTrace();
		}
		////Log.i(prefix,"Result: "+pageHTML);
    	return wm;
    }
} 
