
<#-- Retrieve Handler Freemarker Template.
     The Spring configuration is configured such that this template is resolved locally and executed locally.
     The script ought to execute successfully either on a Windows MIF host (e.g. a developer's laptop) or a Linux MIF host (e.g. a developer's VM). -->


echo  Doing some dummy file-retrieval.

<#-- The execution of this pair of commands is mutually exclusive dependent on the O/S;
     the invalid command ought to produce an error message but shouldn't fail the script execution -->
copy "${job.jobMifHiddenDir}\MIF.list" "${job.jobDirectory}\MIF-list-of-files.out"
cp "${job.jobMifHiddenDir}/MIF.list" "${job.jobDirectory}/MIF-list-of-files.out"

exit 0
