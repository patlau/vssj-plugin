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

import java.util.Iterator;
import java.util.List;

import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;

/**
 * SourceSafe ChangeLogSet implementation.
 * @author patlau
 *
 */
public class VssChangeLogSet extends ChangeLogSet<VssChangeLogEntry> {
	
    private List<VssChangeLogEntry> entries;
    
    protected VssChangeLogSet(Run<?, ?> build, RepositoryBrowser<?> browser, List<VssChangeLogEntry> logs) {
    	//super((AbstractBuild<?, ?>) build);
        super(build, browser);
        this.entries = logs;
    }

    /**
     * Used in Browser config.jelly?
     */
    @Override
    public String getKind() {
        return "vss";
    }

    @Override
    public boolean isEmptySet() {
        return entries.isEmpty();
    }

    @Override
	public Iterator<VssChangeLogEntry> iterator() {
        return entries.iterator();
    }

    public List<VssChangeLogEntry> getEntries() {
        return entries;
    }

    // Synonym for getEntries() - allows this plugin to work with the Multiple SCMs plugin
    public List<VssChangeLogEntry> getLogs() {
        return getEntries();
    }
}
