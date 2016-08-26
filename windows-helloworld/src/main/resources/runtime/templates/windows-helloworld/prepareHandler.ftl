
<#-- Prepare Handler Freemarker Template -->


set HIDDENDIR=${job.jobDirectory}\.MIF
mkdir "%HIDDENDIR%"

exit %ERRORLEVEL%
