package com.dgutkin.pairstool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class InputsSimpleFragment extends Fragment {
	
	View root_view;
	
	Button first_ticker_button;
	Button second_ticker_button;
	Spinner lookback_spinner;
	Button save_button;
	Button run_button;
	
	String first_ticker = "Ticker 1";
	String second_ticker = "Ticker 2";
	String first_security_name = "";
	String second_security_name = "";
	String start_date = "Start Date";
	String end_date = "End Date";
	
	static String FILENAME = "PAIRS";
	
	public interface RunListener {
		
		public void onRunButtonClick(boolean simple_date_mode, String first_ticker, String second_ticker, String start_date, String end_date);
		
	}
	
	RunListener run_listener;
	
	public interface SwitchDateModeListener {
		public void onSwitchButtonClick(boolean simple_date_mode, String first_ticker, String second_ticker, 
				String first_security_name, String second_security_name, String start_date, String end_date);
	}
	
	SwitchDateModeListener switch_listener;
	
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		try {run_listener = (RunListener) activity;}
		catch (ClassCastException e) {throw new ClassCastException(activity.toString() + "must implement RunListener");}
		try {switch_listener = (SwitchDateModeListener) activity;}
		catch (ClassCastException e) {throw new ClassCastException(activity.toString() + "must implement SwitchDateModeListener");}
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
		if (item.getItemId() == R.id.action_about) {
			
			AboutDialogFragment about_dialog = new AboutDialogFragment();
    		about_dialog.show(getFragmentManager(), "about");
    		
		} else if (item.getItemId() == R.id.action_saved_pairs) {
			
			Intent intent = new Intent(getActivity(), SavedPairsActivity.class);
			intent.putExtra("FIRST_TICKER", first_ticker);
			intent.putExtra("SECOND_TICKER", second_ticker);
			intent.putExtra("FIRST_SECURITY_NAME", first_security_name);
			intent.putExtra("SECOND_SECURITY_NAME", second_security_name);
			intent.putExtra("START_DATE", start_date);
			intent.putExtra("END_DATE", end_date);
			intent.putExtra("SIMPLE_DATE_RANGE", true);
			startActivity(intent);
			
		} else if (item.getItemId() == R.id.action_switch_date_mode) {
			switch_listener.onSwitchButtonClick(true, first_ticker, second_ticker, first_security_name, 
					second_security_name, start_date, end_date);
		}
		
		return super.onOptionsItemSelected(item);
    	
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		getActivity().getMenuInflater().inflate(R.menu.inputs, menu);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
		
		if (root_view == null) {
			root_view = inflater.inflate(R.layout.fragment_inputs_simple, container, false);
		}
		
		first_ticker_button = (Button) root_view.findViewById(R.id.first_ticker_simple_button);
    	second_ticker_button = (Button) root_view.findViewById(R.id.second_ticker_simple_button);
    	lookback_spinner = (Spinner) root_view.findViewById(R.id.lookback_spinner);
        save_button = (Button) root_view.findViewById(R.id.save_simple_button);
        run_button = (Button) root_view.findViewById(R.id.run_simple_button);
        
        
        first_ticker_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(getActivity(),TickerActivity.class);
				int which_button = 1;
				intent.putExtra("WHICH_BUTTON", which_button);
				intent.putExtra("FIRST_TICKER", first_ticker);
				intent.putExtra("SECOND_TICKER", second_ticker);
				intent.putExtra("FIRST_SECURITY_NAME", first_security_name);
				intent.putExtra("SECOND_SECURITY_NAME", second_security_name);
				intent.putExtra("START_DATE", start_date);
				intent.putExtra("END_DATE", end_date);
				intent.putExtra("SIMPLE_DATE_RANGE", true);
				startActivity(intent);
				
			}
		});
        
        first_ticker_button.setText(first_ticker);
        
        second_ticker_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				Intent intent = new Intent(getActivity(),TickerActivity.class);
				int which_button = 2;
				intent.putExtra("WHICH_BUTTON", which_button);
				intent.putExtra("FIRST_TICKER", first_ticker);
				intent.putExtra("SECOND_TICKER", second_ticker);
				intent.putExtra("FIRST_SECURITY_NAME", first_security_name);
				intent.putExtra("SECOND_SECURITY_NAME", second_security_name);
				intent.putExtra("START_DATE", start_date);
				intent.putExtra("END_DATE", end_date);
				intent.putExtra("SIMPLE_DATE_RANGE", true);
				startActivity(intent);
				
			}
		});
        
        second_ticker_button.setText(second_ticker);
        
        final SimpleDateFormat date_format = new SimpleDateFormat("dd - MMM - yyyy", Locale.CANADA);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		end_date = date_format.format(today.getTime()); 
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), 
				R.array.lookback_periods, R.layout.spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		lookback_spinner.setAdapter(adapter);
		lookback_spinner.setSelection(3);
		lookback_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Calendar calendar = Calendar.getInstance();
				switch (pos) {
				case 0:
					calendar.add(Calendar.MONTH, -1);
					break;
				case 1:
					calendar.add(Calendar.MONTH, -3);
					break;
				case 2:
					calendar.add(Calendar.MONTH, -6);
					break;
				case 3:
					calendar.add(Calendar.YEAR, -1);
					break;
				case 4:
					calendar.add(Calendar.YEAR, -3);
					break;
				}
				start_date = date_format.format(calendar.getTime());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
		});
        
        run_button.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		
        		// go back to MainActivity and call calculate method
        		run_listener.onRunButtonClick(true, first_ticker, second_ticker, start_date, end_date);
        		
        	}
        	
        });
        
        save_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (first_ticker.equals("Ticker 1") || second_ticker.equals("Ticker 2")) {
					Toast no_pair_to_save_toast = Toast.makeText(getActivity(), 
							R.string.no_pair_to_save, Toast.LENGTH_SHORT);
					no_pair_to_save_toast.show();
				} else {
					String pair_list = "";
					String[] file_list = getActivity().fileList();
					if (file_list.length != 0) {
						try {
							FileInputStream fis = getActivity().openFileInput(FILENAME);
							byte[] bytes = new  byte[fis.available()];
							if (bytes.length == 0) { // avoid infinte loop if can't read file
								getActivity().deleteFile(FILENAME);
								return;
							}
							String content = "";
							while (fis.read(bytes) != -1) {
								content = new String(bytes);
							}
							pair_list = content;
						} catch(IOException e) {return;}
					}
					String pair_item = first_security_name + " / " + second_security_name;
					if (pair_list.equals("")) { // nothing in list
						pair_list = pair_list + pair_item;
						Toast pair_saved_toast = Toast.makeText(getActivity(), 
								R.string.pair_saved, Toast.LENGTH_SHORT);
						pair_saved_toast.show();
					} else if (pair_list.indexOf(",,") == -1 && !pair_list.equals(pair_item)) { // one item in list
						pair_list = pair_list + ",," + pair_item;
						Toast pair_saved_toast = Toast.makeText(getActivity(), 
								R.string.pair_saved, Toast.LENGTH_SHORT);
						pair_saved_toast.show();
					} else { // more than one item in list
						String [] pair_list_array = pair_list.split(",,");
						boolean duplicate = false;
						for (int i = 0; i < pair_list_array.length; i++) {
							if (pair_list_array[i].equals(pair_item)) {duplicate = true;}
						}
						if (!duplicate) {
							if (pair_list_array.length > 24) {
								Toast limit_reached_toast = Toast.makeText(getActivity(), 
										R.string.limit_reached, Toast.LENGTH_SHORT);
								limit_reached_toast.show();
							} else {
								pair_list = pair_list + ",," + pair_item;
								Toast pair_saved_toast = Toast.makeText(getActivity(), 
										R.string.pair_saved, Toast.LENGTH_SHORT);
								pair_saved_toast.show();
							}
						} else {
							Toast pair_already_saved_toast = Toast.makeText(getActivity(), 
									R.string.pair_already_saved, Toast.LENGTH_SHORT);
							pair_already_saved_toast.show();
						}
					}
					
					try {
						FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
						fos.write(pair_list.getBytes());
						fos.close();
					} catch (IOException e) {return;}
					
				}
				
			}
		});
		
		return root_view;
	
	}
	
}
