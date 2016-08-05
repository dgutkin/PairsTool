package com.dgutkin.pairstool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

public class TickerDownloadTask extends AsyncTask<String, Void, List<String>> {
	
	@Override
	protected List<String> doInBackground(String...urls) {
		
		try {
			
			return loadYahooFinanceTickerSuggestions(urls[0]);
			
		} catch (IOException e) {
			
			return null;
			
		}
		
	}
	
	private List<String> loadYahooFinanceTickerSuggestions(String url_string) throws IOException {
		
		DefaultHttpClient http_client = new DefaultHttpClient(new BasicHttpParams()); 
		HttpPost http_post = new HttpPost(url_string); 
		http_post.setHeader("Content-type", "application/json"); 
		InputStream input_stream = null; 
		String json_string = null;
		List<String> ticker_list = new ArrayList<String>();
		
		try {
			
    	   HttpResponse response = http_client.execute(http_post);         
    	   HttpEntity entity = response.getEntity(); 
    	   input_stream = entity.getContent(); 
    	   BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream, "UTF-8"), 8); 
    	   StringBuilder string_builder = new StringBuilder();
    	   String line = null;
    	   while ((line = reader.readLine()) != null) {
    		   
    		   string_builder.append(line + "\n");
    		   
    	   }
    	   
    	   json_string = string_builder.toString();
    	   
       } catch (Exception e) {
    	   
    	   //e.printStackTrace();
    	   return null;
    	   
       } finally {
    	  
    	   try { if (input_stream != null) input_stream.close(); } catch (Exception e) {} 

       }
       
       JSONObject json_object;

       try {
    	   
    	   json_string = json_string.substring(39, json_string.length() - 1);
    	   json_object = new JSONObject(json_string);
    	   JSONObject resultset = json_object.getJSONObject("ResultSet");
    	   JSONArray result = resultset.getJSONArray("Result");
    	   
    	   for (int i = 0; i < result.length(); i++) {
    		   
    		   JSONObject stock = result.getJSONObject(i);
    		   String ticker = stock.getString("symbol");
    		   String name = StringEscapeUtils.unescapeXml(stock.getString("name"));
    		   String exchange = stock.getString("exchDisp");
    		   ticker_list.add(i, ticker + " : " +  name + " (" + exchange + ")");
    		   
    	   }
    	   
       } catch (JSONException e) {
    	   
    	   //e.printStackTrace();
    	   return null;
    	   
       }
       
       return ticker_list;
		
	}

}
