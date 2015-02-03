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
package net.caseif.quartz;

import net.caseif.quartz.prep.SrgDownloader;
import net.caseif.quartz.prep.VanillaServerDownloader;
import net.caseif.quartz.util.QuartzUtil;
import nl.hardijzer.fw.applysrg.ApplySrg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Main {

	public static final String MC_VERSION = "1.8";

	public static final Logger log;

	static {
		log = LoggerFactory.getLogger(Main.class);
	}

	public static void main(String[] args) {
		VanillaServerDownloader.main();
		SrgDownloader.main();
		File deobf = new File("lib/minecraft_server-deobf-" + MC_VERSION + ".jar");
		if (deobf.exists()) {
			try {
				ApplySrg.main(new String[]{
						"--srg", "workspace/srg/notch-mcp.srg",
						"--in", "lib/minecraft_server-$mcVersion.jar",
						"--inheritance", "lib/minecraft_server-$mcVersion.jar",
						"--out", "lib/minecraft_server-deobf-$mcVersion.jar"
				});
			}
			catch (IOException ex) {
				ex.printStackTrace();
				System.err.println("Failed to deobfuscate vanilla server!");
				System.exit(1);
			}
			QuartzUtil.restart();
		}
	}

}
