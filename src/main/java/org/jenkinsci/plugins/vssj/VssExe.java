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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Class to run SourceSafe ss.exe.
 * @author patlau
 */
public class VssExe {
	
	boolean useSubdirs;
	String ssexe;
	Launcher launcher;
	FilePath workspace;
	TaskListener listener;
	
	/**
	 * Constructor for SourceSafeExe.
	 * @param launcher Jenkins launcher
	 * @param workspace Jenkins workspace
	 * @param listener Jenkins task listener
	 * @param pathToSSexe Path to ss.exe
	 */
	public VssExe(Launcher launcher, FilePath workspace, TaskListener listener, String pathToSSexe, boolean useSubdirs) {
		this.ssexe = pathToSSexe;
		this.launcher = launcher;
		this.workspace = workspace;
		this.listener = listener;
		this.useSubdirs = useSubdirs;
	}
	
	private String getVssProjectDirectory(String p) {
		if (useSubdirs)
			return workspace + File.separator + p.substring(2);
		else
			return workspace + "";
	}
	
	/**
	 * Clean a project directory.
	 * @param project SourceSafe project
	 * @throws IOException if clean failed
	 */
	public void clean(String project) throws IOException {
		File dir = new File(getVssProjectDirectory(project));
    	if (dir.exists()) {
    		listener.getLogger().println("Clean Directory: " + dir.getAbsolutePath());
    	    FileUtils.cleanDirectory(dir);
    	}
    	dir.mkdirs();
	}
	
	/**
	 * Get SourceSafe files.
	 * @param project SourceSafe project
	 * @param paramString SourceSafe ss.exe parameters
	 * @return ss.exe return value
	 */
	public int get(String project, String paramString) {
		
		File dir = new File(getVssProjectDirectory(project));
		if (!dir.exists())
			dir.mkdirs();
		
		// %SSEXE% get $/Project -R -GWR -I-Y
		List<String> params = new ArrayList<String>();
		
		//SS.EXE Parameter
		params.add("\"" + ssexe + "\"");
		params.add("GET");
		params.add(project);
		params.add(paramString);
		
		String command = StringUtils.join(params, " ");
		
		try {
			return runCommand(command, dir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
	}
	
    int runCommand(String command, File pwd) throws IOException, InterruptedException {
        if (launcher == null) {
            launcher = new Launcher.LocalLauncher(listener);
        }
        
        WinUtils.checkForUnmappedNetworkDrive(new File(ssexe));

//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        listener.getLogger().println("Get Directory: " + pwd.getAbsolutePath());
        listener.getLogger().println("Get Command: " + command);
		
        ProcStarter procStarter = launcher.launch()
        		.pwd(pwd)
        		.cmdAsSingleString("C:\\Windows\\System32\\CMD.EXE /Q /C " + command)
        		.stderr(errorStream)
        		.stdout(listener);
        
//        Proc proc = procStarter.start();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getStdout()));
//        String s = reader.readLine();
//        while(s != null) {
//        	listener.getLogger().println(s);
//        	s = reader.readLine();
//        }
        
        int rc = procStarter.join();
        if (rc != 0) {
            listener.getLogger().println("Error running command: " + errorStream.toString());
            throw new RuntimeException(errorStream.toString());
        }

        return rc;
    }
	
}
