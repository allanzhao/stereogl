package com.bitwiseops.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public final class Resources {
	private Resources() {
	}

	public static URL getResource(String path) {
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

	public static String getResourceAsString(String path) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(path);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IoUtils.copyStreams(is, baos);
		return baos.toString();
	}

	public static InputStream getResourceAsStream(String path)
			throws IOException {
		return Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(path);
	}

	public static void copyResourceToFile(String path, File destFile)
			throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(path);
			os = new FileOutputStream(destFile);
			IoUtils.copyStreams(is, os);
		} finally {
			if(is != null) is.close();
			if(os != null) os.close();
		}
	}
}