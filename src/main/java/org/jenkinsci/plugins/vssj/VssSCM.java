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

/*
 * https://wiki.jenkins-ci.org/display/JENKINS/SCM+plugin+architecture
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
//import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

import org.jenkinsci.plugins.vssj.browsers.SourceSafeRepositoryBrowser;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;


/**
 * SourceSafe SCM journal file implementation.
 * 
 * This implementation is based on monitoring a SourceSafe journal file for changes.
 * Which means it is more or less a file monitor SCM, with some SourceSafe extensions.
 * See http://msdn.microsoft.com/en-US/library/ms181070%28v=vs.80%29.aspx.
 * 
 * For Get Latest ss.exe command line utility is used. Because this is currently not
 * connected to the journal file monitoring, it may get more files than displayed in
 * the change log. It should be possible to enhance this by using either the -V command
 * line option.
 * 
 * @author patlau
 *
 */
public class VssSCM extends SCM {

	private SourceSafeRepositoryBrowser browser;
	
	private boolean clean;
    private String locale = "de";
    private boolean useSubdirs;
	private boolean vssGet;
	private String vssGetOptions = "-R -GWR -I-Y";
	private String vssJournalFile = "";
	private String vssProjects = "";
	private Long waitSeconds = 1L;
    private String workspaceName = "";

	/**
	 * Plugin constructor for Jenkins.
	 * @param clean clean
	 * @param vssGet vss get
	 * @param vssJournalFile vss journal file
	 * @param vssProjects vss projects
	 * @param workspaceName sub directory in workspace
	 * @param waitSeconds vss log wait time in seconds
	 * @param browser selected scm browser
	 */
	@DataBoundConstructor
	public VssSCM(boolean clean, boolean vssGet, String vssGetOptions, String vssJournalFile, String vssProjects, String workspaceName, Long waitSeconds, 
			String locale, boolean useSubdirs, SourceSafeRepositoryBrowser browser) {
	    // Copying arguments to fields
		this.clean = clean;
		this.vssGet = vssGet;
		this.vssGetOptions = vssGetOptions;
		this.workspaceName = workspaceName;
		this.vssJournalFile = vssJournalFile;
		this.vssProjects = vssProjects;
		this.waitSeconds = waitSeconds;
		this.locale = locale;
		this.useSubdirs = useSubdirs;
		this.browser = browser;

    	Logger log = LogManager.getLogManager().getLogger("hudson.WebAppMain");
    	log.info("SCM BROWSER: " + browser);
	}
	
    @Override
    @Exported
    public SourceSafeRepositoryBrowser getBrowser() {
        return browser;
    }
    
	public Long getWaitSeconds() {
		return waitSeconds;
	}

	/**
	 * Set wait time.
	 * @param waitSeconds wait time in seconds
	 */
	public void setWaitSeconds(Long waitSeconds) {
		this.waitSeconds = waitSeconds;
	}

	public boolean isClean() {
		return clean;
	}

	/**
	 * Set clean before checkout.
	 */
	public void setClean(boolean clean) {
		this.clean = clean;
	}

	public boolean isVssGet() {
		return vssGet;
	}

	/**
	 * Set if VSS GET is enabled.
	 */
	public void setVssGet(boolean vssGet) {
		this.vssGet = vssGet;
	}

	public String getVssGetOptions() {
		return vssGetOptions;
	}
	
	public void setVssGetOptions(String options) {
		this.vssGetOptions = options;
	}
	
	public String getWorkspaceName() {
		return workspaceName;
	}

	/**
	 * Set sub directory of workspace.
	 * @param workspaceName name of sub directory
	 */
	public void setWorkspaceName(@Nullable String workspaceName) {
		this.workspaceName = workspaceName;
	}

	public String getVssJournalFile() {
		return vssJournalFile;
	}

	/**
	 * Set Path of VSS journalFile.
	 */
	public void setVssJournalFile(String vssJournalFile) {
		this.vssJournalFile = vssJournalFile;
	}
	
	public String getVssProjects() {
		return vssProjects;
	}

	/**
	 * Set VSS projects (multiple projects separated by ";").
	 */
	public void setVssProjects(String vssProjects) {
		this.vssProjects = vssProjects;
	}
	
	private String[] getVssProjectList() {
		return this.getVssProjects().split(";");
	}
	
