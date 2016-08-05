package com.dgutkin.pairstool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuotesFragment extends Fragment {
	
	boolean pair_loaded = false;
	String first_ticker;
	String second_ticker;
	Date[] dates;
	BigDecimal[] first_ticker_prices;
	BigDecimal[] second_ticker_prices;
	String last_quote_time;
	
	TextView first_ticker_text;
	TextView second_ticker_text;
	TextView first_ticker_last_text;
	TextView second_ticker_last_text;
	TextView first_ticker_change_text;
	TextView second_ticker_change_text;
	TextView first_ticker_week_change_text;
	TextView second_ticker_week_change_text;
	TextView first_ticker_month_change_text;
	TextView second_ticker_month_change_text;
	TextView date_disclaimer_text;
	
	public interface RefreshListener {
		
		public void onRefreshButtonClick(String fragment);
		
	}
	
	RefreshListener listener;
	
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		try {listener = (RefreshListener) activity;}
		catch (ClassCastException e) {throw new ClassCastException(activity.toString() + "must implement RefreshListener");}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		super.onCreateOptionsMenu(menu, inflater);
		getActivity().getMenuInflater().inflate(R.menu.quotes, menu);
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
		if (item.getItemId() == R.id.action_about) {
			
			AboutDialogFragment about_dialog = new AboutDialogFragment();
    		about_dialog.show(getFragmentManager(), "about");
    		
		} else if (item.getItemId() == R.id.action_refresh) {
			if (pair_loaded) {listener.onRefreshButtonClick("quotes");}
		}
		
		return super.onOptionsItemSelected(item);
    	
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
	
		View view = inflater.inflate(R.layout.fragment_no_pairs, container, false);
		
		if (pair_loaded) {
			
			view = inflater.inflate(R.layout.fragment_quotes, container, false);
			
			first_ticker_text = (TextView) view.findViewById(R.id.first_ticker);
			second_ticker_text = (TextView) view.findViewById(R.id.second_ticker);
			first_ticker_month_change_text = (TextView) view.findViewById(R.id.first_ticker_month_change);
			second_ticker_month_change_text = (TextView) view.findViewById(R.id.second_ticker_month_change);
			first_ticker_week_change_text = (TextView) view.findViewById(R.id.first_ticker_week_change);
			second_ticker_week_change_text = (TextView) view.findViewById(R.id.second_ticker_week_change);
			first_ticker_change_text = (TextView) view.findViewById(R.id.first_ticker_change);
			second_ticker_change_text = (TextView) view.findViewById(R.id.second_ticker_change);
			first_ticker_last_text = (TextView) view.findViewById(R.id.first_ticker_price);
			second_ticker_last_text = (TextView) view.findViewById(R.id.second_ticker_price);
			
			date_disclaimer_text = (TextView) view.findViewById(R.id.date_disclaimer);
			DecimalFormat df = new DecimalFormat("0.00");
			
			first_ticker_text.setText(first_ticker);
			second_ticker_text.setText(second_ticker);
		
			BigDecimal first_ticker_last = first_ticker_prices[0];
			BigDecimal second_ticker_last = second_ticker_prices[0];
			
			if (first_ticker_prices.length > 20) {
				double first_ticker_month_change = first_ticker_last.divide(first_ticker_prices[20], 9, RoundingMode.HALF_UP).doubleValue();
				double second_ticker_month_change = second_ticker_last.divide(second_ticker_prices[20], 9, RoundingMode.HALF_UP).doubleValue();
				first_ticker_month_change_text.setText(String.valueOf(df.format((first_ticker_month_change-1)*100)) + "% (20D)");
				second_ticker_month_change_text.setText(String.valueOf(df.format((second_ticker_month_change-1)*100)) + "% (20D)");
				if (first_ticker_month_change < 1) {first_ticker_month_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {first_ticker_month_change_text.setTextColor(Color.parseColor("#006400"));}
				if (second_ticker_month_change < 1) {second_ticker_month_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {second_ticker_month_change_text.setTextColor(Color.parseColor("#006400"));}
			} else {
				first_ticker_month_change_text.setText("NA");
				second_ticker_month_change_text.setText("NA");
			}
			if (first_ticker_prices.length > 5) {
				double first_ticker_week_change = first_ticker_last.divide(first_ticker_prices[5], 9, RoundingMode.HALF_UP).doubleValue();
				double second_ticker_week_change = second_ticker_last.divide(second_ticker_prices[5], 9, RoundingMode.HALF_UP).doubleValue();
				first_ticker_week_change_text.setText(String.valueOf(df.format((first_ticker_week_change-1)*100)) + "% (5D)");
				second_ticker_week_change_text.setText(String.valueOf(df.format((second_ticker_week_change-1)*100)) + "% (5D)");
				if (first_ticker_week_change < 1) {first_ticker_week_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {first_ticker_week_change_text.setTextColor(Color.parseColor("#006400"));}
				if (second_ticker_week_change < 1) {second_ticker_week_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {second_ticker_week_change_text.setTextColor(Color.parseColor("#006400"));}
			} else {
				first_ticker_week_change_text.setText("NA");
				second_ticker_week_change_text.setText("NA");
			}
			if (first_ticker_prices.length > 1) {
				double first_ticker_change = first_ticker_last.divide(first_ticker_prices[1], 9, RoundingMode.HALF_UP).doubleValue();
				double second_ticker_change = second_ticker_last.divide(second_ticker_prices[1], 9, RoundingMode.HALF_UP).doubleValue();
				first_ticker_change_text.setText(String.valueOf(df.format((first_ticker_change-1)*100)) + "% (1D)");
				second_ticker_change_text.setText(String.valueOf(df.format((second_ticker_change-1)*100)) + "% (1D)");
				if (first_ticker_change < 1) {first_ticker_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {first_ticker_change_text.setTextColor(Color.parseColor("#006400"));}
				if (second_ticker_change < 1) {second_ticker_change_text.setTextColor(Color.parseColor("#AA0114"));}
				else {second_ticker_change_text.setTextColor(Color.parseColor("#006400"));}
			} else {
				first_ticker_last_text.setText("NA");
				second_ticker_last_text.setText("NA");
			}
			first_ticker_last_text.setText(String.valueOf(df.format(first_ticker_last.doubleValue())));
			second_ticker_last_text.setText(String.valueOf(df.format(second_ticker_last.doubleValue())));
			
			
			double[] first_ticker_prices_double = new double[first_ticker_prices.length];
			double[] second_ticker_prices_double = new double[second_ticker_prices.length];
			for (int i = 0; i < first_ticker_prices.length; i++) {
				
				first_ticker_prices_double[i] = first_ticker_prices[i].doubleValue();
				second_ticker_prices_double[i] = second_ticker_prices[i].doubleValue();
				
			}
			
			SimpleDateFormat dtf = new SimpleDateFormat("dd-MMM-yyyy", Locale.CANADA);
			date_disclaimer_text.setText("*As Of " + dtf.format(dates[0]) + " " + last_quote_time);
			
			//create charts
			createChart(first_ticker_prices_double, dates, first_ticker, view, true);
			createChart(second_ticker_prices_double, dates, second_ticker, view, false);
		
		}
		
		return view;
		
	}
	
public void createChart(double[] y, Date[] x, String ticker, View view, boolean first) {
		
		TimeSeries series_time_series = new TimeSeries(ticker + " Price");
		
		for (int i = 0; i < x.length; i++) {
			
			series_time_series.add(x[i], y[i]);
			
		}
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series_time_series);
		
		XYSeriesRenderer series_renderer = new XYSeriesRenderer();
		series_renderer.setColor(Color.parseColor("#8C001A"));
		if (first) {series_renderer.setColor(Color.parseColor("#000066"));}
		series_renderer.setFillPoints(true);
		series_renderer.setLineWidth(3f);
		
		XYMultipleSeriesRenderer mult_renderer = new XYMultipleSeriesRenderer();
		mult_renderer.setLabelsColor(Color.BLACK);
		mult_renderer.setAxesColor(Color.BLACK);
		mult_renderer.setXLabelsColor(Color.BLACK);
		mult_renderer.setYLabelsColor(0,Color.BLACK);
		mult_renderer.addSeriesRenderer(series_renderer);
		mult_renderer.setYLabelsAlign(Align.RIGHT);
		mult_renderer.setApplyBackgroundColor(true);
		mult_renderer.setBackgroundColor(Color.parseColor("#C0C0C0"));
		mult_renderer.setMarginsColor(Color.parseColor("#C0C0C0"));
		mult_renderer.setShowLegend(true);
		mult_renderer.setFitLegend(true);
		mult_renderer.setShowGrid(true);
		mult_renderer.setGridColor(Color.parseColor("#454545"));
		mult_renderer.setYLabelsPadding(10);
		mult_renderer.setYLabels(6);
		mult_renderer.setLabelsTextSize(15);
		mult_renderer.setPanEnabled(true);
		mult_renderer.setZoomEnabled(true);
		mult_renderer.setLegendTextSize(20);
		DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
		float val_side = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 27, metrics);
		float val_top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9, metrics);
		int val_side_int = Math.round(val_side); // screen resolution adjusted margin value
		int val_top_int = Math.round(val_top);
		int[] margins = {val_top_int, val_side_int, val_top_int, val_side_int}; // top, left, bottom, right
		mult_renderer.setMargins(margins);
		double series_min = y[0];
		double series_max = y[0];
		for (int i = 0; i < y.length; i++) {if (series_min > y[i]) {series_min = y[i];}}
		for (int i = 0; i < y.length; i++) {if (series_max < y[i]) {series_max = y[i];}}
		if (series_min > 0) {series_min = series_min * 0.95;} else {series_min = series_min * 1.05;}
		if (series_max > 0) {series_max = series_max * 1.05;} else {series_max = series_max * 0.95;}
		double[] limits = {x[x.length-1].getTime(), x[0].getTime(), series_min, series_max};
		mult_renderer.setPanLimits(limits);
		mult_renderer.setZoomLimits(limits);
		
		GraphicalView chartview = ChartFactory.getTimeChartView(getActivity(), dataset, mult_renderer,"dd-MMM-yy");
		LinearLayout linearlayout;
		if (first) {linearlayout = (LinearLayout) view.findViewById(R.id.first_ticker_chart);}
		else {linearlayout = (LinearLayout) view.findViewById(R.id.second_ticker_chart);}
		linearlayout.removeAllViews();
		linearlayout.addView(chartview, new LayoutParams (LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		
	}

}
