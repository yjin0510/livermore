/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import com.google.common.collect.Lists;
import com.infoxu.livermore.data.MetricBuilder;
import com.infoxu.livermore.data.StockQuote;
import com.infoxu.livermore.storage.InfluxDBClient;
import com.infoxu.livermore.storage.StorageClient;

/**
 * Collect Yahoo stock data based on a list of ticker labels
 * @author yujin
 *
 */
public class StockPriceCollector extends BaseCollector {
	private static Logger logger = Logger.getLogger(StockPriceCollector.class);
	
	private int years = 0;
	private List<String> tickers = Lists.newArrayList();
	
	private Options options = new Options();

	public StockPriceCollector(String config, StorageClient storageClient, int backFillYears) {
		super(config, storageClient);
		years = backFillYears; 	// If backFillYears == 0, only fill in the most recent one day of data
								// Otherwise, back fill data of specified # of years
	}
	
	@Override
	public String collect() {
		// Create a temp file to store the collection results
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("stock.price.collector", "metric");
		} catch (IOException e) {
			logger.error("Unable to create temp file " +
					"for storing stock price metrics", e);
			throw new RuntimeException("Unable to create temp file " +
					"for storing stock price metrics", e);
		}
		logger.info("Loaded " + tickers.size() + " stock tickers.");
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(tmpFile);
		
			for (String ticker : tickers) {
					Stock stock = YahooFinance.get(ticker);
					Calendar from = Calendar.getInstance();
					from.setTimeZone(TimeZone.getDefault());
					List<HistoricalQuote> quotes = null;
					if (years <= 0) { // The most recent day
						from.add(Calendar.DATE, -1);
						quotes = stock.getHistory(from, Interval.DAILY);
					} else {
						from.add(Calendar.YEAR, -1 * years);
						quotes = stock.getHistory(from,  Interval.DAILY);
					}
					for (HistoricalQuote hq : quotes) {
						StockQuote quote = new StockQuote(ticker, 
								hq.getOpen().doubleValue(), 
								hq.getClose().doubleValue(), 
								hq.getHigh().doubleValue(),
								hq.getLow().doubleValue(), 
								hq.getAdjClose().doubleValue(),
								hq.getVolume(),
								hq.getDate().getTimeInMillis() * 1000L);
						out.println(MetricBuilder.toJson(MetricBuilder.StockQuoteToMetric(quote)));
					}
			}
		} catch (IOException e) {
			logger.error("Failed to get stock quote for " + tickers, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		logger.info("Stock quote tmp file created: " + tmpFile.getAbsolutePath());
		return tmpFile.getAbsolutePath();
	}

	/**
	 * Read tickers from config file
	 */
	@Override
	public void setConfig(String configFilePath) {
		BufferedReader brin = null;
		try {
			 brin = new BufferedReader(new FileReader(configFilePath));
			 String line = "";
			 while ((line = brin.readLine()) != null) {
				 tickers.add(line.trim());
			 }
		} catch (IOException e) {
			logger.error("Failed to read ticker file: " + configFilePath, e);
			throw new RuntimeException("Failed to read ticker file: " 
					+ configFilePath, e);
		} finally {
			IOUtils.closeQuietly(brin);
		}
	}

	public static void main(String[] args) {
		String tickerFile = "";
		int year = -1;
		if (args.length == 0) {
			logger.error("com.infoxu.livermore.collector.StockPriceCollector tickerFile [year_to_backfill]");
		}
		if (args.length >= 1) {
			tickerFile = args[0];
		}
		if (args.length == 2) {
			year = Integer.parseInt(args[1]);
		}
		BaseCollector collector = new StockPriceCollector(
				tickerFile,
				new InfluxDBClient(), 
				year);
		collector.run();
	}
}
