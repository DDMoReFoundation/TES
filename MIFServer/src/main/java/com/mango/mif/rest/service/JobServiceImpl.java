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

package com.mango.mif.rest.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Context;
import javax.xml.bind.JAXBException;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.mango.mif.client.api.rest.FileList;
import com.mango.mif.client.api.rest.FileSystemItem;
import com.mango.mif.client.api.rest.JobService;
import com.mango.mif.client.api.rest.JobSummaryMessages;
import com.mango.mif.client.api.rest.MIFResponse;
import com.mango.mif.client.api.rest.ResponseStatus;
import com.mango.mif.connector.Connector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.InvokerResult;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.invoker.InvokerHelperFactory;
import com.mango.mif.core.messaging.MessagingHelper;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.TextMessageCreator;

/**
 *          
 * FIXME Detailed status methods should apply the same rules in respect to non-existent job, invalid job type, etc. Currently they are not
 */
@MTOM
@Service
public class JobServiceImpl implements JobService {

    private static final String TYPE_DIR = "d";

    private static final String TYPE_FILE = "f";

    private static final String TAIL_CMD = "tail -c";

    private static final String RF = "rf";

    private static final String LD = "ld";

    private static final Logger LOG = Logger.getLogger(JobServiceImpl.class);

    private final static byte[] EMPTY_FILE_CONTENT = new byte[0];
    
    private String requestQueue;

    private JobManagementService jobManagementService;

    private MessageContext messageContext;

    private JmsTemplate jmsTemplate;

    private ConnectorsRegistry connectorsRegistry;
    /**
     * Configurable list of patterns to blacklist files from directory listing.
     */
    private Collection<String> fileTypeBlackList = new ArrayList<String>();
    private InvokerHelperFactory invokerHelperFactory;

    /**
     * Max response size foe the header.
     */
    @Value("${jobservice.controller.tailfile.maxsize}")
    private int tailFileMaxSizeInBytes;

    /**
     * The root folder on the filesystem. 
     * 
     */
    private String rootFolder = "/";

    public MessageContext getMessageContext() {
        return messageContext;
    }

    @Context
    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public String getJobStatus(String requestId) {
        LOG.info("Retrieving client request status for request " + requestId);
        Job job = jobManagementService.getJob(requestId);
        if (null == job) {
            return JobStatus.NOT_AVAILABLE.name();
        } else {
            JobStatus status = job.getJobStatus();
            LOG.info(String.format("Status of the Job %s is %s.",requestId, status));
            return status.name();
        }
    }

    @Override
    public FileList listDir(String jobId, String endPath) throws MIFException {

        Job job = jobManagementService.getJob(jobId);
        if (null == job) {
            throw new MIFException("Job does not exist: " + jobId);
        }
        String workingDir = job.getJobDirectory().getAbsolutePath();

        LOG.debug(String.format("listDir() - jobId = %s, working directory = %s",jobId, workingDir));
        final String requestUri = getMessageContext().getUriInfo().getRequestUri().toString();
        final String endPathParameter = getMessageContext().getUriInfo().getPathParameters().get("endPath").get(0);

        InvokerHelper invokerHelper = invokerHelperFactory.create(job.getInvoker());
        try {
            if (StringUtils.isBlank(workingDir) || !invokerHelper.directoryExists(workingDir)) {
                return new FileList();
            }
        } catch (ExecutionException e1) {
            throw new MIFException(String.format("Unable to check working directory %s exists using Invoker.", workingDir),e1 );
        }

        return getFileItemsList(invokerHelper, workingDir, endPath, requestUri, endPathParameter);
    }