	/**
	 * Get the VSS journal locale.
	 */
	public Locale getLocale() {
		return new Locale(this.locale);
	}
	
	public boolean getUseSubdirs() {
		return useSubdirs;
	}
	
	public void setUseSubdirs(boolean useSubdirs) {
		this.useSubdirs = useSubdirs;
	}
	

	/**
	 * Must implement for Jenkins.
	 */
	@Override
	public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> arg0,
			Launcher arg1, TaskListener arg2) throws IOException,
			InterruptedException {
		return null;
	}

	/**
	 * This method does the Update / GetLatest.
	 */
	@Override
	public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher,
			FilePath workspace, BuildListener buildListener, File changeLogFile) throws AbortException  {
		
		PrintStream logger = buildListener.getLogger();
		
		logger.println("Get SourceSafe Projects " + (Arrays.asList(this.getVssProjects())));
        
		if (this.getVssProjectList() == null || this.getVssProjectList().length == 0) {
			buildListener.fatalError("No valid VSS Projects specified");
		}
		
		AbstractBuild<?, ?> lastBuild = build.getPreviousBuild();
        Calendar lastBuildTime = lastBuild == null ? CalendarUtils.midnight() : lastBuild.getTimestamp();
    	
        try {
            VssExe exe = new VssExe(launcher, workspace, buildListener, getSsExePath(), getUseSubdirs());
            checkoutVss(logger, exe);
		} catch (IOException e) {
			e.printStackTrace();
            throw new AbortException("Error Cleaning: " + e.getMessage());
		}   
       
        // Could also be done in calcRevisionsFromBuild
        VssRevisionState currentState = VssRevisionState.checkJournal(new File(this.vssJournalFile), getVssProjectList(), getLocale());
        build.addAction(currentState);

        FilePath localChangeLogFile = null;
        try {
     	   	localChangeLogFile = workspace.createTempFile("vss", ".xml");
     	   currentState.writeChangeFile(localChangeLogFile, lastBuildTime);

 		} catch (IOException e) {
 			e.printStackTrace();
             throw new AbortException("Error Reading VSS Log: " + e.getMessage());
 		} catch (InterruptedException e) {
 			e.printStackTrace();
             throw new AbortException("Interrupted Reading VSS Log: " + e.getMessage());
        }      
        
        try {
	        if (localChangeLogFile.exists()) {
	            localChangeLogFile.copyTo(new FilePath(changeLogFile));
	            localChangeLogFile.delete();
	        } else {
	            createEmptyChangeLog(changeLogFile, buildListener, "log");
	        }
		} catch (IOException e) {
			e.printStackTrace();
            throw new AbortException("Error Copying Change File: " + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
            throw new AbortException("Interrupted Copying Change File: " + e.getMessage());
        }
        
        // Could also add Parameters
        //build.addAction(new ParametersAction(new StringParameterValue("myParameterName", "myParameterValue")));

        return true;
	}

	protected void checkoutVss(PrintStream logger, VssExe exe)
			throws IOException {
		for (String project : this.getVssProjectList()) {
			if (isClean())
				try {
					exe.clean(project);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			else
				logger.println("Clean Disabled");
		}
		for (String project : this.getVssProjectList()) {
			if (isVssGet()) {
				int result = exe.get(project, getVssGetOptions());
				if (result != 0) {
					logger.println("VSS Get Failed with " + result);
				}
			} else {
				logger.println("VSS Get Disabled");
			}
		}
	}
	
	@Override
	public PollingResult compareRemoteRevisionWith(Job<?, ?> project,
			Launcher launcher, FilePath workspace, TaskListener taskListener,
			SCMRevisionState baseline) throws IOException, InterruptedException {

        if (this.getVssJournalFile() == null) {
            taskListener.fatalError("No VSS Logfile specified");
            return PollingResult.NO_CHANGES;
        }
        
        File journalFile = new File(this.getVssJournalFile());
        if (!journalFile.exists()) {
            taskListener.fatalError("No valid VSS Logfile specified");
            return PollingResult.NO_CHANGES;
        }
        
		taskListener.getLogger().println("Checking SourceSafe Log " + journalFile.getAbsolutePath());
        
		VssRevisionState currentState = VssRevisionState.checkJournal(journalFile, getVssProjectList(), getLocale());
		
		final Run<?, ?> lastBuild =  project.getLastBuild();
        if (lastBuild == null && !currentState.mustWait(this.getWaitSeconds())) {
            taskListener.getLogger().println("No existing build. Scheduling a new one.");
            return PollingResult.BUILD_NOW;
        }

        boolean changes = false;
        VssRevisionState vssBaseline = (VssRevisionState) baseline;
        if (currentState.isNewerThan(vssBaseline)) {
        	if (currentState.mustWait(this.getWaitSeconds())) {
                taskListener.getLogger().println("File change detected, but must wait.");
        	} else {
                taskListener.getLogger().println("File change detected.");
        		changes = true;
        	}
        }
		
		return new PollingResult(vssBaseline, currentState, changes ? Change.SIGNIFICANT : Change.NONE);
	}

	@Override
	public ChangeLogParser createChangeLogParser() {
		return new VssChangeLogParser();
	}
    
    /**
     * Get SS.EXE path.
     */
    public String getSsExePath() {
		return getDescriptor().getSsExePath();
	}
    
    /**
     * Default VSS projects.
     */
    public String getDefaultVssProjects() {
		return "";
	}

    /**
     * Get Plugin descriptor.
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    
    /**
     * Plugin descriptor implementation.
     * @author patlau
     *
     */
    @Extension
    public static class DescriptorImpl extends SCMDescriptor<VssSCM> {

    	private String ssExePath = getDefaultSsExePath();
    	
    	/**
    	 * Default Contructor.
    	 */
        public DescriptorImpl() {
//        	super(VssSCM.class, SourceSafeRepositoryBrowser.class);
        	super(VssSCM.class, null);
            load();
        }

        /**
         * Create new SCM instance.
         */
        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	
        	//Logger log = LogManager.getLogManager().getLogger("hudson.WebAppMain");
        	
            VssSCM scm = (VssSCM) super.newInstance(req, formData);
            
//            scm.browser = RepositoryBrowsers.createInstance(
//            		SourceSafeRepositoryBrowser.class,
//                                        req,
//                                        formData,
//                                        "browser");
//        	
//        	if (scm.browser == null) {
//        		scm.browser = req.bindJSON(SourceSafeRepositoryBrowser.class, formData);
//        	}
            
            return scm;
        }

        /**
         * SCM display name.
         */
		@Override
		public String getDisplayName() {
			return "SourceSafe Journal";
		}

        /**
         * Configure SCM instance.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        	req.bindJSON(this, json.getJSONObject("vssj"));
            save();
            return true;
        }

        /**
         * With which kinds of projects can this SCM be used.
         */
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

		public String getSsExePath() {
			return ssExePath;
		}

		public void setSsExePath(String ssExePath) {
			this.ssExePath = ssExePath;
		}
		
		/**
		 * Do form validation for SS.EXE.
		 */
		public FormValidation doCheckSsExePath(@QueryParameter String value) {
			File file = new File(value);  
			WinUtils.checkForUnmappedNetworkDrive(file);
			if (file.exists() && !file.isDirectory()) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Invalid Filename.");
			}
		}
		
		/**
		 * Default SS.EXE path.
		 */
		public String getDefaultSsExePath() {
			return "SS.EXE";
		}

		/**
		 * Do form validation of VSS journalFile.
		 */
		public FormValidation doCheckVssJournalFile(@QueryParameter String value) {
			if (value == null || value.isEmpty())
				return FormValidation.ok();
			
			File file = new File(value);
			
			// Check for network drive and try to map
			WinUtils.checkForUnmappedNetworkDrive(file);
			
			if (file.exists() && !file.isDirectory()) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Invalid Filename.");
			}
		}

		/**
		 * Do form validation of wait seconds.
		 */
		public FormValidation doCheckWaitSeconds(@QueryParameter String value) {
			if (value == null || value.isEmpty())
				return FormValidation.error("Can not be empty.");
			Integer i = Integer.valueOf(value);
			if (i != null && i >= 0) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Invalid value.");
			}
		}

		/**
		 * Do form validation of locale.
		 */
		public FormValidation doCheckLocale(@QueryParameter String value) {
			if (value == null || value.isEmpty())
				return FormValidation.error("Can not be empty.");
			if (value.equals("de")) {
				return FormValidation.ok();
			} else {
				return FormValidation.error("Currently only <de> is supported.");
			}
		}
    }
}
