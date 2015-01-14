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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * SourceSafe journal file parser.
 * @author patlau
 *
 */
public class VssJournal {

	private Calendar lastModified;
	private List<VssJournalEntry> entries = new ArrayList<VssJournalEntry>();
	private Locale locale;
	
	public VssJournal() {
		this(Locale.getDefault());
	}
	
	public VssJournal(Locale locale) {
		this.locale = locale;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified.setTime(lastModified);
	}
	
	public List<VssJournalEntry> getEntries() {
		return entries;
	}
	
	public VssJournalEntry getLastChange() {
		return getEntries().get(getEntries().size() - 1);
	}
	
	
	// Must first call readFile(logfile)
	public void writeChangeFile(File file, Calendar fromDate) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		try {
			writeChangeFile(writer, fromDate);
		} finally {
			writer.close();
		}
	}
	
	public void writeChangeFile(PrintWriter writer, Calendar fromDate) {
		writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.println("<log>");
		for (VssJournalEntry entry : getNewEntries(fromDate)) {
		    writer.println("\t<change>");
		    writer.println(String.format("\t\t<version>%s</version>", entry.getVersion()));
		    writer.println(String.format("\t\t<date>%s</date>", entry.getDatetimeString()));
		    writer.println(String.format("\t\t<user>%s</user>", entry.getUser()));
		    writer.println(String.format("\t\t<action>%s</action>", StringEscapeUtils.escapeXml(entry.getAction())));
		    writer.println(String.format("\t\t<filename>%s</filename>", entry.getFilename()));
		    if (entry.getComment() != null && !entry.getComment().isEmpty())
		    	writer.println(String.format("\t\t<comment>%s</comment>", StringEscapeUtils.escapeXml(entry.getComment())));
		    writer.println("\t</change>");
		}
		writer.println("</log>");
	}


	public List<VssJournalEntry> getNewEntries(Calendar fromDate) {
		List<VssJournalEntry> newEntries = new ArrayList<VssJournalEntry>();
		for (VssJournalEntry e : getEntries()) {
			
			//System.out.println(e.getDatetimeString() + " - " + SimpleDateFormat.getDateTimeInstance().format(lastModified.getTime()) + " => " + (e.getDatetime().before(lastModified)));
			//System.out.println(e.getDatetime().getTime().getTime() + " - " + lastModified.getTime().getTime() + " => " + (e.getDatetime().getTime().after(lastModified.getTime())));
			
			if (e.getDatetime().after(fromDate)) {
				newEntries.add(e);
			}
		}
		return newEntries;
	}
	
	/**
	 * Check if Filename is in Project by comparing if Filename starts with one of the
	 * project names. Project names should start with $/
	 * @param filename
	 * @param list of projects 
	 * @return true if filename is in project, otherwise false
	 */
	protected boolean isFilenameInProject(String filename, String[] projects) {
		if (projects == null)
			return true;
		for (String project : projects) {
			String fn = FilenameUtils.normalize(filename, true);
			String proj = FilenameUtils.normalize(project, true);
			if (fn.trim().startsWith(proj.trim()))
				return true;
		}
		return false;
	}


	public void readJournal(File journalFile) {
		readJournal(journalFile, null);
	}
	
	public VssJournal readJournal(File journalFile, String[] projects) {
		
		lastModified = Calendar.getInstance();
		lastModified.setTime(new Date(journalFile.lastModified()));
		
		ResourceBundle vssJournalResourceBundle = ResourceBundle.getBundle("vss-journal", locale);
		
		entries.clear();
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(journalFile));
			String line = reader.readLine();
			while (line != null) {

				VssJournalEntry entry = new VssJournalEntry();
				
				while (line != null && !line.isEmpty()) {
					
					if (entry.getFilename() == null) {
						entry.setFilename(line);
					} else if (entry.getVersion() == null) {
						String[] substrings = line.split("\\s+");
						if (substrings.length >= 2) {
							entry.setVersion(substrings[1]);
						} else {
							entry.setVersion("");
						}
					} else if (entry.getUser() == null) {
						String[] substrings = line.split("\\s+");
						if (substrings.length >= 6) {
							entry.setUser(substrings[1].toLowerCase());
							Calendar cal = CalendarUtils.getDatetime(substrings[3], vssJournalResourceBundle.getString("dateFormat"), substrings[5], vssJournalResourceBundle.getString("timeFormat"));
							entry.setDatetime(cal);
						} else {
							entry.setUser("");
						}
					} else if (entry.getAction() == null) {
						entry.setAction(line);
					} else if (entry.getComment() == null) {
						if (line.startsWith(vssJournalResourceBundle.getString("commentLabel"))) {
							entry.setComment(line.substring(11).trim());
						} else {
							entry.setComment(line.trim());
						}
					}
					
					line = reader.readLine();
				}
				
				if (entry.getFilename() != null && isFilenameInProject(entry.getFilename(), projects)) {
					entries.add(entry);
				}
				
				if (line != null)
					line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		return this;
	}
	
}
