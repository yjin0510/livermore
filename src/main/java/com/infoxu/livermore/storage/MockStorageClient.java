/**
 * @Copyright 2016 Infoxu.com
 */
package com.infoxu.livermore.storage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

/**
 * Mock StorageClient that print result file to stdout
 * @author yujin
 *
 */
public final class MockStorageClient implements StorageClient {
	public void submit(String file) {
		BufferedReader brin = null;
		try {
			brin = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = brin.readLine()) != null) {
				System.out.println(line.trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(brin);
		}
	}
}
