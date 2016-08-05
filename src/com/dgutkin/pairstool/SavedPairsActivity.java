package com.dgutkin.pairstool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SavedPairsActivity extends Activity {
	
	static String FILENAME = "PAIRS";
	String first_ticker = "Ticker 1";
	String second_ticker = "Ticker 2";
	String first_security_name = "";
	String second_security_name = "";
	String start_date = "Start Date";
	String end_date = "End Date";
	String list_item = "";
	boolean simple_date_range = true;
	
	ListView pair_list_view;
	ArrayList<String> pair_list = new ArrayList<String>();
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_saved_pairs);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		final Intent intent = getIntent();
		if (intent.getExtras() != null) { // intent from InputsFragment
			first_ticker = intent.getStringExtra("FIRST_TICKER");
			second_ticker = intent.getStringExtra("SECOND_TICKER");
			first_security_name = intent.getStringExtra("FIRST_SECURITY_NAME");
			second_security_name = intent.getStringExtra("SECOND_SECURITY_NAME");
			start_date = intent.getStringExtra("START_DATE");
			end_date = intent.getStringExtra("END_DATE");
			simple_date_range = intent.getBooleanExtra("SIMPLE_DATE_RANGE", true);
		}
		
		pair_list_view = (ListView) findViewById(R.id.saved_pairs_list);
		pair_list = new ArrayList<String>();
		//getApplicationContext().deleteFile(FILENAME); // reset file for debugging
		String[] file_list = getApplicationContext().fileList();
		if (file_list.length == 0) {} // no pairs in file
		else {
			try {
				FileInputStream fis = openFileInput(FILENAME);
				byte[] bytes = new byte[fis.available()];
				String content = "";
				if (bytes.length == 0) { // avoid infinte loop if can't read file
					getApplicationContext().deleteFile(FILENAME);
					return;
				}
				while (fis.read(bytes) != -1) {
					content = new String(bytes);
				}
				if (content.indexOf(",,") != -1) {
					String[] pair_list_array = content.split(",,");
					for (int i = 0; i < pair_list_array.length; i++) {
						pair_list.add(pair_list_array[i]);
					}
				} else { // only one pair in file
					pair_list.add(content);
				}
			} catch (IOException e) {return;}
		}
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, pair_list);
		pair_list_view.setAdapter(adapter);
		
		pair_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				list_item = (String) parent.getItemAtPosition(position);
				
			}
		
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.saved_pairs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_load) {
			loadPair();
			return true;
		} else if (id == R.id.action_delete) {
			deletePair();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void loadPair() {
		
		if (!list_item.equals("")) {
			first_security_name = list_item.substring(0, list_item.indexOf("/")-1);
			first_ticker = first_security_name.substring(0, first_security_name.indexOf(":")-1);
			second_security_name = list_item.substring(list_item.indexOf("/")+2, list_item.length());
			second_ticker = second_security_name.substring(0, second_security_name.indexOf(":")-1);
			Intent intent = new Intent(SavedPairsActivity.this, MainActivity.class);
			intent.putExtra("FIRST_TICKER", first_ticker);
			intent.putExtra("SECOND_TICKER", second_ticker);
			intent.putExtra("FIRST_SECURITY_NAME", first_security_name);
			intent.putExtra("SECOND_SECURITY_NAME", second_security_name);
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat date_out = new SimpleDateFormat("dd - MMM - yyyy",Locale.CANADA);
			end_date = date_out.format(calendar.getTime());
			calendar.add(Calendar.YEAR, -1);
			start_date = date_out.format(calendar.getTime());
			intent.putExtra("START_DATE", start_date);
			intent.putExtra("END_DATE", end_date);
			intent.putExtra("SIMPLE_DATE_RANGE", simple_date_range);
			Log.v("simple date range", String.valueOf(simple_date_range));
			startActivity(intent);
		}
		
	}
	
	public void deletePair() { // deleting first pair corrupts file
		
		try {
			if (pair_list.size() == 1) {
				pair_list.remove(list_item);
				getApplicationContext().deleteFile(FILENAME);
				list_item = "";
				adapter.notifyDataSetChanged();
			} else if (pair_list.size() > 1) {
				pair_list.remove(list_item);
				String pair_list_string = "";
				for (int i = 0; i < pair_list.size(); i++) {
					if (i == 0) {pair_list_string = pair_list_string + pair_list.get(i);}
					else {pair_list_string = pair_list_string + ",," + pair_list.get(i);}
				}
				FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
				fos.write(pair_list_string.getBytes());
				fos.close();
				list_item = "";
				pair_list_view.clearChoices();
				adapter.notifyDataSetChanged();
			}
		} catch (IOException e) {return;}
		
	}

}
