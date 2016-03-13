package com.infoxu.livermore.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;

public class MetricBuilderTest {
	@Test
	public void testBuilder() {
		Metric m = new MetricBuilder()
			.setTable("stock")
			.setTimeStamp(1L)
			.addField("f1=v1")
			.addField("f2=v2")
			.addTag("t1=v3")
			.addTag("t2=v4")
			.addTag("t3=v5")
			.build();
		
		Assert.assertEquals(m.getTable(), "stock");
		Assert.assertEquals(m.getTimeStamp(), 1L);
		Assert.assertEquals(m.getFields().size(), 2);
		Assert.assertEquals(m.getTags().size(), 3);
		
		// Gson serialization/deserialization test
		String json = MetricBuilder.toJson(m);
		Metric m2 = MetricBuilder.fromJson(json);
		System.out.println(json);
		Assert.assertEquals(m, m2);
	}
}
