/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.storage;

/**
 * @author yujin
 *
 */
public interface StorageClient {
	/**
	 * Submit metrics in the file to TSDB
	 * @param file with metrics in json format
	 */
	public void submit(String file);
}
