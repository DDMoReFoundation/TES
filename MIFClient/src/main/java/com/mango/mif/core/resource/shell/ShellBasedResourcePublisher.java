/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/
package com.mango.mif.core.resource.shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.api.ResourceCopierFactory;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.template.CommandBuilderContextBuilder;
import com.mango.mif.core.resource.BaseResourcePublisher;
import com.mango.mif.core.resource.ResourceCopier;
import com.mango.mif.core.resource.ResourceCopierFactoryImpl;
import com.mango.mif.core.resource.ResourceCopierParameters;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.Pair;

/**
 * A template based resource publisher which is responsible for publishing resources.
 *
 */
public class ShellBasedResourcePublisher extends BaseResourcePublisher {

    private static Logger logger = Logger.getLogger(ShellBasedResourcePublisher.class);

    /** The invoker. */
    Invoker invoker;

    /** The resource copier. */
    ResourceCopier resourceCopier;

    /**
     * Instantiates a new shell based resource publisher.
     *
     * @param publisherParameters the shell based resource publisher parameters
     */
    public ShellBasedResourcePublisher(PublisherParameters publisherParameters) {
        Preconditions.checkNotNull(publisherParameters, "Parameters must not be null");
        Preconditions.checkNotNull(publisherParameters.getInvoker(), "Invoker must be set");
        Preconditions.checkNotNull(publisherParameters.getCommandBuilder(), "CommandBuilder must be set");
        Preconditions.checkNotNull(publisherParameters.getRootDirectory(), "Root Directory must be set");
        Preconditions.checkState(publisherParameters.getRequestAttributes().containsKey(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()), String.format("%s request attribute is empty",ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName()));

        this.invoker = publisherParameters.getInvoker();
        this.commandBuilder = publisherParameters.getCommandBuilder();
        this.submitHostPreamble = publisherParameters.getSubmitHostPreamble();
        this.gridHostPreamble = publisherParameters.getGridHostPreamble();
        this.rootDirectory = publisherParameters.getRootDirectory();
        this.requestAttributes = publisherParameters.getRequestAttributes();
    }

    /**
     * 1. retrieve request id from the sharedLocationManager;
     * 2. Populate the template with parameters using a command builder (TODO create abstract PublishFilesCommandBuilder and TemplateBasePublishFilesCommandBuilder)
     *      a. root directory to copy empty folder structure from
     *      b. request input directory (sharedLocationManager.getRequestInputDirectory(requestID)
     * 3. execute the script using JSCH and user name and password
     */
    @Override
    public String publish() throws MIFException {
        List<String> directoriesToCreate = new ArrayList<String>();
        String uuid = null;
        File requestDir = null;
        try {
            do {
                uuid = UUID.randomUUID().toString();
                requestDir = getRequestDirectory(uuid);
            } while (requestDir.exists());

            directoriesToCreate.add(requestDir.getAbsolutePath());

            Pair<String, String> directoriesStructureToCopy = Pair.empty();
            directoriesStructureToCopy.setFirst(rootDirectory.getAbsolutePath());
            directoriesStructureToCopy.setSecond(requestDir.getAbsolutePath());

            // We get pairs of files in "rootedFiles".  For example:
            //   1.   /usr/share/tomcat6/navigator/resources/resource1.x
            //   2.   /usr/share/tomcat6/navigator
            // This means we want to "subtract" the path in 2 from the path in 1 and copy
            //        /usr/share/tomcat6/navigator/resources/resource1.x
            //        /shared/location/root/resources/resource1.x
            //
            List<Pair<String, String>> filesNotInSourceDirectoryToCopy = prepareSpecialFilesToCopy(uuid.toString());

            // What's going on here?  Well, filesToPublish is populated from the SCRIPT_INPUT_RECORD and
            // will be a bunch of absolute paths which will include the sandbox location.  So, for example
            // pair.first will be set to "/usr/global/navws-tb/navigatorsandbox/34/Scripts/data/theoph.csv"
            // pair.second needs to be set to the input directory, plus the "Scripts/data/theoph.csv" (the
            // part that isn't the sandbox), i.e.:
            // /usr/global/navws-tb/mifshare/02f8a3a5-77ff-49f6-bcf3-d1558a1bbd6d/inputs/Scripts/data/theoph.csv
            //
            if (!CollectionUtils.isEmpty(filesToPublish)) {
                for (File fileToPublish : filesToPublish) {

                    // This test added because SCM execution was causing a few problems as it was being
                    // given "/" as something to copy.
                    //
                    if (fileToPublish.exists() && fileToPublish.isFile()) {

                        String extra = fileToPublish.getAbsolutePath();
                        File target = new File(extra);
                        if (extra.startsWith(rootDirectory.getAbsolutePath())) {
                            extra = extra.substring(rootDirectory.getAbsolutePath().length());
                            target = new File(requestDir, extra);
                        }
                        Pair<String, String> pair = Pair.of(fileToPublish.getAbsolutePath(),target.getAbsolutePath());
                        filesNotInSourceDirectoryToCopy.add(pair);
                    }
                }
            }
            runScript(uuid, directoriesStructureToCopy, directoriesToCreate, filesNotInSourceDirectoryToCopy, submitHostPreamble);

        } catch (MIFException exp) {
            throw exp;
        } catch (Exception exp) {
            throw new MIFException("Unexpected error during the publishing", exp);
        }
        setRequestID((uuid != null) ? uuid.toString() : null);
        return getRequestID();
    }

