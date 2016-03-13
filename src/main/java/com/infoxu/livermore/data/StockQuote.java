/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.data;

/**
 * Stock daily quote information
 * 
 * @author yujin
 * 
 */
public class StockQuote {
	private String ticker = "";
	private double open = 0d;
	private double close = 0d;
	private double high = 0d;
	private double low = 0d;
	private double adjClose = 0d;
	private long volume = 0L;
	private long timeStamp = 0L; // In nanoseconds

	public StockQuote() {

	}

	public StockQuote(String ticker, double open, double close, double high,
			double low, double adjClose, long volume, long timeStamp) {
		this.ticker = ticker;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.adjClose = adjClose;
		this.volume = volume;
		this.timeStamp = timeStamp;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getAdjClose() {
		return adjClose;
	}

	public void setAdjClose(double adjClose) {
		this.adjClose = adjClose;
	}

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
}
