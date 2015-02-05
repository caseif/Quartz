/*
 * Quartz
 * Copyright (c) 2015, Maxim Roncacé <mproncace@gmail.com>
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
package net.caseif.quartz.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Extractor {

	public static void main(String[] args) {
		String zipPath = null;
		String destPath = null;
		String entry = null;
		boolean overwrite = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-i") || args[i].equalsIgnoreCase("--input")) {
				if (args.length >= i) {
					zipPath = args[i + 1];
					i += 1;
				}
				else {
					System.err.println("Invalid value for flag " + args[i]);
					return;
				}
			}
			else if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("--output")) {
				if (args.length >= i) {
					destPath = args[i + 1];
					i += 1;
				}
				else {
					System.err.println("Invalid value for flag " + args[i]);
					return;
				}
			}
			else if (args[i].equalsIgnoreCase("-e") || args[i].equalsIgnoreCase("--entry")) {
				if (args.length >= i) {
					entry = args[i + 1];
					i += 1;
				}
				else {
					System.err.println("Invalid value for flag " + args[i]);
					return;
				}
			}
			else if (args[i].equalsIgnoreCase("-ow") || args[i].equalsIgnoreCase("--overwrite")) {
				overwrite = true;
			}
		}
		if (zipPath == null) {
			System.err.println("Missing required parameter --url");
			return;
		}
		if (destPath == null) {
			System.err.println("Missing required parameter --dest-file");
			return;
		}
		File zip = new File(zipPath);
		File dest = new File(destPath);
		try {
			if (entry == null) {
				extractAll(new ZipFile(zip), dest, overwrite);
			}
			else {
				extract(new ZipFile(zip), entry, dest, overwrite);
			}
		}
		catch (MalformedURLException ex) {
			System.err.println("Failed to parse URL!");
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("An exception occurred while downloading the file.");
		}
	}

	/**
	 * Extracts the given entry from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param entry the entry to extract
	 * @param dest the file to extract to
	 * @param overwrite whether existing files should be overwritten
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public static void extract(ZipFile zip, ZipEntry entry, File dest, boolean overwrite) throws IOException {
		if (entry.isDirectory()) {
			if (dest.exists()) {
				if (overwrite) {
					dest.delete();
					dest.mkdirs();
				}
				else {
					System.err.println("Failed to extract " + entry.getName() + " - directory already exists");
					return;
				}
			}
			else {
				dest.mkdirs();
			}
			return;
		}
		System.out.println("Extracting " + entry.getName() + "...");
		if (dest.exists() && !overwrite) {
			System.err.println("Failed to extract " + entry.getName() + " - file already exists");
			return;
		}
		File temp = new File(dest.getParentFile(), dest.getName() + ".extract");
		temp.getParentFile().mkdirs();
		temp.createNewFile();
		BufferedInputStream bIs = new BufferedInputStream(zip.getInputStream(entry));
		int b;
		byte buffer[] = new byte[1024];
		FileOutputStream fOs = new FileOutputStream(temp);
		BufferedOutputStream bOs = new BufferedOutputStream(fOs, 1024);
		while ((b = bIs.read(buffer, 0, 1024)) != -1)
			bOs.write(buffer, 0, b);
		bOs.flush();
		bOs.close();
		bIs.close();
		dest.delete();
		temp.renameTo(dest);
	}

	/**
	 * Extracts the entry by the given name from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param entry the entry to extract
	 * @param dest the file to extract to
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public static void extract(ZipFile zip, ZipEntry entry, File dest) throws IOException {
		extract(zip, entry, dest, false);
	}

	/**
	 * Extracts the entry by the given name from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param entry the name of the entry to extract
	 * @param dest the file to extract to
	 * @param overwrite whether existing files should be overwritten
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public static void extract(ZipFile zip, String entry, File dest, boolean overwrite) throws IOException {
		extract(zip, zip.getEntry(entry), dest, overwrite);
	}

	/**
	 * Extracts the entry by the given name from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param entry the name of the entry to extract
	 * @param dest the file to extract to
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public static void extract(ZipFile zip, String entry, File dest) throws IOException {
		extract(zip, entry, dest, false);
	}

	/**
	 * Extracts all entries from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param dest the directory to extract to
	 * @param overwrite whether existing files should be overwritten
	 * @throws IOException if an error occurs while extracting to disk
	 */
	public static void extractAll(ZipFile zip, File dest, boolean overwrite) throws IOException {
		if (dest.isFile()) {
			if (overwrite) {
				if (!dest.delete()) {
					throw new IOException("Failed to delete existing file at " + dest.getPath());
				}
			}
			else {
				throw new IOException("Failed to extract to " + dest.getPath() + " - already exists as regular file");
			}
		}
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			try {
				extract(zip, entry, new File(dest, entry.getName()), overwrite);
			}
			catch (IOException ex) {
				ex.printStackTrace();
				System.err.println("Failed to extract " + entry.getName());
			}
		}
	}

	/**
	 * Extracts all entries from this extractor's ZipFile.
	 * @param zip the ZIP archive to extract from
	 * @param dest the directory to extract to
	 * @throws IOException if an error occurs while extracting to disk
	 */
	public static void extractAll(ZipFile zip, File dest) throws IOException {
		extractAll(zip, dest, false);
	}

}