    /**
     * Creates the resource copier.
     *
     * @param sourceDir the source dir
     * @param destDir the dest dir
     * @return the resource copier
     */
    ResourceCopier createResourceCopier(File sourceDir, File destDir) {
        ResourceCopierParameters resourceCopierParameters = buildResourceCopierparameters(sourceDir, destDir);
        ResourceCopierFactory resourceCopierFactory = new ResourceCopierFactoryImpl();
        return resourceCopierFactory.create(resourceCopierParameters);
    }

    /**
     * Builds the ResourceCopier parameters.
     *
     * @param sourceDir the source dir
     * @param destDir the dest dir
     * @return the resource copier parameters
     */
    private ResourceCopierParameters buildResourceCopierparameters(File sourceDir, File destDir) {
        ResourceCopierParameters resourceCopierParameters = new ResourceCopierParameters();
        resourceCopierParameters.setCommandBuilder(getCommandBuilder());
        resourceCopierParameters.setDestDirectory(destDir);
        resourceCopierParameters.setSourceDirectory(sourceDir);
        resourceCopierParameters.setInvoker(getInvoker());
        return resourceCopierParameters;
    }

    /**
     * Prepare special files to copy.
     *
     * @param requestID the request id
     * @return the list
     */
    private List<Pair<String, String>> prepareSpecialFilesToCopy(String requestID) {
        List<Pair<String, String>> specialFilesToCopy = new ArrayList<Pair<String, String>>();
        for (Pair<File, File> pair : rootedFiles) {
            File file = pair.getFirst();
            File root = pair.getSecond();

            String fullPath = file.getAbsolutePath();
            String rootPath = root.getAbsolutePath();

            if (fullPath.indexOf(rootPath) != 0) {
                logger.warn("Publisher warning: file "
                        + fullPath
                        + " does not contain specified root "
                        + rootPath
                        + " SO THIS FILE WILL NOT BE COPIED.");
            } else {
                String relativePath = fullPath.substring(rootPath.length());
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                File destFile = getRequestDestinationForSpecialFile(relativePath, requestID);
                Pair<String, String> fromTo = Pair.of(file.getAbsolutePath(), destFile.getAbsolutePath());
                specialFilesToCopy.add(fromTo);
            }
        }
        return specialFilesToCopy;
    }

    /**
     * Invoke the template which will create all the directories as the user and copy all the files in one go.
     *
     * @param uuid - effectively the job id of the job that doesn't exist at this point.
     * @param directoriesToCopy A pair of directory paths, one of which must be copied to the other
     * @param directoriesToCreate The list of full paths of directories to create.
     * @param filesToCopy Pairs of file paths to copy [to] and [from] which need special care
     * @param submitHostPreamble The script preamble to be placed at the top of the script
     */
    private void runScript(String uuid, Pair<String, String> directoriesToCopy, List<String> directoriesToCreate,
            List<Pair<String, String>> filesToCopy, String submitHostPreamble) throws MIFException {

        Preconditions.checkNotNull(commandBuilder, "The command builder cannot be null");

        String entireCommand;
        try {
            CommandBuilderContextBuilder contextBuilder = new CommandBuilderContextBuilder(commandBuilder.getContext()).populateCommandBuilderContext(requestAttributes)
                    .setVariable("navigatorJobId", uuid.toString())
                    .setVariable("directoriesToCreate", directoriesToCreate)
                    .setVariable("directoriesToCopy", directoriesToCopy)
                    .setVariable("filesToCopy", filesToCopy);
            if(submitHostPreamble!=null) {
                contextBuilder.setSubmitHostPreamble(submitHostPreamble);
            }
            commandBuilder.setContext(contextBuilder.buildCommandBuilderContext());
            entireCommand = commandBuilder.getCommand();

            logger.debug("PREPARE HANDLER in its FULL form");
            logger.debug(entireCommand);

        } catch (ExecutionException ee) {
            throw new MIFException(ee);
        }

        InvokerHelper helper = new InvokerHelper(getInvoker());
        InvokerResult results = null;

        String requestId = (uuid != null) ? uuid.toString() : null;

        try {
            logExecutionMetrics(requestId, "started");
            results = helper.runAndReportFailures(entireCommand);
        } catch (Exception e) {
            throw new MIFException(e);
        } finally {
            logExecutionMetrics(requestId, "ended");
        }

        if (InvokerHelper.failed(results)) {
            throw new MIFException("File publishing failed, please see logs for details");
        }
    }

    /**
     * Logs the execution metrics for the publishing process
     */
    private void logExecutionMetrics(String requestId, String status) {
        logger.info(String.format("*** Execution Metrics : {Resource.Publishing} Publishing of resources for request id [%s] has %s",
            requestId, status));
    }

    private File getRequestDestinationForSpecialFile(String file, String requestID) {
        return new File(getRequestDirectory(requestID), file);
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public void setSubmitHostPreamble(String submitHostPreamble) {
        this.submitHostPreamble = submitHostPreamble;
    }

    public ResourceCopier getResourceCopier() {
        return resourceCopier;
    }

    @Override
    public void setResourceCopier(ResourceCopier resourceCopier) {
        this.resourceCopier = resourceCopier;
    }

}
