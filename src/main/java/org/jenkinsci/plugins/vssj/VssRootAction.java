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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.model.Project;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;


/**
 * Starting point for a simple VSS file based browser.
 * 
 * Should display the configured journal files of the projects.
 * 
 * @author patlau
 *
 */
@Extension
public class VssRootAction implements RootAction {

	private static final Logger LOGGER = Logger.getLogger(VssRootAction.class.getName());
	private static final long serialVersionUID = 1L;
	
	Project<?, ?> selectedProject;

	public VssRootAction() {
		LOGGER.log(Level.FINE, "START");
	}

	/**
	 * Icon for action.
	 * 
	 * @return icon file name
	 */
	@Override
	public String getIconFileName() {
		return "/plugin/vss-journal/images/vss.png";
		// Why does my own icon not work?
		// "/plugin/objectstudio-vss/icons/game-22x22.png";
		// "/plugin/objectstudio-vss/images/logo.png";
	}

	/**
	 * Action display name (showed in left menu).
	 * 
	 * @return name
	 */
	@Override
	public String getDisplayName() {
		return "SourceSafe Journals";
	}

	/**
	 * Action URL.
	 * 
	 * @return url
	 */
	@Override
	public String getUrlName() {
		return "/vssj";
	}
	
	public List<Project<?, ?>> getProjects() {
		List<Project<?, ?>> projects = new ArrayList<Project<?, ?>>();
		Jenkins j = Jenkins.getInstance();
		for (Project<?, ?> p : j.getProjects()) {
			if (isVssSCM(p))
				projects.add(p);
		}
		return projects;
	}
	
	public Project<?, ?> getSelectedProject() {
		return selectedProject;
	}

	public void doProjectSubmit(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
		
		JSONObject form = request.getSubmittedForm();
		try {
			selectedProject = getProjects().get(Integer.parseInt(form.getString("")));
		} catch (NumberFormatException ex) {
			// Invalid format
			selectedProject = null;
		}
		response.sendRedirect("");
		
	}
	
	public List<VssJournalEntry> getJournalEntries() {
		
		List<VssJournalEntry> list = new ArrayList<VssJournalEntry>();
		
		Project<?, ?> p = getSelectedProject();
		if (isVssSCM(p)) {
			VssSCM vss = (VssSCM) p.getScm();
			File f = new File(vss.getVssJournalFile());
			if (f.exists()) {
				VssJournal journal = new VssJournal();
				journal.readJournal(f);
				list = journal.getNewEntries(CalendarUtils.midnight());
			}
		}
		
		return list;
	}
	
	public String getProjectLabel(Project<?, ?> p) {
		return isVssSCM(p) ? p.getName() + " [" + ((VssSCM) p.getScm()).getVssJournalFile() + "]" : "";
	}
	
	boolean isVssSCM(Project<?, ?> p) {
		return p != null && p.getScm() != null && (p.getScm() instanceof VssSCM);
	}
	
}
