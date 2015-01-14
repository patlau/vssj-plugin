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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hudson.FilePath;
import hudson.scm.SCMRevisionState;

/**
 * SourceSource Revision State implementation.
 * @author patlau
 *
 */
public class VssRevisionState extends SCMRevisionState {

	private Long lastModified;
	private VssJournal log;
	
	/**
	 * Check the SourceSafe log file for new entries for specified projects.
	 * @param journalFile SourceSafe journal file
	 * @param projects SourceSafe projects
	 * @param locale Journal file locale
	 * @return new SourceSafe revision state
	 */
	public static VssRevisionState checkJournal(File journalFile, String[] projects, Locale locale) {
		VssRevisionState state = new VssRevisionState();
		state.readJournal(journalFile, projects, locale);
		return state;
	}
	
	/**
	 * Get last modified date of SourceSafe log.
	 * @return last modified date
	 */
	public Long getLastModified() {
		return lastModified;
	}
	
	/**
	 * Set last modified date of SourceSafe log.
	 * @param date last modified date
	 */
	public void setLastModified(Long date) {
		this.lastModified = date;
	}

	/**
	 * Check if last modified date is newer than baseline.
	 * @param baseline baseline
	 * @return true if last modified is newer than baseline
	 */
	public boolean isNewerThan(VssRevisionState baseline) {
		// Should also check last entries in logfile
		if (baseline == null)
			return true;
		return this.getLastModified() > (baseline.getLastModified());
	}
	
	/**
	 * Check if wait time exceeded.
	 * @param seconds wait time in seconds
	 * @return true if wait time not exceeded
	 */
	public boolean mustWait(Long seconds) {
		Date now = new Date();
		return now.getTime() > (lastModified + (seconds * 1000L));
	}
	
	
	private void readJournal(File journalFile, String[] projects, Locale locale) {
		this.setLastModified(journalFile.lastModified());
		log = new VssJournal(locale);
		log.readJournal(journalFile, projects);
	}

	/**
	 * Write SCM change file.
	 * @param localChangeLogFile local change file
	 * @param lastBuildTime last build time from Jenkins
	 * @throws IOException on write errors
	 * @throws InterruptedException on write errors
	 */
	public void writeChangeFile(FilePath localChangeLogFile,
			Calendar lastBuildTime) throws IOException, InterruptedException {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		try {
			log.writeChangeFile(writer, lastBuildTime);
		} finally {
			writer.close();
		}
		localChangeLogFile.write(sw.getBuffer().toString(), "UTF-8");
	}

}
