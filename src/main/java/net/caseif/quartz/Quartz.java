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
package net.caseif.quartz;

import net.caseif.quartz.util.Downloader;
import net.caseif.quartz.util.Extractor;
import net.caseif.quartz.util.SrgTweaker;
import net.minecraft.server.MinecraftServer;
import nl.hardijzer.fw.applysrg.ApplySrg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipFile;

public class Quartz {

	public static final String MC_VERSION = "1.8";

	public static final Logger log;

	static {
		log = LoggerFactory.getLogger(Quartz.class);
	}

	public static void main(String[] args) {
		fetchResources(false);
		ArrayList<String> argList = new ArrayList<String>();
		argList.addAll(Arrays.asList(args));
		if (!argList.contains("gui")) {
			argList.add("nogui");
		}
		MinecraftServer.main(argList.toArray(new String[argList.size()]));
	}

	private static void fetchResources(boolean isReattempt) {
		File deobf = new File("lib", "minecraft_server-deobf-" + MC_VERSION + ".jar");
		if (!deobf.exists()) {
			try {
				//TODO: use checksums or something similar
				File vanilla = new File("lib", "minecraft_server-" + MC_VERSION + ".jar");
				if (!vanilla.exists()) {
					Downloader.download(new URL("https://s3.amazonaws.com/Minecraft.Download/versions/" + MC_VERSION +
							"/minecraft_server." + MC_VERSION + ".jar"), vanilla, "vanilla server");
				}
				File srg = new File("lib" + File.separator + "srg", "joined.srg");
				if (!srg.exists()) {
					File mcp = new File("lib" + File.separator + "mcp", "mcp.zip");
					if (!mcp.exists()) {
						Downloader.download(new URL(
								"http://download1147.mediafire.com/056n9q38z1ag/56xoalz89957n7o/mcp910-pre1.zip"
						), mcp, "MCP");
						Extractor.extract(new ZipFile(mcp), "conf/joined.srg", srg, false);
						SrgTweaker.tweakSrg(srg);
					}
				}
				try {
					ApplySrg.main(new String[]{
							"--srg", "./lib/srg/joined.srg",
							"--in", "./lib/minecraft_server-" + MC_VERSION + ".jar",
							"--inheritance", "./lib/minecraft_server-" + MC_VERSION + ".jar",
							"--out", "./lib/minecraft_server-deobf-" + MC_VERSION + ".jar"
					});
					log.info("Finished downloading resources! You may now relaunch the server.");
					System.exit(0);
				}
				catch (IOException ex) {
					ex.printStackTrace();
					log.error("Failed to deobfuscate vanilla server!");
					System.exit(1);
				}
			}
			catch (IOException ex) {
				ex.printStackTrace();
				log.error("Failed to download required libraries!");
				System.exit(1);
			}
		}
	}
}
