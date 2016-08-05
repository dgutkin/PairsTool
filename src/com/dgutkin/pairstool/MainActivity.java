package com.dgutkin.pairstool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import com.dgutkin.pairstool.StockCSVReader.Quote;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements 
	DateDialogFragment.DateListener, InputsFragment.RunListener, InputsSimpleFragment.RunListener, 
	QuotesFragment.RefreshListener, PairsFragment.RefreshListener, PairsFragment.FlipListener, 
	InputsFragment.SwitchDateModeListener, InputsSimpleFragment.SwitchDateModeListener {
	// logo colour is #12586F
	// stacked tabs colour is #454545
	String first_ticker = "Ticker 1";
	String second_ticker = "Ticker 2";
	String first_security_name = "";
	String second_security_name = "";
	String start_date = "Start Date";
	String end_date = "End Date";
	boolean simple_date_range = true;
	
	InputsFragment inputs;
	InputsSimpleFragment inputs_simple;
	PairsFragment pairs;
	QuotesFragment quotes;
	
	BigDecimal[] first_ticker_prices;
	BigDecimal[] second_ticker_prices;
	double[] first_ticker_returns;
	double[] second_ticker_returns;
	Date[] dates;
	String last_quote_time = "Close";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		inputs = new InputsFragment();
		inputs_simple = new InputsSimpleFragment();
		pairs = new PairsFragment();
		quotes = new QuotesFragment();
		
		FragmentManager input_fragment_manager = getFragmentManager();
		input_fragment_manager.beginTransaction().replace(R.id.container, inputs).commit();
		
		final Intent intent = getIntent();
		if (intent.getExtras() != null) { // intent from TickerActivity or SavedPairsActivity
			first_ticker = intent.getStringExtra("FIRST_TICKER");
			second_ticker = intent.getStringExtra("SECOND_TICKER");
			first_security_name = intent.getStringExtra("FIRST_SECURITY_NAME");
			second_security_name = intent.getStringExtra("SECOND_SECURITY_NAME");
			start_date = intent.getStringExtra("START_DATE");
			end_date = intent.getStringExtra("END_DATE");
			simple_date_range = intent.getBooleanExtra("SIMPLE_DATE_RANGE", true);
		}
		
		// not switching but just setting up the right fragment so use the negate simple_date_range
		onSwitchButtonClick(!simple_date_range, first_ticker, second_ticker, first_security_name,
				second_security_name, start_date, end_date);
		
		final ActionBar action_bar = getActionBar();
		action_bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		ActionBar.TabListener tab_listener = new ActionBar.TabListener() {
			
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				
				if (tab.getText().equals("INPUTS")) {
					if (simple_date_range) {
						ft.replace(R.id.container, inputs_simple);
					} else {
						ft.replace(R.id.container, inputs);
					}
				} else if (tab.getText().equals("QUOTES")) {
					ft.replace(R.id.container, quotes);
				} else {
					ft.replace(R.id.container, pairs);
				}
				
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// do nothing
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// do nothing
			}
			
		};
		
		action_bar.addTab(action_bar.newTab().setText("INPUTS").setTabListener(tab_listener));
		action_bar.addTab(action_bar.newTab().setText("QUOTES").setTabListener(tab_listener));
		action_bar.addTab(action_bar.newTab().setText("ANALYSIS").setTabListener(tab_listener));
		
		//inputs_simple.first_ticker = first_ticker;
		//inputs_simple.second_ticker = second_ticker;
		//inputs_simple.first_security_name = first_security_name;
		//inputs_simple.second_security_name = second_security_name;
		//inputs_simple.start_date = start_date;
		//inputs_simple.end_date = end_date;
		// InputsFragment button texts updated once fragment created
		
	}

	@Override
	public void onStartDateSelected(String date, boolean start) {
		
		if (start) {
			start_date = date;
			inputs.start_date = date;
			Button start_date_button = (Button) findViewById(R.id.start_date_button);
			start_date_button.setText(date);
		} else {
			end_date = date;
			inputs.end_date = date;
			Button end_date_button = (Button) findViewById(R.id.end_date_button);
			end_date_button.setText(date);	
		}
		
	}

	@Override
	public void onRunButtonClick(boolean simple_date_mode, String first_ticker, String second_ticker,
			String start_date, String end_date) {
		
		calculate(simple_date_mode, first_ticker, second_ticker, start_date, end_date);
		// wakelock makes calculating thread not discard on sleep
	}
	
	public void calculate(boolean sdm, String ft, String st, String sd, String ed) {
		
		final boolean simple_date_mode = sdm;
		final String first_ticker = ft;
		final String second_ticker = st;
		final String start_date = sd;
		final String end_date = ed;
		
		final ProgressDialog pdialog = new ProgressDialog(MainActivity.this);
		pdialog.setMessage(getString(R.string.calculating));
		pdialog.setCancelable(false);
		pdialog.show();
		
		new Thread(new Runnable() {
			
			Handler handler = new Handler();
			
			public void run() {
				
				Looper.myLooper();
				Looper.prepare();
				
				if (first_ticker.equals("Ticker 1") || second_ticker.equals("Ticker 2") || 
						start_date.equals("Start Date") || end_date.equals("End Date")) {
					
					pdialog.dismiss();
					
					handler.post(new Runnable() {
    					@Override
    					public void run() {
    						Toast missing_parameter_toast = Toast.makeText(getApplicationContext(), 
    								R.string.missing_input, Toast.LENGTH_SHORT);
    						missing_parameter_toast.show();
    					}
					});
        			
        			return;
					
				}
				
				
				SimpleDateFormat date_in = new SimpleDateFormat("dd - MMM - yyyy",Locale.CANADA);
        		SimpleDateFormat date_out = new SimpleDateFormat("yyyy-MM-dd",Locale.CANADA);
				
        		Date start_date_temp = new Date();
        		try {start_date_temp = date_in.parse(start_date);}
        		catch (ParseException e) {return;}
        		String start_date_formatted = date_out.format(start_date_temp);
        		Date end_date_temp = new Date();
        		try {end_date_temp = date_in.parse(end_date);}
        		catch (ParseException e) {return;}
        		String end_date_formatted = date_out.format(end_date_temp);
        		
        		if (System.currentTimeMillis() < end_date_temp.getTime() || 
        				end_date_temp.getTime() <= start_date_temp.getTime()) {
        			
        			pdialog.dismiss();
					
					handler.post(new Runnable() {
    					@Override
    					public void run() {
    						Toast date_error_toast = Toast.makeText(getApplicationContext(), 
    								R.string.invalid_dates, Toast.LENGTH_SHORT);
    						date_error_toast.show();
    					}
					});
        			
        			return;
        			
        		}
				    			
    			if (end_date_temp.getTime() - start_date_temp.getTime() > 20L*1000L*60L*60L*24L*365L) {
    				
    				pdialog.dismiss();
    				
    				handler.post(new Runnable() {
    					@Override
    					public void run() {
    						Toast max_thirty_years_toast = Toast.makeText(getApplicationContext(), 
    								R.string.maximum_twenty_years, Toast.LENGTH_SHORT);
    						max_thirty_years_toast.show();
    					}
    				});
        			
        			return;
    				
    			}
    			
    			List<String> date_list = new ArrayList<String>();
    			List<BigDecimal> first_ticker_price_list = new ArrayList<BigDecimal>();
    			List<BigDecimal> second_ticker_price_list = new ArrayList<BigDecimal>();
    			
    			ConnectivityManager conn_manager = (ConnectivityManager) 
						getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo network_info = conn_manager.getActiveNetworkInfo();
				
				if (network_info == null || !network_info.isConnected()) {
					
					pdialog.dismiss();
        			
        			handler.post(new Runnable() {
    					@Override
    					public void run() {
    						Toast no_internet_connection_toast = Toast.makeText(getApplicationContext(), 
    								R.string.no_internet_connection, 
    								Toast.LENGTH_SHORT);
    						no_internet_connection_toast.show();
    					}	
    				});
        			
        			return;
        			
				}
				
				String start_day = start_date_formatted.substring(8,10);
				String start_month = String.valueOf(Integer.parseInt(start_date_formatted.substring(5,7))-1);
				if (start_month.length() < 2) {start_month = "0" + start_month;}
				String start_year = start_date_formatted.substring(0,4);
				String end_day = end_date_formatted.substring(8,10);
				String end_month = String.valueOf(Integer.parseInt(end_date_formatted.substring(5,7))-1);
				if (end_month.length() < 2) {end_month = "0" + end_month;}
				String end_year = end_date_formatted.substring(0,4);
							
				String first_ticker_url = "http://real-chart.finance.yahoo.com/table.csv?s=" + first_ticker + 
						"&a=" + start_month + "&b=" + start_day + "&c=" + start_year +
						"&d=" + end_month + "&e=" + end_day + "&f=" + end_year +
						"&g=d&ignore=.csv";
				String second_ticker_url = "http://real-chart.finance.yahoo.com/table.csv?s=" + second_ticker + 
						"&a=" + start_month + "&b=" + start_day + "&c=" + start_year +
						"&d=" + end_month + "&e=" + end_day + "&f=" + end_year +
						"&g=d&ignore=.csv";
				String latest_quote_url = "http://finance.yahoo.com/webservice/v1/symbols/" + first_ticker + "," +
						second_ticker + "/quote?format=json";
	        			
	        	AsyncTask<String, Void, List<Quote>> first_ticker_sdt = 
	        			new StockDownloadTask(MainActivity.this).execute(first_ticker_url);
	        	AsyncTask<String, Void, List<Quote>> second_ticker_sdt = 
	        			new StockDownloadTask(MainActivity.this).execute(second_ticker_url);
	        	AsyncTask<String, Void, List<Quote>> latest_quote_sdt = 
	        			new StockDownloadTask(MainActivity.this).execute(latest_quote_url);
	        			
	        	if (first_ticker_sdt == null || second_ticker_sdt == null || latest_quote_sdt == null) {
                			
                	pdialog.dismiss(); 
                	
                	handler.post(new Runnable() {
            			@Override
            			public void run() {
            				Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
            						R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
            			}
                	});
                			
                	return;
                			
                }
	        			
	        	List<Quote> first_ticker_quote_list;
	        	List<Quote> second_ticker_quote_list;
	        	List<Quote> latest_quote_list;
	        			
	        	try {
	        				
	        		first_ticker_quote_list = first_ticker_sdt.get();
					second_ticker_quote_list = second_ticker_sdt.get();
					latest_quote_list = latest_quote_sdt.get();
							
	        	} catch (ExecutionException e) {
	        				
	        		pdialog.dismiss(); 
                	
                	handler.post(new Runnable() {
            			@Override
            			public void run() {
            				Toast connection_error_toast = Toast.makeText(getApplicationContext(), 
            						R.string.connection_error, Toast.LENGTH_SHORT);
            				connection_error_toast.show();
            			}
                	});
                	
                	return;
	        				
	        	} catch (InterruptedException e) {
	        				
	        		pdialog.dismiss(); 
                	
                	handler.post(new Runnable() {
            			@Override
            			public void run() {
            				Toast connection_error_toast = Toast.makeText(getApplicationContext(), 
            						R.string.connection_error, Toast.LENGTH_SHORT);
            				connection_error_toast.show();
            			}
                	});
                	
                	return;
	        		
	        	}
	        			
	        	if (first_ticker_quote_list == null || second_ticker_quote_list == null || latest_quote_list == null) {
	        				
	        		pdialog.dismiss(); 
                			
                	handler.post(new Runnable() {
            			@Override
            			public void run() {
            				Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
            						R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
            			}
                	});
                			
                	return;
	        				
	        	}
	        			
	        	if (first_ticker_quote_list.size() != second_ticker_quote_list.size() ||
						first_ticker_quote_list.size() == 0) {
							
					pdialog.dismiss();
							
					if (first_ticker_quote_list.size() == 0 || second_ticker_quote_list.size() == 0) {
						
						handler.post(new Runnable() {
	            			@Override
	            			public void run() {
	            				Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
	            						R.string.data_unavailable, Toast.LENGTH_SHORT);
	            				data_unavailable_toast.show();
	            			}
	                	});
	                	
	                	return;
								
					} else if (first_ticker_quote_list.get(first_ticker_quote_list.size() - 1).date.equals(
								second_ticker_quote_list.get(second_ticker_quote_list.size() - 1).date)) {
								
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast date_misalignment_toast = Toast.makeText(getApplicationContext(),
										R.string.date_misalignment, Toast.LENGTH_SHORT);
								date_misalignment_toast.show();
							}
						});
								
					} else {
								
						String suggested_start_date;
						if (first_ticker_quote_list.size() < second_ticker_quote_list.size()) {
									
							suggested_start_date = first_ticker_quote_list.get(first_ticker_quote_list.size() - 1).date;
									
						} else {
									
							suggested_start_date = second_ticker_quote_list.get(second_ticker_quote_list.size() - 1).date;
									
						}
								
						Date suggested_start_date_temp;
						Date one_month_start_date_temp;
						try {
							suggested_start_date_temp = date_out.parse(suggested_start_date);
							
						}
						catch (ParseException e) {return;}
						// in case of simple inputs
						Calendar calendar = Calendar.getInstance();
						calendar.add(Calendar.MONTH, -1);
						one_month_start_date_temp = calendar.getTime();
						final String one_month_start_date = date_in.format(one_month_start_date_temp);
						final String suggested_start_date_formatted = date_in.format(suggested_start_date_temp);
						final String error_message = "DATA UNAVAILABLE BEFORE " + suggested_start_date_formatted;
								
						handler.post(new Runnable() {
	            			@Override
	            			public void run() {
	            				if (simple_date_mode) {
	            					inputs_simple.start_date = one_month_start_date;
	            					inputs_simple.lookback_spinner.setSelection(0);
	            				} else {
		            				inputs.start_date = suggested_start_date_formatted;
		            				inputs.start_date_button.setText(suggested_start_date_formatted);
	            				}
	            				Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
	            						error_message, Toast.LENGTH_LONG);
	            				data_unavailable_toast.show();
	            			}
	                	});
	                	
	                	return;
						
					}
				}
	        	
	        	SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.CANADA);
	        	Calendar calendar = Calendar.getInstance();
	        	calendar.clear();
	        	Date first_ticker_last_date;
	        	try {first_ticker_last_date = date_format.parse(first_ticker_quote_list.get(0).date);}
	        	catch (ParseException e) {return;}
	        	calendar.setTime(first_ticker_last_date);
	        	long first_ticker_last_date_in_seconds = calendar.getTimeInMillis() / 1000L;
	        	long latest_quote_date_in_seconds;
	        	try {latest_quote_date_in_seconds = Long.parseLong(latest_quote_list.get(0).date);}
	        	catch (NumberFormatException e) {return;}
	        	Date latest_quote_date = new Date(latest_quote_date_in_seconds * 1000L);
	        	String latest_quote_date_string = date_format.format(latest_quote_date);
	        	last_quote_time = "Close"; // changed to time if refresh valid
	        	
	        	// do not refresh if end date does not equal today
	    		SimpleDateFormat date_format2 = new SimpleDateFormat("dd - MMM - yyyy", Locale.CANADA);
	    		Date end_date_time;
	    		try {end_date_time = date_format2.parse(end_date);}
	    		catch (ParseException e) {return;}
	    		Calendar today = Calendar.getInstance();
	    		today.set(Calendar.HOUR_OF_DAY, 0);
	    		today.set(Calendar.MINUTE, 0);
	    		today.set(Calendar.SECOND, 0);
	    		today.set(Calendar.MILLISECOND, 0);
	    		Date today_time = today.getTime();
	    		
	        	if (latest_quote_date_in_seconds - first_ticker_last_date_in_seconds > 86400 && 
	        			end_date_time.equals(today_time)) {
	        	// the dates from the csv use midnight whereas the quote is at close
	        	// the midday quote is available so add to front of lists
	        		date_list.add(latest_quote_date_string);
	        		first_ticker_price_list.add(latest_quote_list.get(0).price);
	        		second_ticker_price_list.add(latest_quote_list.get(1).price);
	        		long last_quote_minute = (latest_quote_date_in_seconds / 60L) % 60;
	        		long last_quote_hour = (latest_quote_date_in_seconds / 3600L) % 24 - 4; // convert from GMT to EST
	        		TimeZone new_york_time_zone = TimeZone.getTimeZone("America/New York");
					Date latest_date = new Date(latest_quote_date_in_seconds);
					if (new_york_time_zone.inDaylightTime(latest_date)) {
						last_quote_hour = last_quote_hour - 1;
					}
	        		last_quote_time = String.format("%02d:%02d", last_quote_hour, last_quote_minute);
	        	}
						
	        	for (int i = 0; i < first_ticker_quote_list.size(); i++) {
	
            		date_list.add(first_ticker_quote_list.get(i).date);
            		first_ticker_price_list.add(first_ticker_quote_list.get(i).price);
            		second_ticker_price_list.add(second_ticker_quote_list.get(i).price);
            				
            	}

	        	first_ticker_quote_list.clear();
	        	second_ticker_quote_list.clear();
					
					
	        	dates = new Date[date_list.size()];
	        	for (int i = 0; i < date_list.size(); i++) {
            			
	        		try {
            				
	        			dates[i] = date_out.parse(date_list.get(i));
            				
	        		} catch (ParseException e) {
            				
            		pdialog.dismiss();
                			
                	handler.post(new Runnable() {
            			@Override
            			public void run() {
            				Toast dataerrortoast = Toast.makeText(getApplicationContext(), R.string.execution_error, 
            						Toast.LENGTH_SHORT);
            				dataerrortoast.show();
            			}	
            		});
                			
                	return;
                			
            	} 
            			
            }
            		
            date_list.clear();
            		
           	first_ticker_prices = new BigDecimal[first_ticker_price_list.size()];
           	second_ticker_prices = new BigDecimal[second_ticker_price_list.size()];
            		
            for (int i = 0; i < first_ticker_price_list.size(); i++) {
            	
            	first_ticker_prices[i] = first_ticker_price_list.get(i);
            	second_ticker_prices[i] = second_ticker_price_list.get(i);
            		
            }
            		
            		
            first_ticker_price_list.clear();
            second_ticker_price_list.clear();
            		
            first_ticker_returns = new double[first_ticker_prices.length - 1];
            second_ticker_returns = new double[second_ticker_prices.length - 1];
            		
            for (int i = 0; i < first_ticker_returns.length; i++) {
            			
            	BigDecimal temp = new BigDecimal("0.00");
            	temp = first_ticker_prices[i].divide(first_ticker_prices[i+1], 9, RoundingMode.HALF_UP);
            	first_ticker_returns[i] = (temp.doubleValue() - 1) * 100;
            	temp = second_ticker_prices[i].divide(second_ticker_prices[i+1], 9, RoundingMode.HALF_UP);
            	second_ticker_returns[i] = (temp.doubleValue() - 1) * 100;
            		
            }
            		
           calculateVariables(false, false);
				
			pdialog.dismiss();
				
			handler.post(new Runnable() {
				@Override
				public void run() {
					getActionBar().getTabAt(1).select();
				}
			});
				
			FragmentManager fragment_manager = getFragmentManager();
			FragmentTransaction transaction = fragment_manager.beginTransaction();
			transaction.replace(R.id.container, quotes);
			//transaction.addToBackStack(null);
			transaction.commitAllowingStateLoss();
				
			}
			
		}).start();
		
	}
	
	public double[] returnLine(double[] returns) {
		
		double[] return_line = new double[returns.length + 1];
		return_line[returns.length] = 0;
		
		for (int i = 1; i < return_line.length; i++) { // latest point is first
			
			return_line[return_line.length - 1 - i] = ((1 + return_line[return_line.length - i] / 100.00) * 
					(1 + returns[return_line.length - 1 - i] / 100.00) - 1) * 100.00;
			
		}
		
		return return_line;
		
	}
	
	public double[] spread(BigDecimal[] first_prices, BigDecimal[] second_prices) {
		
		double[] spread = new double[first_prices.length];
		
		for (int i = 0; i < spread.length; i++) {
			
			spread[i] = first_prices[i].subtract(second_prices[i]).doubleValue();
			
		}
		
		return spread;
		
	}
	
	public double[] ratio(BigDecimal[] first_prices, BigDecimal[] second_prices) {
		
		double[] ratio = new double[first_prices.length];
		
		for (int i = 0; i < ratio.length; i++) {
			
			ratio[i] = first_prices[i].divide(second_prices[i], 9, RoundingMode.HALF_UP).doubleValue();
			
		}
		
		return ratio;
		
	}
	
	public double mean(double[] array) {
		
		double mean = 0.00;
		
		for (int i = 0; i < array.length; i++) {
			
			mean = mean + array[i];
			
		}
		
		return mean / (double) array.length;
		
	}
	
	public double beta(double[] first_prices, double[] second_prices) {
		
		double beta = 0.00;
		double sum_prod = 0.00;
		double sum_squares = 0.00;
		
		for (int i = 0; i < first_prices.length; i++) { // calculating non-intercept beta
			
			sum_prod = sum_prod + first_prices[i] * second_prices[i];
			sum_squares = sum_squares + Math.pow(second_prices[i], 2);
			
		}
		
		beta = sum_prod / sum_squares;
		
		return beta;
		
	}
	
	public double[] residuals(double[] first_prices, double[] second_prices, double beta) {
		
		double[] residuals = new double[first_prices.length];
		
		for (int i = 0; i < residuals.length; i++) {
			
			residuals[i] = first_prices[i] - beta * second_prices[i];
			
		}
		
		return residuals;
		
	}
	
	public double[] convert(BigDecimal[] array, boolean log) {
		
		double[] converted_array = new double[array.length];
		
		for (int i = 0; i < converted_array.length; i++) {
			
			if (log) {converted_array[i] = Math.log(array[i].doubleValue());}
			else {converted_array[i] = array[i].doubleValue();}
			
		}
		
		return converted_array;
		
	}
	
	public double correlation(double[] first_returns, double[] second_returns) {
		
		double first_mean = mean(first_returns);
		double second_mean = mean(second_returns);
		double sum_prod = 0.00;
		double sum_sq_1 = 0.00;
		double sum_sq_2 = 0.00;
		
		for (int i = 0; i < first_returns.length; i++) {
			
			sum_prod = sum_prod + ((first_returns[i] - first_mean) * (second_returns[i] - second_mean));
			sum_sq_1 = sum_sq_1 + Math.pow(first_returns[i] - first_mean, 2);
			sum_sq_2 = sum_sq_2 + Math.pow(second_returns[i] - second_mean, 2);
			
		}
		
		double correlation = sum_prod / (Math.sqrt(sum_sq_1) * Math.sqrt(sum_sq_2));
		
		return correlation;
		
		
	}
	
	public double calculateReturn(BigDecimal[] first_prices, BigDecimal[] second_prices, int interval) {
		
		double change = 0.0;
		
		if (first_prices.length > interval) {
			BigDecimal first_change = first_prices[0].divide(first_prices[interval], 9, RoundingMode.HALF_UP);
			BigDecimal second_change = second_prices[0].divide(second_prices[interval], 9, RoundingMode.HALF_UP);
			BigDecimal change_decimal = first_change.subtract(second_change);
			change = change_decimal.doubleValue();
		}
		
		return change;
		
	}
	
	@Override
	public void onSwitchButtonClick(boolean simple_date_mode, String first_ticker, String second_ticker, String first_security_name,
			String second_security_name, String start_date, String end_date) {
		
		simple_date_range = !simple_date_mode;
		if (simple_date_mode) { // coming from simple so going to advanced
			inputs.first_ticker = first_ticker;
			inputs.second_ticker = second_ticker;
			inputs.first_security_name = first_security_name;
			inputs.second_security_name = second_security_name;
			inputs.start_date = start_date;
			inputs.end_date = end_date;
		} else {
			inputs_simple.first_ticker = first_ticker;
			inputs_simple.second_ticker = second_ticker;
			inputs_simple.first_security_name = first_security_name;
			inputs_simple.second_security_name = second_security_name;
			inputs_simple.start_date = start_date;
			inputs_simple.end_date = end_date;
		}
		
		FragmentManager fragment_manager = getFragmentManager();
		FragmentTransaction transaction = fragment_manager.beginTransaction();
		if (simple_date_mode) {transaction.replace(R.id.container, inputs);}
		else {transaction.replace(R.id.container, inputs_simple);}
		//transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
		
	}

	
	@Override
	public void onRefreshButtonClick(final String fragment) {
		
		// do not refresh if end date does not equal today
		SimpleDateFormat date_format = new SimpleDateFormat("dd - MMM - yyyy", Locale.CANADA);
		Date end_date_time;
		try {end_date_time = date_format.parse(end_date);}
		catch (ParseException e) {return;}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date today_time = calendar.getTime();
		if (end_date_time.before(today_time)) {return;}
		
		final ProgressDialog pdialog = new ProgressDialog(MainActivity.this);
		pdialog.setMessage(getString(R.string.calculating));
		pdialog.setCancelable(false);
		pdialog.show();
		
		new Thread(new Runnable() {
			
			Handler handler = new Handler();
			
			public void run() {
				
				Looper.myLooper();
				Looper.prepare();
		
				// get the new prices
				String latest_quote_url = "http://finance.yahoo.com/webservice/v1/symbols/" + first_ticker + "," +
						second_ticker + "/quote?format=json";
		    	AsyncTask<String, Void, List<Quote>> latest_quote_sdt = 
		    			new StockDownloadTask(MainActivity.this).execute(latest_quote_url);
		    	List<Quote> latest_quote_list;
		    	try {latest_quote_list = latest_quote_sdt.get();}
		    	catch (ExecutionException e) {
		    		pdialog.dismiss();
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
									R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
						}
					});
		    		return;
		    	}
		    	catch (InterruptedException e) {
		    		pdialog.dismiss();
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
									R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
						}
					});
		    		return;
		    	}
		    	if (latest_quote_list == null) {
		    		pdialog.dismiss();
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
									R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
						}
					});
		    		return;
		    	}
		    	if (latest_quote_list.size() < 2) {
		    		pdialog.dismiss();
					handler.post(new Runnable() {
						@Override
						public void run() {
							Toast data_unavailable_toast = Toast.makeText(getApplicationContext(), 
									R.string.data_unavailable, Toast.LENGTH_SHORT);
            				data_unavailable_toast.show();
						}
					});
		    		return;
		    	}
		    	// update the last prices and returns
				first_ticker_prices[0] = latest_quote_list.get(0).price;
				second_ticker_prices[0] = latest_quote_list.get(1).price;
				first_ticker_returns[0] = (first_ticker_prices[0].divide(first_ticker_prices[1], 9, RoundingMode.HALF_UP).doubleValue() - 1) * 100;
				second_ticker_returns[0] = (second_ticker_prices[0].divide(second_ticker_prices[1], 9, RoundingMode.HALF_UP).doubleValue() - 1) * 100;
				long latest_quote_date_in_seconds = Long.parseLong(latest_quote_list.get(0).date);
				long last_quote_minute = (latest_quote_date_in_seconds / 60L) % 60;
				long last_quote_hour = (latest_quote_date_in_seconds / 3600L) % 24 - 4; // convert from GMT to EST
				TimeZone new_york_time_zone = TimeZone.getTimeZone("America/New York");
				Date latest_date = new Date(latest_quote_date_in_seconds);
				if (new_york_time_zone.inDaylightTime(latest_date)) {
					last_quote_hour = last_quote_hour - 1;
				}
        		last_quote_time = String.format("%02d:%02d", last_quote_hour, last_quote_minute);		
        		
        		boolean flipped = pairs.flipped;
        		
        		calculateVariables(flipped, false);
				
				pdialog.dismiss();
				
				FragmentManager fragment_manager = getFragmentManager();
				FragmentTransaction transaction = fragment_manager.beginTransaction();
				if (fragment.equals("quotes")) {
					transaction.detach(quotes);
					transaction.attach(quotes);
				}
				else {
					transaction.detach(pairs);
					transaction.attach(pairs);
				}
				//transaction.addToBackStack(null);
				transaction.commitAllowingStateLoss();
				
			}
		}).start();
		
	}

	@Override
	public void onFlipButtonClick() {
		
		boolean flipped = pairs.flipped; // before discarding all variables
		
		pairs = new PairsFragment();
		
		calculateVariables(flipped, true);
		
		FragmentManager fragment_manager = getFragmentManager();
		FragmentTransaction transaction = fragment_manager.beginTransaction();
		transaction.replace(R.id.container, pairs);
		//transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
		
	}
	
	public void calculateVariables(boolean flipped, boolean flip_intent) {
		
		quotes.pair_loaded = true;
		quotes.first_ticker = first_ticker;
		quotes.second_ticker = second_ticker;
		quotes.dates = dates;
		quotes.first_ticker_prices = first_ticker_prices;
		quotes.second_ticker_prices = second_ticker_prices;
		quotes.last_quote_time = last_quote_time;
		
		if ((!flipped && flip_intent) || (flipped && !flip_intent)) {
			
			pairs.pair_loaded = true;
			pairs.first_ticker = second_ticker;
			pairs.second_ticker = first_ticker;
			pairs.first_ticker_return_line = returnLine(second_ticker_returns);
			pairs.second_ticker_return_line = returnLine(first_ticker_returns);
			pairs.dates = dates;
			pairs.last_quote_time = last_quote_time;
			pairs.spread = spread(second_ticker_prices, first_ticker_prices);
			pairs.ratio = ratio(second_ticker_prices, first_ticker_prices);
			pairs.beta = beta(convert(second_ticker_prices, false), convert(first_ticker_prices, false));
			pairs.residuals = residuals(convert(second_ticker_prices, false), convert(first_ticker_prices, false), pairs.beta);
			pairs.log_beta = beta(convert(second_ticker_prices, true), convert(first_ticker_prices, true));
			pairs.log_residuals = residuals(convert(second_ticker_prices, true), convert(first_ticker_prices, true), pairs.log_beta);
			pairs.correlation = correlation(first_ticker_returns, second_ticker_returns);
			pairs.last_spread = pairs.spread[0];
	        pairs.last_ratio = pairs.ratio[0];
	        pairs.last_residuals = pairs.residuals[0];
	        pairs.last_log_residuals = pairs.log_residuals[0];
	        pairs.day_change = calculateReturn(second_ticker_prices, first_ticker_prices, 1);
	        pairs.week_change = calculateReturn(second_ticker_prices, first_ticker_prices, 5);
			pairs.spread_mean = mean(pairs.spread);
			pairs.ratio_mean = mean(pairs.ratio);
			pairs.residuals_mean = mean(pairs.residuals);
			pairs.log_residuals_mean = mean(pairs.log_residuals);
			pairs.flipped = true;
		
		} else {
			
			pairs.pair_loaded = true;
    		pairs.first_ticker = first_ticker;
    		pairs.second_ticker = second_ticker;
    		pairs.dates = dates;
    		pairs.last_quote_time = last_quote_time;
    		pairs.first_ticker_return_line = returnLine(first_ticker_returns);
    		pairs.second_ticker_return_line = returnLine(second_ticker_returns);
    		pairs.spread = spread(first_ticker_prices, second_ticker_prices);
    		pairs.ratio = ratio(first_ticker_prices, second_ticker_prices);
    		pairs.beta = beta(convert(first_ticker_prices, false), convert(second_ticker_prices, false));
    		pairs.residuals = residuals(convert(first_ticker_prices, false), convert(second_ticker_prices, false), pairs.beta);
    		pairs.log_beta = beta(convert(first_ticker_prices, true), convert(second_ticker_prices, true));
    		pairs.log_residuals = residuals(convert(first_ticker_prices, true), convert(second_ticker_prices, true), pairs.log_beta);
    		pairs.correlation = correlation(first_ticker_returns, second_ticker_returns);
    		pairs.last_spread = pairs.spread[0];
            pairs.last_ratio = pairs.ratio[0];
            pairs.last_residuals = pairs.residuals[0];
            pairs.last_log_residuals = pairs.log_residuals[0];
            pairs.day_change = calculateReturn(first_ticker_prices, second_ticker_prices, 1);
	        pairs.week_change = calculateReturn(first_ticker_prices, second_ticker_prices, 5);
    		pairs.spread_mean = mean(pairs.spread);
    		pairs.ratio_mean = mean(pairs.ratio);
    		pairs.residuals_mean = mean(pairs.residuals);
    		pairs.log_residuals_mean = mean(pairs.log_residuals);
    		pairs.flipped = false;
			
		}
		
	}
	
}
