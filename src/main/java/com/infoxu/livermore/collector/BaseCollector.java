/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.collector;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.infoxu.livermore.storage.StorageClient;

public abstract class BaseCollector implements Collector {
	private Logger logger = Logger.getLogger(BaseCollector.class);
	
	private String config = null;
	private StorageClient storageClient = null;
	
	public BaseCollector(String config, StorageClient storageClient) {
		this.config = config;
		this.storageClient = storageClient;
	}
	
	/**
	 * A common procedure followed by all collectors
	 * @return
	 */
	public boolean run() {
		try {
			setConfig(config);
			String tmpFile = collect();
			storageClient.submit(tmpFile);
			return true;
		} catch (Exception e) {
			logger.error("Failed running collector", e);
			return false;
		}
	}
	
	/**
	 * Collect metrics
	 */
	public abstract String collect() throws IOException;

	public abstract void setConfig(String configFilePath);
}
