package com.dgutkin.pairstool;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class PairsFragment extends Fragment implements OnItemSelectedListener {
	
	boolean pair_loaded = false;
	String first_ticker;
	String second_ticker;
	Date[] dates;
	String last_quote_time;
	double[] first_ticker_return_line;
	double[] second_ticker_return_line;
	double[] spread;
	double[] ratio;
	double[] residuals;
	double[] log_residuals;
	double last_spread;
	double last_ratio;
	double last_residuals;
	double last_log_residuals;
	double beta;
	double log_beta;
	double correlation;
	double day_change;
	double week_change;
	double spread_mean;
	double ratio_mean;
	double residuals_mean;
	double log_residuals_mean;
	
	TextView title_text;
	TextView last_text;
	TextView one_day_change_text;
	TextView five_day_change_text;
	TextView date_disclaimer_text;
	
	boolean flipped = false;
	
	public interface RefreshListener {
		
		public void onRefreshButtonClick(String fragment);
		
	}
	
	RefreshListener refresh_listener;
	
	public interface FlipListener {
		
		public void onFlipButtonClick();
		
	}
	
	FlipListener flip_listener;
	
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		try {flip_listener = (FlipListener) activity;}
		catch (ClassCastException e) {throw new ClassCastException(activity.toString() + "must implement FlipListener");}
		try {refresh_listener = (RefreshListener) activity;}
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
		getActivity().getMenuInflater().inflate(R.menu.pairs, menu);
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
		if (item.getItemId() == R.id.action_about) {
			
			AboutDialogFragment about_dialog = new AboutDialogFragment();
    		about_dialog.show(getFragmentManager(), "about");
    		
		} else if (item.getItemId() == R.id.action_refresh) { 
		
			if (pair_loaded) {refresh_listener.onRefreshButtonClick("pairs");}
			
		} else if (item.getItemId() == R.id.action_flip) {
			
			if (pair_loaded) {flip_listener.onFlipButtonClick();}
			
		}
		
		return super.onOptionsItemSelected(item);
    	
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
		
		View view = inflater.inflate(R.layout.fragment_no_pairs, container, false);
		
		if (pair_loaded) {
			
			view = inflater.inflate(R.layout.fragment_pairs, container, false);
			// default is spread
			title_text = (TextView) view.findViewById(R.id.title);
			title_text.setText(first_ticker + " - " + second_ticker);
			
			Spinner series_type_spinner = (Spinner) view.findViewById(R.id.series_type_spinner);
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), 
					R.array.types, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			series_type_spinner.setAdapter(adapter);
			series_type_spinner.setOnItemSelectedListener(this);
			
			createReturnLineChart(first_ticker_return_line, second_ticker_return_line, dates, first_ticker, second_ticker, view);
			
			DecimalFormat df = new DecimalFormat("0.00");
			last_text = (TextView) view.findViewById(R.id.last);
			one_day_change_text = (TextView) view.findViewById(R.id.one_day_change);
			five_day_change_text = (TextView) view.findViewById(R.id.five_day_change);
			TextView corr_text = (TextView) view.findViewById(R.id.correlation);
			date_disclaimer_text = (TextView) view.findViewById(R.id.date_disclaimer_pairs_text);
			
			last_text.setText(String.valueOf(df.format(last_spread)));
			one_day_change_text.setText(String.valueOf(df.format(day_change * 100) + "%"));
			if (day_change < 0) {one_day_change_text.setTextColor(Color.parseColor("#AA0114"));}
			else {one_day_change_text.setTextColor(Color.parseColor("#006400"));}
			five_day_change_text.setText(String.valueOf(df.format(week_change * 100) + "%"));
			if (week_change < 0) {five_day_change_text.setTextColor(Color.parseColor("#AA0114"));}
			else {five_day_change_text.setTextColor(Color.parseColor("#006400"));}
			corr_text.setText(String.valueOf(df.format(correlation)));
			SimpleDateFormat dtf = new SimpleDateFormat("dd-MMM-yyyy", Locale.CANADA);
			date_disclaimer_text.setText("*As Of " + dtf.format(dates[0]) + " " + last_quote_time);
			
			
			createSeriesChart(spread, spread_mean, dates, series_type_spinner.getSelectedItem().toString(), view);
			
		}
		
		return view;
	
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		
		String type = parent.getSelectedItem().toString();
		DecimalFormat df = new DecimalFormat("0.00");
		
		if (id == 0) {
				title_text.setText(first_ticker + " - " + second_ticker);
				last_text.setText(String.valueOf(df.format(last_spread)));
				createSeriesChart(spread, spread_mean, dates, type, getView());
		} else if (id == 1) {
				title_text.setText(first_ticker + " / " + second_ticker);
				last_text.setText(String.valueOf(df.format(last_ratio)));
				createSeriesChart(ratio, ratio_mean, dates, type, getView());
		} else if (id == 2) {
				title_text.setText(first_ticker + " - " + String.valueOf(df.format(beta)) + " " + second_ticker);
				last_text.setText(String.valueOf(df.format(last_residuals)));
				createSeriesChart(residuals, residuals_mean, dates, type, getView());
		} else if (id == 3) {
				title_text.setText("log(" + first_ticker + ") - " + String.valueOf(df.format(log_beta)) + " log(" + second_ticker + ")");
				last_text.setText(String.valueOf(df.format(last_log_residuals)));
				createSeriesChart(log_residuals, log_residuals_mean, dates, type, getView());
		}
		
	}
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// do nothing
	}
	
	public void createReturnLineChart(double[] y1, double[] y2, Date[] x, String first_ticker, String second_ticker, View view) {
		
		TimeSeries first_time_series = new TimeSeries(first_ticker + " % Return");
		TimeSeries second_time_series = new TimeSeries(second_ticker + " % Return");
		
		for (int i = 0; i < x.length; i++) {
			
			first_time_series.add(x[i], y1[i]);
			second_time_series.add(x[i], y2[i]);
			
		}
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(first_time_series);
		dataset.addSeries(second_time_series);
		
		XYSeriesRenderer first_renderer = new XYSeriesRenderer();
		first_renderer.setColor(Color.parseColor("#000066"));
		first_renderer.setFillPoints(true);
		first_renderer.setLineWidth(3f);
		
		XYSeriesRenderer second_renderer = new XYSeriesRenderer();
		second_renderer.setColor(Color.parseColor("#8C001A"));
		second_renderer.setFillPoints(true);
		second_renderer.setLineWidth(3f);
		
		XYMultipleSeriesRenderer mult_renderer = new XYMultipleSeriesRenderer();
		mult_renderer.setLabelsColor(Color.BLACK);
		mult_renderer.setAxesColor(Color.BLACK);
		mult_renderer.setXLabelsColor(Color.BLACK);
		mult_renderer.setYLabelsColor(0,Color.BLACK);
		mult_renderer.addSeriesRenderer(first_renderer);
		mult_renderer.addSeriesRenderer(second_renderer);
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
		double y_min = y1[0];
		double y_max = y1[0];
		for (int i = 0; i < y1.length; i++) {if (y_min > y1[i]) {y_min = y1[i];}}
		for (int i = 0; i < y2.length; i++) {if (y_min > y2[i]) {y_min = y2[i];}}
		for (int i = 0; i < y1.length; i++) {if (y_max < y1[i]) {y_max = y1[i];}}
		for (int i = 0; i < y2.length; i++) {if (y_max < y2[i]) {y_max = y2[i];}}
		if (y_min > 0) {y_min = y_min * 0.95;} else {y_min = y_min * 1.05;}
		if (y_max > 0) {y_max = y_max * 1.05;} else {y_max = y_max * 0.95;}
		double[] limits = {x[x.length-1].getTime(), x[0].getTime(), y_min, y_max};
		mult_renderer.setPanLimits(limits);
		mult_renderer.setZoomLimits(limits);
		
		GraphicalView chartview = ChartFactory.getTimeChartView(getActivity(), dataset, mult_renderer,"dd-MMM-yy");
		LinearLayout linearlayout = (LinearLayout) view.findViewById(R.id.return_line_chart);
		linearlayout.removeAllViews();
		linearlayout.addView(chartview, new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		
	}
	
	public void createSeriesChart(double[] series, double mean, Date[] x, String type, View view) {
		
		//type is the type of series: spread, ratio, residuals, log residuals
		TimeSeries series_time_series = new TimeSeries(type);
		TimeSeries mean_time_series = new TimeSeries("Average");
		
		for (int i = 0; i < x.length; i++) {
			
			series_time_series.add(x[i], series[i]);
			mean_time_series.add(x[i], mean);
			
		}
		
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series_time_series);
		dataset.addSeries(mean_time_series);
		
		XYSeriesRenderer series_renderer = new XYSeriesRenderer();
		series_renderer.setColor(Color.parseColor("#000066")); // may want to try other colours
		series_renderer.setFillPoints(true);
		series_renderer.setLineWidth(3f);
		
		XYSeriesRenderer mean_renderer = new XYSeriesRenderer();
		mean_renderer.setColor(Color.parseColor("#8C001A"));
		mean_renderer.setFillPoints(true);
		mean_renderer.setLineWidth(3f);
		
		XYMultipleSeriesRenderer mult_renderer = new XYMultipleSeriesRenderer();
		mult_renderer.setLabelsColor(Color.BLACK);
		mult_renderer.setAxesColor(Color.BLACK);
		mult_renderer.setXLabelsColor(Color.BLACK);
		mult_renderer.setYLabelsColor(0,Color.BLACK);
		mult_renderer.addSeriesRenderer(series_renderer);
		mult_renderer.addSeriesRenderer(mean_renderer);
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
		double series_min = series[0];
		double series_max = series[0];
		for (int i = 0; i < series.length; i++) {if (series_min > series[i]) {series_min = series[i];}}
		for (int i = 0; i < series.length; i++) {if (series_max < series[i]) {series_max = series[i];}}
		if (series_min > 0) {series_min = series_min * 0.95;} else {series_min = series_min * 1.05;}
		if (series_max > 0) {series_max = series_max * 1.05;} else {series_max = series_max * 0.95;}
		double[] limits = {x[x.length-1].getTime(), x[0].getTime(), series_min, series_max};
		mult_renderer.setPanLimits(limits);
		mult_renderer.setZoomLimits(limits);
		
		GraphicalView chartview = ChartFactory.getTimeChartView(getActivity(), dataset, mult_renderer,"dd-MMM-yy");
		LinearLayout linearlayout = (LinearLayout) view.findViewById(R.id.series_chart);
		linearlayout.removeAllViews();
		linearlayout.addView(chartview, new LayoutParams (LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		
	}
	
}
