/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.data;

import java.util.List;

import com.google.common.base.MoreObjects;

/**
 * Read only class for Metric
 * Need to construct via MetricBuilder
 * @author yujin
 *
 */
public class Metric {
	private String table = "";
	private long timeStamp = -1L;
	private List<String> fields = null;
	private List<String> tags = null;
	
	public Metric() {
		
	}
	
	public Metric(String table, long timeStamp, List<String> fields, List<String> tags) {
		this.table = table;
		this.timeStamp = timeStamp;
		this.fields = fields;
		this.tags = tags;
	}

	public String getTable() {
		return table;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public List<String> getFields() {
		return fields;
	}
	
	public List<String> getTags() {
		return tags;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("table", table)
			.add("timeStamp", timeStamp)
			.add("fields", fields)
			.add("tags", tags)
			.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
		result = prime * result + ((table == null) ? 0 : table.hashCode());
		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
		result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metric other = (Metric) obj;
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (table == null) {
			if (other.table != null)
				return false;
		} else if (!table.equals(other.table))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;
		if (timeStamp != other.timeStamp)
			return false;
		return true;
	}
}
