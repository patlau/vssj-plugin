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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hudson.model.Run;
import hudson.scm.ChangeLogParser;
import hudson.scm.RepositoryBrowser;
import hudson.util.Digester2;

import org.apache.commons.digester.Digester;

import org.xml.sax.SAXException;

/**
 * SourceSafe change log parser.
 * 
 * See https://wiki.jenkins-ci.org/display/JENKINS/Change+log
 * 
 * @author patlau
 */
public class VssChangeLogParser extends ChangeLogParser {

	@Override
	@SuppressWarnings("rawtypes")
	public VssChangeLogSet parse(Run build, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException {

		List<VssJournalEntry> changeList = parseFile(changelogFile);
		
		List<VssChangeLogEntry> entries = new ArrayList<VssChangeLogEntry>();
		for (VssJournalEntry e : changeList) {
			VssChangeLogEntry cle = new VssChangeLogEntry(e);
			entries.add(cle);
		}
		VssChangeLogSet logSet = new VssChangeLogSet(build, browser, entries);
		return logSet;
	}

	
	List<VssJournalEntry> parseFile(File changelogFile) throws IOException, SAXException {
		List<VssJournalEntry> changeList = new ArrayList<VssJournalEntry>();
		Digester digester = new Digester2();
		digester.push(changeList);
	
		digester.addObjectCreate("*/change", VssJournalEntry.class);
		digester.addBeanPropertySetter("*/change/action");
		digester.addBeanPropertySetter("*/change/comment");
		digester.addBeanPropertySetter("*/change/user");
		digester.addBeanPropertySetter("*/change/filename");
		digester.addBeanPropertySetter("*/change/version");
		digester.addBeanPropertySetter("*/change/date", "datetimeString");
		digester.addSetNext("*/change", "add");
	
		// Do the actual parsing
		FileReader reader = new FileReader(changelogFile);
		digester.parse(reader);
		reader.close();
		return changeList;
	}

}
