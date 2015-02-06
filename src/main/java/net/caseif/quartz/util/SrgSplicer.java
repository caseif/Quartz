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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Remnoves the stupid base package remapping included in SRG mappings.
 */
public class SrgSplicer {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Too few arguments! Usage: java -jar SrgTweaker.jar [srg container] [output file]");
			return;
		}
		String srgDir = args[0];
		String output = args[1];
		File f = new File(srgDir);
		File o = new File(output);
		if (!f.exists()) {
			System.err.println("Cannot find file " + srgDir + "!");
			return;
		}
		if (!f.isDirectory()) {
			System.err.println(srgDir + " is not a directory!");
			return;
		}
		try {
			tweakSrg(f, o);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("An error occurred while tweaking the SRG file");
		}
	}

	public static void tweakSrg(File srg, File output) throws IOException {
		List<String> toSplice = new ArrayList<String>();
		HashMap<String, String[]> fieldDescs = new HashMap<String, String[]>();
		HashMap<String, String[]> methodDescs = new HashMap<String, String[]>();
		System.out.println("Splicing SRG mappings, please wait...");
		BufferedReader r = null;
		try {
			File joined = new File(srg, "joined.srg");
			r = new BufferedReader(new FileReader(joined));
			String line;
			while ((line = r.readLine()) != null) {
				if (!line.startsWith("#")) {
					String[] expl = line.split("\\s");
					if (expl.length >= 3) {
						String type = expl[0];
						if (type.equals("PK:")) {
							if (!expl[1].equals(".")) {
								toSplice.add(line);
							}
						}
						else if (type.equals("CL:")) {
							toSplice.add(line);
						}
						else if (type.equals("FD:")) {
							String notchName = expl[1];
							String srgDesc = expl[2];
							String[] descExpl = srgDesc.split("/");
							String srgName = descExpl[descExpl.length - 1];
							fieldDescs.put(srgName, new String[]{notchName, srgDesc});
						}
						else if (type.equals("MD:")) {
							if (expl.length >= 5) {
								String notchName = expl[1];
								String srgDesc = expl[3];
								String[] descExpl = srgDesc.split("/");
								String srgName = descExpl[descExpl.length - 1];
								String returnType = expl[4];
								methodDescs.put(srgName, new String[]{notchName, srgDesc, returnType});
							}
						}
					}
				}
			}
			r.close();
			File fields = new File(srg, "fields.csv");
			if (fields.exists() && fields.isFile()) {
				r = new BufferedReader(new FileReader(fields));
				while ((line = r.readLine()) != null) {
					if (!line.startsWith("#")) {
						String[] expl = line.split(",");
						if (expl.length >= 2) {
							String srgName = expl[0];
							String mcpName = expl[1];
							String[] desc = fieldDescs.get(srgName);
							if (desc != null) {
								String newDesc = "FD: " + desc[0] + " " + desc[1].replace(srgName, mcpName);
								toSplice.add(newDesc);
								fieldDescs.remove(srgName);
							}
						}
					}
				}
			}
			else {
				System.err.println("Could not find field mappings!");
				return;
			}
			r.close();
			// add fields that don't have friendly names
			for (String[] s : fieldDescs.values()) {
				toSplice.add("FD: " + s[0] + " " + s[1]);
			}
			File methods = new File(srg, "methods.csv");
			if (methods.exists() && methods.isFile()) {
				r = new BufferedReader(new FileReader(methods));
				while ((line = r.readLine()) != null) {
					if (!line.startsWith("#")) {
						String[] expl = line.split(",");
						if (expl.length >= 2) {
							String srgName = expl[0];
							String mcpName = expl[1];
							String[] desc = methodDescs.get(srgName);
							if (desc != null) {
								String returnType = desc[2];
								String newDesc = "MD: " + desc[0] + " " + returnType + " " +
										desc[1].replace(srgName, mcpName) + " " + returnType;
								toSplice.add(newDesc);
								methodDescs.remove(srgName);
							}
						}
					}
				}
			}
			else {
				System.err.println("Could not find method mappings!");
				return;
			}
			r.close();
			// add methods that don't have friendly names
			for (String[] s : methodDescs.values()) {
				toSplice.add("MD: " + s[0] + " " + s[2] + " " + s[1] + " " + s[2]);
			}
			File temp = new File(output.getParentFile(), output.getName() + ".splice");
			FileWriter w = new FileWriter(temp);
			for (String s : toSplice) {
				w.write(s + "\n");
			}
			w.flush();
			w.close();
			output.delete();
			temp.renameTo(output);
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.err.println("Failed to read SRG mappings!");
		}
		finally {
			if (r != null) {
				try {
					r.close();
				}
				catch (IOException swallow) {} // meh
			}
		}
	}

}
