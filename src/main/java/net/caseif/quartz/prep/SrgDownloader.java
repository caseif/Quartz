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
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SrgDownloader {

	static final String MC_VERSION = "1.8";

	public static void main(String... args) {
		boolean overwrite = false;
		String dir = ".";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-o") || args[i].equalsIgnoreCase("--overwrite")) {
				overwrite = true;
			}
			else if (args[i].equalsIgnoreCase("-d") || args[i].equalsIgnoreCase("--dir")) {
				if (args.length >= i) {
					dir = args[i + 1];
				}
			}
		}
		final File file = new File(dir + "/workspace/srg/notch-mcp-" + MC_VERSION + ".zip");
		if (file.exists() && !overwrite) {
			System.out.println("Already have local copy of deobfuscation mappings");
			return;
		}
		Downloader dl = new Downloader("https://www.dropbox.com/s/qfqpti9hmflofdh/notch-mcp-1.8.zip?dl=1",
				"obfuscation mappings");
		dl.download(file);
		try {
			System.out.println("Extracting deobfuscation mappings, please wait...");
			Extractor ex = new Extractor(new ZipFile(file));
			ex.extractAll(new File(dir, "workspace/srg"));
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Failed to extract deobfuscation mappings!");
			System.exit(1);
		}
	}

	private static void unzip(ZipFile zip, ZipEntry entry, File dest) throws IOException {

	}

}
