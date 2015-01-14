/**
 * The MIT License
 * Copyright (c) 2015 Patrick Lauper
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
package org.jenkinsci.plugins.vssj;

import java.io.File;
import java.io.IOException;

/**
 * Windows utility methods. 
 * 
 * Prefered way of mapping network drives is in "init.groovy", e.g.
 *   def mapdrive = "net use N: \\\\SERVER\\PATH\"
 *   mapdrive.execute()
 * 
 * @author patlau
 *
 */
public final class WinUtils {
	
	private WinUtils() {
	}

	/**
	 * Map a network path to drive letter. Using a user/password is currently not implemented.
	 * If you can't map a drive using init.groovy, try to run Jenkins under a user which can
	 * connect to the network drive.
	 * 
	 * @param map drive letter
	 * @param path UNC network path
	 */
	public static void mapNetworkDrive(String map, String path) {
	    try {
	        String command = "C:\\Windows\\system32\\net.exe use " + map + " " + path; 
	        	// + " /user:" + user + " " + password;
	        Runtime.getRuntime().exec(command);	   
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}

	/**
	 * Check if a file points to a network drive and try to map the drive 
	 * to the default network path.
	 * @param f absolute filename
	 */
	public static void checkForUnmappedNetworkDrive(File f) {
		WinUtils.checkForUnmappedNetworkDrive(f, getDefaultMapNetDrive(null));
	}

	/**
	 * Check if a file points to a network drive and try to map the drive.
	 * 
	 * Currently simply checks if the file is not on drive C:. If it is not,
	 * it tries to map the given path to the drive letter of the file.
	 * 
	 * @param f File to check
	 * @param mappedPath network path to map
	 */
	public static void checkForUnmappedNetworkDrive(File f, String mappedPath) {
		if (f == null || f.exists())
			return;
		String drive = f.getAbsolutePath().substring(0, 2);
		if (drive.equals("C:"))
			return;
		if (mappedPath != null) {
			mapNetworkDrive(drive, mappedPath);
		}
	}

	/**
	 * Get the default network path.
	 * @param env Value from env.
	 * @return
	 */
	public static String getDefaultMapNetDrive(String env) {
		if (env != null && !env.trim().isEmpty())
			return env;
		String path = System.getenv("VSS_HOME");
		if (path == null || path.trim().isEmpty()) {
			path = "C:\\VSS";
		}
		return path;
	}
	
}
