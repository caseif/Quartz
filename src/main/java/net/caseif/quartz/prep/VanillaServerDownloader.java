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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class VanillaServerDownloader {

	private static final String MC_VERSION = "1.8";

	public static void main(String... args) {
		String dir = args.length > 0 ? args[0] : ".";
		final File jar = new File(dir + "/lib/minecraft_server-" + MC_VERSION + ".jar");
		if (jar.exists()) {
			jar.delete();
		}
		System.out.println("Downloading vanilla server binary, please wait...");
		final URL url;
		try {
			url = new URL(
					"https://s3.amazonaws.com/Minecraft.Download/versions/" + MC_VERSION + "/minecraft_server." +
							MC_VERSION + ".jar"
			);
			final int size = getFileSize(url);
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						URLConnection conn = url.openConnection();
						conn.connect();
						ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream());
						jar.setReadable(true, false);
						jar.setWritable(true, false);
						jar.getParentFile().mkdirs();
						jar.createNewFile();
						FileOutputStream os = new FileOutputStream(jar.getCanonicalPath());
						os.getChannel().transferFrom(rbc, 0, size);
						os.close();
					}
					catch (IOException ex) {
						ex.printStackTrace();
						System.err.println("Failed to fetch vanilla server binary!");
						System.exit(1);
					}
				}
			});
			t.start();
			long lastSize = 0L;
			ArrayList<Long> speeds = new ArrayList<Long>();
			final int AVERAGE_PERIOD_IN_SECONDS = 10;
			final int PROGRESS_BAR_SIZE = 30;
			long speed = 0L;
			while (t.isAlive()) {
				long length = jar.length();
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
				String eta = speed == 0L ? "\u221E" : ((size - length) / avgSpeed) + "";
				StringBuilder sb = new StringBuilder();
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
				sb.append(length / 1024).append('/').append(size / 1024).append(" kb ");
				sb.append(speed / 1024).append(" kb/s ");
				sb.append("eta ").append(eta).append("s");
				sb.append("  ");
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
			sb.append(size / 1024).append("/").append(size / 1024).append("kb ");
			sb.append(speed / 1024).append(" kb/s eta 0s");
			System.out.println("\r" + sb.toString());
			System.out.println("Successfully downloaded vanilla server binary!");
		}
		catch (MalformedURLException ex) {
			ex.printStackTrace();
			System.err.println("Failed to fetch vanilla server binary!");
			System.exit(1);
		}
	}

	private static int getFileSize(URL url){
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("HEAD"); // joke's on you if the server doesn't specify
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
