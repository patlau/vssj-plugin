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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * SourceSafe Log file entry.
 * @author patlau
 *
 */
public class VssJournalEntry {
	private String filename;
	private String version;
	private String user;
	private Calendar datetime;
	private String action;
	private String comment;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Calendar getDatetime() {
		return datetime;
	}
	public void setDatetime(Calendar changed) {
		this.datetime = changed;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getDatetimeString() {
		if (datetime == null)
			return "";
		return DateFormat.getDateTimeInstance().format(datetime.getTime());
	}
	
	public void setDatetimeString(String s) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance();
		try {
			cal.setTime(sdf.parse(s));
			datetime = cal;
		} catch (ParseException e) {
			e.printStackTrace();
		} 
	}
	
	@Override
	public String toString() {
		return 
			(filename == null ? "" : filename) + " - " +
			(version == null ? "" : version) + " - " +
			(user == null ? "" : user) + " - " +
			(datetime == null ? "" : getDatetimeString()) + " - " +
			(action == null ? "" : action) + " - " +
			(comment == null ? "" : comment);
	}
	
}
