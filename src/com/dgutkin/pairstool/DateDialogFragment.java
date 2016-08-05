package com.dgutkin.pairstool;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.widget.DatePicker;


public class DateDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
	
	boolean start;
	
	public interface DateListener {
		// some method from activity to be handled in activity
		public void onStartDateSelected(String date, boolean start);
	}
	
	DateListener listener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {listener = (DateListener) activity;}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + 
					"must implement OnDateSelectedListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Bundle bundle = this.getArguments();
		Long date_previous = bundle.getLong("DATE_PREVIOUS");
		start = bundle.getBoolean("START");
		
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date_previous);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		
		DatePickerDialog date_dialog = new DatePickerDialog(getActivity(),this,year,month,day);
		date_dialog.setTitle("");
		return date_dialog;
	}
	
	public void onDateSet(DatePicker view,int year,int month,int day) {
		SimpleDateFormat dateformatin = new SimpleDateFormat("dd-MM-yyyy",Locale.CANADA);
		SimpleDateFormat dateformatout = new SimpleDateFormat("dd - MMM - yyyy",Locale.CANADA);
		String date = Integer.toString(day) + "-" + 
				Integer.toString(month+1) + "-" + Integer.toString(year);
		try {
			Date temp = dateformatin.parse(date);
			date = dateformatout.format(temp);
		} catch (ParseException e) {
			// nothing
		}
		listener.onStartDateSelected(date, start);
		
	}

}
