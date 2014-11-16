package com.bitwiseops.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public final class IoUtils {
	private IoUtils() {
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if(!destFile.exists())
			destFile.createNewFile();

		FileInputStream source = null;
		FileOutputStream dest = null;
		try {
			source = new FileInputStream(sourceFile);
			dest = new FileOutputStream(destFile);
			FileChannel sourceChannel = source.getChannel();
			FileChannel destChannel = dest.getChannel();
			long bytesTransferred = 0;
			long length = sourceFile.length();
			while(bytesTransferred < length) {
				bytesTransferred += destChannel.transferFrom(sourceChannel, 0,
						length - bytesTransferred);
			}
		} finally {
			if(source != null)
				source.close();
			if(dest != null)
				dest.close();
		}
	}

	public static void copyStreams(InputStream is, OutputStream os)
			throws IOException {
		ReadableByteChannel source = null;
		WritableByteChannel dest = null;
		try {
			source = Channels.newChannel(is);
			dest = Channels.newChannel(os);
			copyChannels(source, dest);
		} finally {
			if(source != null)
				source.close();
			if(dest != null)
				dest.close();
		}
	}

	public static void copyChannels(ReadableByteChannel source,
			WritableByteChannel dest) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		while(source.read(buffer) != -1) {
			buffer.flip();// switch to write mode
			dest.write(buffer);
			buffer.compact();// switch to read mode
		}
		buffer.flip();
		while(buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}
}