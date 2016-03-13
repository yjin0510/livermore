/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.collector;

/**
 * @author yujin
 *
 */
public interface Collector {
	/**
	 * Collect metrics
	 * @return temp file containing collected metrics
	 */
	public String collect() throws Exception;
	
	/**
	 * Set configuration file
	 * @param configFilePath
	 */
	public void setConfig(String configFilePath) throws Exception;
}
