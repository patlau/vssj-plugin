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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.EditType;

import org.apache.commons.io.FilenameUtils;

import org.kohsuke.stapler.export.ExportedBean;

/**
 * SourceSafe Changelog entry. 
 * @author patlau
 *
 */
@ExportedBean(defaultVisibility = 999)
public class VssChangeLogEntry extends ChangeLogSet.Entry {

	private VssJournalEntry logEntry;
	
	public VssChangeLogEntry(VssJournalEntry entry) {
		this.logEntry = entry;
	}
	
	@Override
	public String getMsg() {
		return this.logEntry.getFilename();
	}

	@Override
	public User getAuthor() {
		return this.logEntry.getUser().isEmpty() ? User.getUnknown() : User.get(this.logEntry.getUser());
	}

	@Override
	public Collection<String> getAffectedPaths() {
		List<String> list = new ArrayList<String>();
		String s = this.logEntry.getFilename().substring(2).replace("\\", "/");
		//list.add(FilenameUtils.getPath(s));
		list.add(s);
		return list;
	}

    public String getFormattedTimestamp() {
    	   return this.logEntry.getDatetimeString();
    }
    
    public String getVssUser() {
    	return this.logEntry.getUser();
    }
    
    public String getAction() {
    	return this.logEntry.getAction();
    }
    
    public String getComment() {
    	if (this.logEntry.getComment() == null || this.logEntry.getComment().trim().isEmpty() 
    			|| this.logEntry.getComment().equals("null"))
    		return null;
    	return this.logEntry.getComment();
    }
    
    public String getVersion() {
    	return this.logEntry.getVersion();
    }


	public Object getFilenameForUrl() {
		String s = FilenameUtils.normalize(this.logEntry.getFilename());
		return s;
	}
	
    @Override
    public Collection<? extends AffectedFile> getAffectedFiles() {
		List<AffectedFile> list = new ArrayList<AffectedFile>();
		list.add(new VssFile(this.logEntry));
		return list;
    }
    
    /**
     * SourceSafe File implementation.
     * @author patlau
     *
     */
    public class VssFile implements AffectedFile {

    	private VssJournalEntry entry;
    	
    	public VssFile(VssJournalEntry entry) {
    		this.entry = entry;
    	}
    	
		
		@Override
		public String getPath() {
			return logEntry.getFilename().substring(2).replace("\\", "/");
		}

		
		@Override
		public EditType getEditType() {
			if (entry == null || entry.getVersion().equals("1")) {
				return EditType.ADD;
			}
			return EditType.EDIT;
		}
    	
    }
	
}
