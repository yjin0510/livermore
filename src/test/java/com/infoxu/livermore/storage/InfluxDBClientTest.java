package com.infoxu.livermore.storage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.influxdb.dto.QueryResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.infoxu.livermore.data.Metric;
import com.infoxu.livermore.data.MetricBuilder;

public class InfluxDBClientTest {

	@Test
	public void metricToInfluxDBRecord() {
		Metric m = new MetricBuilder()
		.setTable("stock")
		.setTimeStamp(2L)
		.addTag("t1=v3")
		.addTag("t2=v4")
		.addTag("t3=v5")
		.build();
		
		Optional<String> record = InfluxDBClient.metricToInfluxDBRecord(m);
		Assert.assertFalse(record.isPresent());
		
		Metric m2 = new MetricBuilder()
		.setTable("stock")
		.setTimeStamp(2L)
		.addField("f1=v1")
		.addField("f2=v2")
		.addTag("t1=v3")
		.addTag("t2=v4")
		.addTag("t3=v5")
		.build();
		
		record = InfluxDBClient.metricToInfluxDBRecord(m2);
		Assert.assertTrue(record.isPresent());
		
		Assert.assertEquals(record.get(), "stock,t1=v3,t2=v4,t3=v5 f1=v1,f2=v2 2");
	}
	
	@Test
	public void testSingleSubmit() {
		InfluxDBClient client = new InfluxDBClient();
		Metric m = new MetricBuilder()
		.setTable("stock")
		.setTimeStamp(2L)
		.addField("f1=10")
		.addField("f2=20")
		.addTag("t1=v3")
		.addTag("t2=v4")
		.addTag("t3=v5")
		.build();
		
		client.submitRecord(m);
		QueryResult qr = client.queryDB("SELECT * FROM stock where t1=\'v3\'");
		Assert.assertEquals(qr.getResults().get(0).getSeries().size(), 1);
		Assert.assertEquals(qr.getResults().get(0).getSeries().get(0).getName(), "stock");
	}
	
	@Test
	public void testBatchSubmit() throws IOException {
		InfluxDBClient client = new InfluxDBClient();
		Metric m = new MetricBuilder()
		.setTable("test1")
		.setTimeStamp(1L)
		.addField("key=10")
		.addTag("tag=v")
		.build();
		
		Metric m2 = new MetricBuilder()
		.setTable("test1")
		.setTimeStamp(2L)
		.addField("key=20")
		.addTag("tag=v")
		.build();
		
		File tmpFile = File.createTempFile("metric", "tmpfile");
		PrintWriter out = new PrintWriter(tmpFile);
		out.println(MetricBuilder.toJson(m));
		out.println(MetricBuilder.toJson(m2));
		IOUtils.closeQuietly(out);
		
		client.submit(tmpFile.getAbsolutePath());
		
		QueryResult qr = client.queryDB("SELECT * FROM test1");
		Assert.assertEquals(qr.getResults().get(0).getSeries().size(), 1);
		Assert.assertEquals(qr.getResults().get(0).getSeries().get(0).getName(), "test1");
	}
}
