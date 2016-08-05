package com.dgutkin.pairstool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class TickerActivity extends Activity {
	
	String ticker = "";
	String security_name = "";
	String ticker_other = "";
	String security_name_other = "";
	EditText ticker_input;
	int which_button;
	String start_date;
	String end_date;
	boolean simple_date_range;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticker);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Intent intent = getIntent();
		which_button = intent.getIntExtra("WHICH_BUTTON", 1);
		if (which_button == 1) {
			ticker = intent.getStringExtra("FIRST_TICKER");
			security_name = intent.getStringExtra("FIRST_SECURITY_NAME");
			ticker_other = intent.getStringExtra("SECOND_TICKER");
			security_name_other = intent.getStringExtra("SECOND_SECURITY_NAME");
		} else {
			ticker = intent.getStringExtra("SECOND_TICKER");
			security_name = intent.getStringExtra("SECOND_SECURITY_NAME");
			ticker_other = intent.getStringExtra("FIRST_TICKER");
			security_name_other = intent.getStringExtra("FIRST_SECURITY_NAME");
		}
		start_date = intent.getStringExtra("START_DATE");
		end_date = intent.getStringExtra("END_DATE");
		simple_date_range = intent.getBooleanExtra("SIMPLE_DATE_RANGE", true);
			
		
		EditText ticker_input = (EditText) findViewById(R.id.ticker_input);
		if (!ticker.equals("Ticker 1") && !ticker.equals("Ticker 2")) {ticker_input.setText(ticker);}
		ticker_input.setSelection(ticker_input.getText().length());
		
		final ListView ticker_list = (ListView) findViewById(R.id.ticker_list);
		ticker_list.setTextFilterEnabled(true);
		ArrayList<String> initial = new ArrayList<String>();
		initial.add("---");
		ArrayAdapter<String> ticker_list_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, initial);
		ticker_list.setAdapter(ticker_list_adapter);
		
		ticker_input.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			
			@Override
			public void afterTextChanged(Editable s){
				
				String search_url = "";
				try {
					
					search_url = "http://autoc.finance.yahoo.com/autoc?query=" + 
							URLEncoder.encode(s.toString(), "UTF-8") +
							"&region=US&lang=en-US&callback=YAHOO.Finance.SymbolSuggest.ssCallback";
					
				} catch (UnsupportedEncodingException e) {
					
					Toast dataunavailabletoast = Toast.makeText(getApplicationContext(), 
					R.string.data_unavailable, Toast.LENGTH_SHORT);
					dataunavailabletoast.show();
					return;
					
				}
				
				ConnectivityManager conn_manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        		NetworkInfo network_info = conn_manager.getActiveNetworkInfo();
        		
        		if (network_info == null || !network_info.isConnected()) {
					
					Toast nointernetconnectiontoast = Toast.makeText(getApplicationContext(), 
							R.string.no_internet_connection, Toast.LENGTH_SHORT);
					nointernetconnectiontoast.show();
					return;
					
				}
        		
        		AsyncTask<String, Void, List<String>> tdt = new TickerDownloadTask().execute(search_url);
        		
        		try {
					
					List<String> ticker_suggestions = new ArrayList<String>();
					ticker_suggestions = tdt.get();
					
					if (ticker_suggestions == null) {
						
						Toast nointernetconnectiontoast = Toast.makeText(getApplicationContext(), 
    							R.string.no_internet_connection, Toast.LENGTH_SHORT);
    					nointernetconnectiontoast.show();
    					
    					return;
						
					}
					
					if (ticker_suggestions.size() == 0) {
						
						if (s.toString().equals("")) {ticker_suggestions.add(0, "-");}
						else {ticker_suggestions.add(0, "No Results Found");}
						
					}
					
					ArrayAdapter<String> ticker_list_adapter = new ArrayAdapter<String>(
							TickerActivity.this, android.R.layout.simple_list_item_1, ticker_suggestions);
					ticker_list.setAdapter(ticker_list_adapter);
					ticker_list_adapter.notifyDataSetChanged();
					
				} catch (Exception e) {
					
					Toast dataunavailabletoast = Toast.makeText(getApplicationContext(), 
					R.string.data_unavailable, Toast.LENGTH_SHORT);
					dataunavailabletoast.show();
					
				}
        		
        		ticker_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        			@Override
        			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        				
        				String item = (String) parent.getItemAtPosition(position);
        				if (item.equals("No Results Found") || item.equals("") || item.equals("-")) {return;}
        				ticker = item.substring(0, item.indexOf(":") - 1);
        				if (item.length() <= 35) {
        					security_name = item;
        				} else {
        					security_name = item.substring(0, 35);
        				}
        				
        				Intent intent = new Intent(TickerActivity.this, MainActivity.class);
        				if (which_button == 1) {
        					intent.putExtra("FIRST_TICKER", ticker);
        					intent.putExtra("FIRST_SECURITY_NAME", security_name);
        					intent.putExtra("SECOND_TICKER", ticker_other);
        					intent.putExtra("SECOND_SECURITY_NAME", security_name_other);
        				} else {
        					intent.putExtra("FIRST_TICKER", ticker_other);
        					intent.putExtra("FIRST_SECURITY_NAME", security_name_other);
        					intent.putExtra("SECOND_TICKER", ticker);
        					intent.putExtra("SECOND_SECURITY_NAME", security_name);
        				}
        				intent.putExtra("START_DATE", start_date);
        				intent.putExtra("END_DATE", end_date);
        				intent.putExtra("SIMPLE_DATE_RANGE", simple_date_range);
        				startActivity(intent);
        				
        			}
        			
        		});
			}
			
		});
		
	}


}
