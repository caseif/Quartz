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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Remnoves the stupid base package remapping included in SRG mappings.
 */
public class SrgTweaker {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Too few arguments! Usage: java -jar SrgTweaker.jar [srg file]");
			return;
		}
		String srg = args[0];
		File f = new File(srg);
		if (!f.exists()) {
			System.err.println("Cannot find file " + srg + "!");
			return;
		}
		try {
			tweakSrg(f);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("An error occurred while tweaking the SRG file");
		}
	}

	public static void tweakSrg(File srg) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader r = new BufferedReader(new FileReader(srg));
		String line;
		System.out.println("Tweaking SRG mapping, please wait...");
		while ((line = r.readLine()) != null) {
			if (line.startsWith("PK")) {
				String[] exploded = line.split("\\s");
				if (exploded.length >= 2) {
					if (exploded[1].equals(".")) {
						continue;
					}
				}
			}
			sb.append(line).append("\n");
		}
		r.close();
		File temp = new File(srg.getParentFile(), srg.getName() + ".tweak");
		FileWriter w = new FileWriter(temp);
		w.write(sb.toString());
		w.close();
		srg.delete();
		temp.renameTo(srg);
	}

}
