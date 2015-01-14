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
package org.jenkinsci.plugins.vssj.browsers;

import java.io.IOException;
import java.net.URL;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.scm.RepositoryBrowser;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.vssj.VssChangeLogEntry;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


/**
 * SourceSafe Repository browser implementation. Not yet working.
 * @author patlau
 *
 */
public class SourceSafeRepositoryBrowser extends
		RepositoryBrowser<VssChangeLogEntry> {

	/**
	 * Descriptor instance.
	 */
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	private static final long serialVersionUID = 1L;
	
	String url;

    @DataBoundConstructor
    public SourceSafeRepositoryBrowser(String path) {
    	this.url = path;
    }
    
    @Override
	public Descriptor<RepositoryBrowser<?>> getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public URL getChangeSetLink(VssChangeLogEntry changeSet) throws IOException {
    	URL rootUrl = new URL(this.url == null ? Jenkins.getInstance().getRootUrl() : this.url);
        String path = String.format("vss/browse/%s/%s", changeSet.getFilenameForUrl(), changeSet.getVersion());
        return new URL(rootUrl, path);
    }
    
    /**
     * Plugin descriptor implementation.
     * @author patlau
     *
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        
    	public  DescriptorImpl() {
            super(SourceSafeRepositoryBrowser.class);
        }
    	
        @Override
        public String getDisplayName() {
            return "SourceSafe Browser";
        }
        
        @Override
        public RepositoryBrowser<?> newInstance(StaplerRequest req,
        		JSONObject formData)
        		throws hudson.model.Descriptor.FormException {
        	RepositoryBrowser<?> instance = super.newInstance(req, formData);
        	return instance;
        }
        
    }	

}
