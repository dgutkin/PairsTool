package com.dgutkin.pairstool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dgutkin.pairstool.StockCSVReader.Quote;

public class QuoteJSONReader {

	public List<Quote> parse(InputStream input_stream) throws IOException {
		
		
		List<Quote> quote_list = new ArrayList<Quote>();
		String json_string = null;
		
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream, "UTF-8"), 8);
			StringBuilder string_builder = new StringBuilder();
	 	   	String line = null;
	 	   	
	 	   	while ((line = reader.readLine()) != null) {
	 		   
	 	   		string_builder.append(line + "\n");
	 		   
	 	   	}
	 	   
	 	   	json_string = string_builder.toString();
		} catch (Exception e) {
			return null;
		} finally {
			try {if (input_stream != null) {
				input_stream.close();
				}
			} catch (Exception e) {return null;}
		}
		
		 JSONObject json_object;

	       try {
	    	   
	    	   json_object = new JSONObject(json_string);
	    	   JSONObject list = json_object.getJSONObject("list");
	    	   JSONArray result_set = list.getJSONArray("resources");
	    	   for (int i = 0; i < result_set.length(); i++) {
	    		   
	    		   JSONObject result = result_set.getJSONObject(i);
	    		   JSONObject resource = result.getJSONObject("resource");
	    		   JSONObject json_quote = resource.getJSONObject("fields");
	    		   String date = json_quote.getString("ts");
	    		   String price_string = json_quote.getString("price");
	    		   BigDecimal price;
	    		   try {price = new BigDecimal(price_string);} catch (NumberFormatException e) {return null;}
	    		   quote_list.add(new Quote(date, price));
	    		   
	    	   }
	    	   
	       } catch (JSONException e) {return null;}
 	   	
		return quote_list;
		
	}
	
}
