/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.data;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

/**
 * Builder class for Metric
 * @author yujin
 *
 */
public class MetricBuilder {
	private String table = "";
	private long timeStamp = -1L;
	private List<String> fields = Lists.newArrayList();
	private List<String> tags = Lists.newArrayList();
	
	public MetricBuilder(){
		
	}
	
	public MetricBuilder setTable(String table) {
		this.table = table;
		return this;
	}
	
	public MetricBuilder setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
		return this;
	}
	
	/**
	 * Add field key=value
	 * @param field
	 */
	public MetricBuilder addField(String field) {
		this.fields.add(field);
		return this;
	}
	
	/**
	 * Add tag key=value
	 * @param tag
	 */
	public MetricBuilder addTag(String tag) {
		this.tags.add(tag);
		return this;
	}
	
	public Metric build() {
		return new Metric(table, timeStamp, fields, tags);
	}
	
	// To Json and Back
	public static String toJson(Metric m) {
		Gson gson = new Gson();
		return gson.toJson(m);
	}
	
	public static Metric fromJson(String json) {
		Gson gson = new Gson();
		return gson.fromJson(json, Metric.class);
	}
	
	/**
	 * Convert a stock quote to metric
	 * @param q
	 * @return
	 */
	public static Metric StockQuoteToMetric(StockQuote q) {
		return new MetricBuilder()
			.setTable("stock")
			.setTimeStamp(q.getTimeStamp())
			.addTag("symbol=" + q.getTicker())
			.addField("open=" + q.getOpen())
			.addField("close=" + q.getClose())
			.addField("high=" + q.getHigh())
			.addField("low=" + q.getLow())
			.addField("adj_close=" + q.getAdjClose())
			.addField("volume=" + q.getVolume())
			.build();
	}
}
