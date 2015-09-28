Jenkins SourceSafe Journal Plugin
=================================

Provides Jenkins integration with Visual SourceSafe Journal file.

The goal of this plugin was to provide a simple monitoring of changes in SourceSafe, 
without the need of a local SourceSafe installation or COM. It is similar to the [FSTrigger Plugin](https://wiki.jenkins-ci.org/display/JENKINS/FSTrigger+Plugin), but can also parse the journal file and display new entries in the Jenkins change log. 

You need to [set up a journal file](http://msdn.microsoft.com/en-us/library/ms181070%28v=vs.80%29.aspx) to audit user activity.

If the journal file is on a shared drive, you can map that drive in init.groovy if needed:

    def mapdrive = "net use V: \\\\server\\path\"
    mapdrive.execute()

The plugin can also get the latest versions of files using the [SS command line utility] (http://msdn.microsoft.com/en-us/library/5ws92cw2%28v=vs.80%29.aspx) [GET command](http://msdn.microsoft.com/en-us/library/661w6e3d%28v=vs.80%29.aspx).

The SS GET command may get more changes than displayed in the change log, because it is currently not connected to the change monitoring. This was never a problem in our environment, but may be in others. 

See also [SourceSafe Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Visual+SourceSafe+Plugin).

Notes
* On "missing its descriptor" messages, make shure to clean target directory before compiling


License
-------

	(The MIT License)

	Copyright (c) 2015 Patrick Lauper

	Permission is hereby granted, free of charge, to any person obtaining
	a copy of this software and associated documentation files (the
	'Software'), to deal in the Software without restriction, including
	without limitation the rights to use, copy, modify, merge, publish,
	distribute, sublicense, and/or sell copies of the Software, and to
	permit persons to whom the Software is furnished to do so, subject to
	the following conditions:

	The above copyright notice and this permission notice shall be
	included in all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
	EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
	IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
	CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
	TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
	SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.