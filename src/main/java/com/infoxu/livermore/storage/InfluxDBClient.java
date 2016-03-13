/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.util.StringUtils;
import org.apache.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.testng.collections.Lists;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.infoxu.livermore.data.Metric;
import com.infoxu.livermore.data.MetricBuilder;

/**
 * @author yujin
 * 
 */
public class InfluxDBClient implements StorageClient {
	private static Logger logger = Logger.getLogger(InfluxDBClient.class);
	
	private static int POINT_BUF = 2000;

	private InfluxDB influxDB;
	private String db;
	
	public InfluxDBClient() {
		this("localhost", 8086, "root", "root", "mydb");
	}

	public InfluxDBClient(String host, int port, String user, String pass,
			String db) {
		this.db = db;

		influxDB = InfluxDBFactory.connect("http://" + host + ":" + port, user,
				pass);
		Pong response = influxDB.ping();
		if (response == null || response.getVersion() == null
				|| response.getVersion().equalsIgnoreCase("unknown")) {
			throw new RuntimeException("Unable to connect to influx DB at http://" 
				+ host + ":" + port + ", response=" + response);
		}
		logger.info("Influx DB connected successfully, version=" + response.getVersion());
		List<String> dbList = influxDB.describeDatabases();
		if (!dbList.contains(db)) {
			logger.warn("Targeting database " + db + " not found, creating one instead!");
			influxDB.createDatabase(db);
		} else {
			logger.info("Targetting database " + db + " found.");
		}
		
		// Flush every 2000 Points, at least every 100ms
		influxDB.enableBatch(POINT_BUF, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Submit a single record
	 * @param record
	 */
	public void submitRecord(Metric metric) {
		Optional<Point> point = metricToInfluxDBPoint(metric);
		System.out.println(point.get());
		if (!point.isPresent()) {
			logger.error("Submit InfluxDB record failed.");
			return;
		}
		this.influxDB.write(db, "default", point.get());
	}
	
	/**
	 * Query DB
	 * @param queryString
	 * @return
	 */
	public QueryResult queryDB(String queryString) {
		Query query = new Query(queryString, db);
		return influxDB.query(query);
	}

	/**
	 * Submit a file of records
	 * Records are in json format and can be reach into Metric object
	 */
	public void submit(String file) {
		File recordFile = new File(file);
		if (!recordFile.exists()) {
			logger.error("Record file " + file + " does not exist!");
			return;
		}
		BufferedReader brin = null;
		String line;
		try {
			brin = new BufferedReader(new FileReader(recordFile));
			List<Point> points = Lists.newArrayList();
			while ((line = brin.readLine()) != null) {
				Metric metric = MetricBuilder.fromJson(line.trim());
				Optional<Point> point = metricToInfluxDBPoint(metric);
				if (point.isPresent()) {
					points.add(point.get());
				}
				if (points.size() % POINT_BUF == POINT_BUF - 1) {
					writeOutPoints(points);
					points.clear();
				}
			}
			writeOutPoints(points);
		} catch (IOException e) {
			logger.error("Error reading record file " + file, e);
		} finally {
			IOUtils.closeQuietly(brin);
		}
	}

	/**
	 * Flush points to InfluxDB
	 * @param points
	 */
	private void writeOutPoints(List<Point> points) {
		BatchPoints batchPoints = BatchPoints
                .database(db)
                .retentionPolicy("default")
                .build();
		for (Point point : points) {
			batchPoints.point(point);
		}
		influxDB.write(batchPoints);
	}

	/**
	 * Translate metric object to influx DB record for insertion
	 * This will return null if conversion failed
	 * 
	 * <measurement>[,<tag-key>=<tag-value>...] <field-key>=<field-value>[,<field2-key>=<field2-value>...] [unix-nano-timestamp]
	 */
	public static Optional<String> metricToInfluxDBRecord(Metric metric) {
		StringBuilder sb = new StringBuilder();
		// 	Measurement
		sb.append(metric.getTable());
		// Tags, if any
		if (metric.getTags().size() > 0) {
			sb.append("," + Joiner.on(",").join(metric.getTags()));
		}
		// Fields should be at least 1
		if (metric.getFields().size() < 1) {
			return Optional.absent();
		}
		sb.append(" " + Joiner.on(",").join(metric.getFields()));
		// Timestamp, optional, assume to be nanoseconds
		long ts = metric.getTimeStamp();
		if (ts == -1) {
			ts = System.currentTimeMillis() * 1000L;
		}
		sb.append(" "  + ts);
		return Optional.of(sb.toString());
	}
	
	/**
	 * Translate metric object to influx DB Point to be inserted
	 * This will return null if conversion failed
	 * @param metric
	 * @return
	 */
	public static Optional<Point> metricToInfluxDBPoint(Metric metric) {
		// Measurement
		Builder builder = Point.measurement(metric.getTable());
		try {
			// Tags, if any
			for (String tag : metric.getTags()) {
				String[] tokens = StringUtils.split(tag, '=');
				builder = builder.tag(tokens[0], tokens[1]);
			}
			// Fields should be at least 1
			if (metric.getFields().size() < 1) {
				return Optional.absent();
			}
			for (String field : metric.getFields()) {
				String[] tokens = StringUtils.split(field, '=');
				builder = builder.field(tokens[0], Double.parseDouble(tokens[1]));
			}
			// Use current time is not set
			long ts = metric.getTimeStamp();
			if (ts == -1) {
				ts = System.currentTimeMillis() * 1000L;
			}
			builder = builder.time(ts, TimeUnit.NANOSECONDS);
		} catch (Exception e) {
			logger.error("Invalid metric: " + metric, e);
			return Optional.absent();
		}
		return Optional.of(builder.build());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new InfluxDBClient();
	}
}
