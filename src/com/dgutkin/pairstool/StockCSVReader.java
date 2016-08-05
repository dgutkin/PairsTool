package com.dgutkin.pairstool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockCSVReader {
	
	public List<Quote> parse(InputStream input_stream) throws IOException {
		
		InputStreamReader in_reader = new InputStreamReader(input_stream);
		BufferedReader b_reader = new BufferedReader(in_reader);
		List<Quote> quote_list = new ArrayList<Quote>();
		
		String line;
		String csv_split = ",";
		line = b_reader.readLine(); //skip the headings
		line = b_reader.readLine();
		
		while(line != null) {
			
			String[] row = line.split(csv_split);
			String date;
			BigDecimal price;
			date = row[0];
			try {price = new BigDecimal(row[6]);} catch (NumberFormatException e) {return null;}
			quote_list.add(new Quote(date, price));
			line = b_reader.readLine();
			
		}
		
		return quote_list;
		
	}
	
	public static class Quote {
		
		public final String date;
		public final BigDecimal price;
		
		Quote(String date, BigDecimal price) {
			
			this.date = date;
			this.price = price;
			
		}
		
	}

}
