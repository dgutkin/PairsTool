package com.dgutkin.pairstool;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.dgutkin.pairstool.StockCSVReader.Quote;

import android.content.Context;
import android.os.AsyncTask;


public class StockDownloadTask extends AsyncTask<String, Void, List<Quote>> {
	
Context mcontext;
	
	public StockDownloadTask(Context context) {
		
		mcontext = context;
		
	}
	
	@Override
	protected List<Quote> doInBackground(String...urls) {
		
		try {return loadYahooFinanceCSV(urls[0]);}
		catch (IOException e) {return null;} 
		
	}
	
	private List<Quote> loadYahooFinanceCSV(String urlString) throws IOException {
		
		InputStream stream = null;
		StockCSVReader csv_reader = new StockCSVReader();
		QuoteJSONReader json_reader = new QuoteJSONReader();
		List<Quote> quote_list = new ArrayList<Quote>();
		
		try {
			
			stream = downloadUrl(urlString);
			//separate method handles url to http connection and download
			if (urlString.contains("format=json")) {
				quote_list = json_reader.parse(stream);
			} else {
				quote_list = csv_reader.parse(stream);
			}
			
		} finally {
			
			if (stream != null) {stream.close();}
			
		}
		
		return quote_list;
		
	}
	
	private InputStream downloadUrl(String urlString) throws IOException {
		
		URL url = new URL(urlString);
		HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
		httpconn.setReadTimeout(60000);
		httpconn.setConnectTimeout(80000);
		httpconn.setRequestMethod("GET");
		httpconn.setDoInput(true);
		httpconn.connect(); //manage wifi sign-on case
		
		InputStream instream = new BufferedInputStream(httpconn.getInputStream());
		
		return instream;
		
	}
	
	
	@Override
	protected void onPostExecute(List<Quote> result) {
		
		// nothing
		
	}

}
