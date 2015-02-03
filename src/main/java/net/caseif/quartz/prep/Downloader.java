/*
 * Quartz
 * Copyright (c) 2014-2015, Maxim Roncacé <mproncace@gmail.com>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.quartz.prep;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class Downloader {

	private String url;
	private String name;

	public Downloader(final String url, final String name) {
		this.url = url;
		this.name = name;
	}

	/**
	 * Returns the URL associated with this Downloader.
	 * @return the URL associated with this Downloader.
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Returns the "firendly" name associated with this Downloader, used for logging.
	 * @return the "firendly" name associated with this Downloader, used for logging.
	 */
	public String getFriendlyName() {
		return this.name;
	}

	/**
	 * Downloads this Downloader's file to the specified location.
	 * @param dest the local file to download to
	 */
	public void download(final File dest) {
		if (dest.exists()) {
			dest.delete();
		}
		dest.getParentFile().mkdirs();
		System.out.println("Downloading " + name + ", please wait...");
		final URL realUrl;
		try {
			realUrl = new URL(url);
			int tempSize = getFileSize(realUrl);
			final int size = tempSize > 0 ? tempSize : -1;
			final String name = this.name;
			Thread t = new Thread(new Runnable() {
				public void run() {
					FileOutputStream os = null;
					ReadableByteChannel rbc = null;
					try {
						URLConnection conn = realUrl.openConnection();
						if (conn == null) {
							System.err.println("Cannot open connection to " + realUrl.getHost() + "!");
							System.exit(1);
						}
						os = new FileOutputStream(dest.getCanonicalPath());
						conn.connect();
						rbc = Channels.newChannel(conn.getInputStream());
						dest.setReadable(true, false);
						dest.setWritable(true, false);
						dest.getParentFile().mkdirs();
						dest.createNewFile();
						os.getChannel().transferFrom(rbc, 0, size > 0 ? size : Integer.MAX_VALUE);
					}
					catch (IOException ex) {
						ex.printStackTrace();
						System.err.println("Failed to fetch " + name + "!");
						System.exit(1);
					}
					finally {
						try {
							if (os != null) {
								os.close();
							}
							if (rbc != null) {
								rbc.close();
							}
						}
						catch (IOException swallow) {}
					}
				}
			});
			t.start();
			long lastSize = 0L;
			ArrayList<Long> speeds = new ArrayList<Long>();
			final int AVERAGE_PERIOD_IN_SECONDS = 10;
			final int PROGRESS_BAR_SIZE = 30;
			long speed;
			int lineLength = 0;
			while (t.isAlive()) {
				long length = dest.length();
				int percent = (int)Math.floor(length / (double)size * 100);
				speed = length - lastSize;
				if (speeds.size() > AVERAGE_PERIOD_IN_SECONDS)
					speeds.remove(0);
				speeds.add(speed);
				long sum = 0L;
				for (Long l : speeds) {
					sum += l;
				}
				long avgSpeed = sum / speeds.size();
				lastSize = length;
				String eta = "\u221E";
				if (speed > 0L) {
					long etaNum = (size - length) / avgSpeed;
					long seconds = etaNum % 60;
					long minutes = etaNum / 60;
					StringBuilder etaSb = new StringBuilder();
					if (minutes > 0) {
						etaSb.append(minutes).append("m");
					}
					etaSb.append(seconds).append("s");
					eta = etaSb.toString();
				}
				StringBuilder sb = new StringBuilder();
				if (size >= 0) {
					sb.append(percent).append("% ");
					sb.append("[");
					boolean end = false;
					for (int i = 1; i <= PROGRESS_BAR_SIZE; i++) {
						if (!end && percent > (int)Math.floor(i / (double)PROGRESS_BAR_SIZE * 100)) {
							if (percent > (int)Math.floor((i + 1) / (double)PROGRESS_BAR_SIZE * 100)) {
								sb.append('=');
							}
							else {
								sb.append('>');
								end = true;
							}
						}
						else {
							sb.append(' ');
						}
					}
					sb.append("] ");
				}
				sb.append(length / 1024).append('/').append(size / (size < 0 ? 1 : 1024)).append(" kb ");
				sb.append(speed / 1024).append(" kb/s");
				if (size >= 0) {
					sb.append(" eta ").append(eta);
					sb.append("    ");
				}
				lineLength = sb.length();
				System.out.print("\r" + sb.toString());
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException swallow) {}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("100% [");
			for (int i = 0; i < PROGRESS_BAR_SIZE; i++) {
				sb.append('=');
			}
			sb.append("] ");
			sb.append((size < 0 ? dest.length() : size) / 1024).append("/").append(size / (size < 0 ? 1 : 1024)).append("kb");
			for (int i = sb.length(); i <= lineLength; i++) {
				sb.append(' ');
			}
			System.out.println("\r" + sb.toString());
			System.out.println("Successfully downloaded " + this.name + "!");
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Failed to fetch " + this.name + "!");
			System.exit(1);
		}
	}

	/**
	 * Retrieves the size of a file on a remote server.
	 * @param url the URL of the file to query
	 * @return the size in bytes of the file
	 */
	private static int getFileSize(URL url){
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("HEAD");
			conn.getInputStream();
			return conn.getContentLength();
		}
		catch (Exception e){
			return -1;
		}
		finally {
			if (conn != null)
				conn.disconnect();
		}
	}

}
