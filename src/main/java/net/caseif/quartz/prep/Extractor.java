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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Extractor {

	private ZipFile zip;

	/**
	 * Creates a new Extractor instance.
	 * @param zip the ZipFile to extract from
	 */
	public Extractor(ZipFile zip) {
		this.zip = zip;
	}

	/**
	 * Extracts the given entry from this extractor's ZipFile.
	 * @param entry the entry to extract
	 * @param dest the file to extract to
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public void extract(ZipEntry entry, File dest) throws IOException {
		System.out.println("Extracting " + entry.getName() + "...");
		if (dest.exists())
			dest.delete();
		dest.getParentFile().mkdirs();
		BufferedInputStream bIs = new BufferedInputStream(zip.getInputStream(entry));
		int b;
		byte buffer[] = new byte[1024];
		FileOutputStream fOs = new FileOutputStream(dest);
		BufferedOutputStream bOs = new BufferedOutputStream(fOs, 1024);
		while ((b = bIs.read(buffer, 0, 1024)) != -1)
			bOs.write(buffer, 0, b);
		bOs.flush();
		bOs.close();
		bIs.close();
	}

	/**
	 * Extracts the entry by the given name from this extractor's ZipFile.
	 * @param entry the name of the entry to extract
	 * @param dest the file to extract to
	 * @throws IOException if the entry is not found or an error occurs while extracting to disk
	 */
	public void extract(String entry, File dest) throws IOException {
		this.extract(zip.getEntry(entry), dest);
	}

	/**
	 * Extracts all entries from this extractor's ZipFile.
	 * @param dest the directory to extract to
	 * @throws IOException if an error occurs while extracting to disk
	 */
	public void extractAll(File dest) throws IOException {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			this.extract(entry, new File(dest, entry.getName()));
		}
	}

}