    /**
     * 
     * FIXME - it does two things - collects directories and files in the given location. Before recactoring make sure that the requirements are still valid
     * @param invokerHelper that is used to retrieve directory contents
     * @param workingDir - a root against which the endPath should be resolved
     * @param endPath - a relative path to a directory
     * @param requestUri - a raw uri from REST request
     * @param endPathParameter - a raw end path from REST request
     * @return a list of files and directories
     * @throws MIFException if there is any problem with retrieving files
     */
    FileList getFileItemsList(InvokerHelper invokerHelper, String workingDir, String endPath, final String requestUri,
            final String endPathParameter) throws MIFException {
        FileList itemList = new FileList();
        // Get all the directories at this level
        List<String> directories = getFilesystemItems(invokerHelper, workingDir, endPath, LD);
        for (String dir : directories) {

            String folderUri = requestUri;
            try {
                folderUri += URLEncoder.encode(dir, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new MIFException("Could not encode URL", uee);
            }
            FileSystemItem item = new FileSystemItem();
            item.setName(dir.trim());
            try {
                item.setUri(new URI(folderUri));
            } catch (URISyntaxException e) {
                throw new MIFException(e);
            }

            item.setEndPath(endPathParameter + "/" + dir.trim());
            item.setType(FileSystemItem.FOLDER_ITEM);
            itemList.getFileSystemItems().add(item);
        }

        // Get all the files at this level
        List<String> filenames = getFilesystemItems(invokerHelper, workingDir, endPath, RF);
        for (String file : filenames) {
            String fileUri = requestUri.replaceAll("/ld/", "/rcf/");

            try {
                fileUri += URLEncoder.encode(file, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new MIFException("Could not encode URL", uee);
            }
            FileSystemItem item = new FileSystemItem();
            item.setName(file.trim());
            try {
                item.setUri(new URI(fileUri));
            } catch (URISyntaxException e) {
                throw new MIFException(e);
            }

            String pathToSet = ("/" + workingDir + "/" + endPathParameter + "/" + file
                    .trim());
            item.setEndPath(pathToSet);

            LOG.debug("listDir() - endPath set on the filesystemitem = " + "*" + pathToSet + "*");

            item.setType(FileSystemItem.FILE_ITEM);
            itemList.getFileSystemItems().add(item);
        }
        return itemList;
    }
    
    @Override
    public String getJobDirectory(String jobId) {
        Job job = jobManagementService.getJob(jobId);
        if (job == null) {
            return StringUtils.EMPTY;
        }
        return job.getJobDirectory().getAbsolutePath();
    }

    /**
     * List files (or directories) in the given endPath relative to workingDir 
     * 
     * FIXME this must be refactored - it does two things
     * 
     * @param invokerHelper - that will be used for listing files in directories
     * @param workingDir - directory root 
     * @param endPath - directory relative path 
     * @param filesystemItemType - a type of the item - RF - File, LD - Directory
     * @return a list of files in the given location
     * @throws MIFException if the listing can't be retrieved
     * 
     */
    private List<String> getFilesystemItems(InvokerHelper invokerHelper, String workingDir, String endPath, String filesystemItemType) throws MIFException {
        try {
            if (filesystemItemType.equals(RF)) {
                InvokerResult invokerResult = getListing(TYPE_FILE, workingDir, endPath, invokerHelper);
                List<String> files = getValidFiles(resultLinesAsList(invokerResult));
                return files;
            } else if (filesystemItemType.equals(LD)) {
                List<String> dirs = resultLinesAsList(getListing(TYPE_DIR, workingDir, endPath, invokerHelper));
                return dirs;
            } else {
                return new ArrayList<String>();
            }
        } catch (ExecutionException e) {
            LOG.error(e); //we want to log it here before throwing it to the client
            throw new MIFException(e);
        }
    }

    private InvokerResult getListing(String typeFlag, String workingDir, String endPath, InvokerHelper invokerHelper)
            throws ExecutionException {
        return invokerHelper.runAndReportFailures(buildRetrieveCommand(workingDir, endPath, typeFlag));
    }

    private List<String> resultLinesAsList(InvokerResult invokerResult) {
        return Arrays.asList(invokerResult.getStdout().split("\n"));
    }

    /**
     * Build a command which will retrieve either a list of files, or a list of directories,
     * depending on the args specified.
     * 
     * @param dir the directory to retrieve from
     * @param append something to arbitrarily stick on the end of the directory 
     * @param type The type of thing to retrieve, f=files d=directories
     */
    private String buildRetrieveCommand(String dir, String append, String type) {
        Preconditions.checkNotNull(append);
        //FIXME: this should be done in a freemarker template
        append = append.trim();
        if (!append.isEmpty()) {
            dir = dir + "/" + append;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("if cd \"");
        sb.append(dir);
        sb.append("\"; then find . -maxdepth 1 -type ");
        sb.append(type);
        sb.append(" | sort | cut -c3-; else echo cd \"");
        sb.append(dir);
        sb.append("\" failed >&2; exit 99; fi");

        return sb.toString();
    }

    @Override
    public byte[] getTailedFileContents(String jobId, String endPath) throws MIFException {
        Job job = jobManagementService.getJob(jobId);
        if (null == job) {
            return EMPTY_FILE_CONTENT;
        }

        byte[] fileContents;
        try {
            InvokerHelper invokerHelper = invokerHelperFactory.create(job.getInvoker());
            InvokerResult result = invokerHelper.runAndReportFailures(buildTailFileCommand(endPath));
            fileContents = result.getOutputStream().getBytes(Charsets.UTF_8.name());
        } catch (ExecutionException e) {
            throw new MIFException("Problem retrieving file: Invoker failure executing: " + buildTailFileCommand(endPath), e);
        } catch (UnsupportedEncodingException e) {
            throw new MIFException("Problem dealing with file encoding: Invoker failure executing: " + buildTailFileCommand(endPath), e);
        }
        return fileContents;
    }

    @Override
    public byte[] getFullFileContents(String jobId, String endPath) throws MIFException {
        Job job = jobManagementService.getJob(jobId);
        if (null == job) {
            return EMPTY_FILE_CONTENT;
        }
        return MIFRESTServiceUtils.getFileContentsForJob(invokerHelperFactory.create(job.getInvoker()), new File(getRootFolder(), endPath));
    }

    /**
     * Tails the specified file by a measure of configured max bytes.
     * 
     * @param jobId
     * @param workingDir
     */
    private String buildTailFileCommand(String filePath) {
        StringBuilder sb = new StringBuilder();
        sb.append(TAIL_CMD);
        sb.append(tailFileMaxSizeInBytes);
        sb.append(" \"");
        sb.append("/");
        sb.append(filePath);
        sb.append("\"; ");
        return sb.toString();
    }

    /**
     * Returns all valid files based on the ignore patterns supplied, Currently
     * only checks suffixes to ignore.
     * 
     * @param dir
     * @return
     * @return
     */
    private ArrayList<String> getValidFiles(List<String> files) {
        ArrayList<String> validFiles = new ArrayList<String>();
        for (Iterator<String> i = files.iterator(); i.hasNext();) {
            String file = i.next();
            if (notBlackListed(file)) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }

    /**
     * Checks if the file is not blacklisted based on the ignore patterns set
     * for suffices and prefices.
     * 
     * @param file
     * @return
     */
    private boolean notBlackListed(String file) {
        for (String pattern : fileTypeBlackList) {
            if (file.endsWith(pattern)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JobSummaryMessages getRunningJobsSummaryMessages() {
        Stopwatch watch = new Stopwatch();
        watch.start();
        JobSummaryMessages jobSummaryMessages = new JobSummaryMessages();
        try {
            jobSummaryMessages.setSummaryMessages(jobManagementService.getSummaryMessagesForRunningJobs());
        } catch (Throwable t) {
            LOG.error("error retrieving job summary messages", t);
            jobSummaryMessages.setStatus(ResponseStatus.FAILURE);
            jobSummaryMessages.setErrorMessage(t.getMessage());
        } finally {
            watch.stop();
            long timeTaken = watch.elapsedMillis();
            LOG.debug("Took " + timeTaken + " ms to query for " + getNumberOfSummaries(jobSummaryMessages)
                + " running jobs summary messages");
        }
        return jobSummaryMessages;
    }

    /**
     * @return the number of summaries within the job summary messages object
     */
    private int getNumberOfSummaries(JobSummaryMessages jobSummaryMessages) {
        return (jobSummaryMessages.getSummaryMessages() == null) ? 0 : jobSummaryMessages.getSummaryMessages().size();
    }

    @Override
    public DetailedStatus getDetailedStatus(String jobId) throws MIFException {
        LOG.debug("Retrieving Detailed status for request " + jobId);
        Job job = MIFRESTServiceUtils.getJob(getJobManagementService(),jobId);
        DetailedStatus detailedStatus = MIFRESTServiceUtils.retrieveDetailedStatus(job);
        return detailedStatus;
    }

    @Override
    public String execute(String executionRequest) throws MIFException {
        LOG.info("Handling REST Execution Request "+ executionRequest);
        if (StringUtils.isEmpty(executionRequest)) {
            throw new MIFException("Could not parse the 'executionRequest' parameter from the body");
        }                      
                        
        ExecutionRequest request = MessagingHelper.parseExecutionRequest(executionRequest);
        Connector connector = connectorsRegistry.getConnectorByExecutionType(request.getType());
        if (connector != null) {
            jmsTemplate.send(this.requestQueue, new TextMessageCreator(executionRequest));
        } else {
            throw new MIFException("A Connector for " + request.getType() + " was not found.");
        }
        
        return request.getRequestId();
    }
    @Override
    public String indextext() {
        return schema();
    }  
    
    @Override
    public String indexweb() {
        
        StringBuilder builder = new StringBuilder();
        builder.append("Execution Request/Response XSD:<br><textarea rows='40' cols='100' style='border:none'>");
        builder.append(schema());
        builder.append("</textarea>");
        return builder.toString();
    }

    private String schema() {
        try {            
            return MIFSchemaGen.getInstance().generateSchema();           
        } catch (JAXBException e) {
            LOG.error("Exception thrown generating schema", e);
        } catch (IOException e) {
            LOG.error("Exception thrown ", e);
        }
        return "Could not generate MIF Schema";
    }
    

    @Override
    public String[] getAcknowledgedJobsByDate(Long dateTo) {
        return jobManagementService.getAcknowledgedJobsToDate(dateTo);
    }

    @Override
    public ExecutionResponse getExecutionResponse(String jobId) throws MIFException {
        Job job = MIFRESTServiceUtils.getJob(getJobManagementService(),jobId);
        ExecutionResponse executionResponse = null;
        executionResponse = job.getExecutionResponse();
        if (executionResponse == null) {
            LOG.debug("Execution response for job " + job.getJobId() + " does not exist");
            throw new MIFException(String.format("Execution response for job %s does not exist. ", job.getJobId()));
        }
        return executionResponse;
    }

    @Override
    public MIFResponse cancel(String jobId) {
        LOG.debug(String.format("Client requested cancellation of a job with ID %s ", jobId));
        MIFResponse response = new MIFResponse();
        if (StringUtils.isEmpty(jobId)) {
            response.setStatus(ResponseStatus.FAILURE);
            response.setErrorMessage("Request ID was not set.");
            return response;
        }
        Job job = jobManagementService.getJob(jobId);
        if (job == null) {
            response.setStatus(ResponseStatus.FAILURE);
            response.setErrorMessage("Job with Request ID " + jobId + " does not exist.");
            return response;
        }

        Connector connector = connectorsRegistry.getConnectorById(job.getConnectorId());
        if (connector != null) {
            jmsTemplate.send(connector.getCancellationQueue(), new TextMessageCreator(jobId));
        } else {
            response.setStatus(ResponseStatus.FAILURE);
            String msg = String.format("Internal server error - connector %s referenced by a job %s was not found.", job.getConnectorId(),
                jobId);
            response.setErrorMessage(msg);
            LOG.error(msg);
        }

        return response;
    }

    public JobManagementService getJobManagementService() {
        return jobManagementService;
    }

    @Required
    public void setJobManagementService(JobManagementService jobManagementService) {
        this.jobManagementService = jobManagementService;
    }

    public Collection<String> getFileTypeBlackList() {
        return fileTypeBlackList;
    }

    public void setFileTypeBlackList(Collection<String> fileTypeBlackList) {
        this.fileTypeBlackList = fileTypeBlackList;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public int getTailFileMaxSizeInBytes() {
        return tailFileMaxSizeInBytes;
    }

    public void setTailFileMaxSizeInBytes(int tailFileMaxSizeInBytes) {
        this.tailFileMaxSizeInBytes = tailFileMaxSizeInBytes;
    }

    public ConnectorsRegistry getConnectorsRegistry() {
        return connectorsRegistry;
    }

    @Required
    public void setConnectorsRegistry(ConnectorsRegistry connectorsRegistry) {
        this.connectorsRegistry = connectorsRegistry;
    }

    @Required
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    @Required
    public void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }
    
    public String getRequestQueue() {
        return requestQueue;
    }

    public InvokerHelperFactory getInvokerHelperFactory() {
        return invokerHelperFactory;
    }
    @Required
    public void setInvokerHelperFactory(InvokerHelperFactory invokerHelperFactory) {
        this.invokerHelperFactory = invokerHelperFactory;
    }
}