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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jenkinsci.plugins.vssj.VssChangeLogParser;
import org.jenkinsci.plugins.vssj.VssJournal;
import org.jenkinsci.plugins.vssj.VssJournalEntry;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Test for VSS journal parsing.
 * @author patlau
 *
 */
public class VssJournalTest {

	@Test
	public void testReadLog() throws IOException, SAXException {
		
		File f = new File("src/test/resources/vss.log");
		System.out.println(f.getAbsolutePath());
		
		assertTrue(f.exists());
		
		VssJournal log = new VssJournal(new Locale("de"));

		log.readJournal(f, new String[]{});
		assertTrue(log.getEntries().isEmpty());

		log.readJournal(f, new String[]{"$/PROJECT1"});
		assertTrue(!log.getEntries().isEmpty());
		
		log.readJournal(f);
		assertTrue(!log.getEntries().isEmpty());
		
		log.readJournal(f, new String[]{"$\\PROJECT1"});
		assertTrue(!log.getEntries().isEmpty());

		
		//for(VssLogEntry e : log.getEntries()) {
		//	System.out.println(e.toString());
		//}
		
		Calendar calNow = Calendar.getInstance();
		calNow.setTime(new Date());
		
		Calendar calBefore = Calendar.getInstance();
		calBefore.setTime(new Date());
		calBefore.add(Calendar.HOUR, -24);
		log.setLastModified(calBefore.getTime());
		
		System.out.println(calBefore.after(calNow));
		
		log.writeChangeFile(new File("target/test-changelog.xml"), calBefore);
		
		VssChangeLogParser p = new VssChangeLogParser();
		List<VssJournalEntry> list = p.parseFile(new File("target/test-changelog.xml"));
		for(VssJournalEntry e : list) {
			System.out.println(e);
		}
		
		
	}

}
