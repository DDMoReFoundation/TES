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
package com.mango.mif.client.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.client.api.TaskExecutionManager;
import com.mango.mif.client.api.TaskExecutionMessageHandler;
import com.mango.mif.client.api.rest.JobService;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.api.ResourceUtils;
import com.mango.mif.core.resource.BaseResourcePublisher;
import com.mango.mif.core.resource.fileutils.FileUtilsResourcePublisher;
import com.mango.mif.core.resource.shell.PublisherParameters;
import com.mango.mif.domain.DetailedStatus;
import com.mango.mif.domain.ExecutionRequest;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.ExecutionResponse;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.encrypt.Encrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * 
 * A simple MIF client command line implementation. It takes the following
 * properties: mif.broker.url - an url of broker of MIF server
 * mif.shared.location - a shared location between
 * the client and the server mif.rest.service.url - a rest service URL
 * 
 * 
 * TODO Refactor so it uses org.kohsuke.args4j
 */
public class MIFInvoker implements TaskExecutionMessageHandler {
	final static Logger LOG = Logger.getLogger(MIFInvoker.class);
	
	private TaskExecutionManager taskExecutionManager;
	private JobService jobService;
    private Encrypter encrypter;

	private static String	userName;
	private static String	userPassword;

	/** Used to synchronize submission and requests handling. */
	static CountDownLatch signal = new CountDownLatch(1);

	/** Used to synchronize polling thread and requests handling. */
	static CountDownLatch restServiceSignal = new CountDownLatch(1);

	/**
	 * A directory to which results will be copied
	 */
	private File outputDirectory;
	/**
	 * connector id
	 */
	private String connectorID;
	/**
	 * A directory containing input files for execution
	 */
	private File scriptDirectory;
	/**
	 * Script file
	 */
	private String scriptFile;
	/**
	 * A request ID that was sent to MIF
	 */
	private String requestUUID;
	/**
	 * Execution response received from MIF
	 */
	private ExecutionResponse response;
	/**
	 * Execution request sent to MIF
	 */
	private ExecutionRequest executionRequest;

    private static String executionHostFileshareRemote;
    private static String executionHostFileshare;

    public final static String MIF_BROKER_URL = "mif.broker.url";
	public final static String SHARED_FILES = "mif.shared.location";
	public final static String MIF_REST_SERVICE_URL = "mif.rest.service.url";
	public final static String MIF_CLIENT_USER_NAME = "mif.client.userName";
	public final static String MIF_CLIENT_USER_PASSWORD = "mif.client.password";
	
	private final static String MIF_INVOKER_SPRING_CONTEXT = "com/mango/mif/client/cli/MIFInvoker-context.xml";
	/**
	 * Error exit code
	 */
	private final static int ERROR = -1;
	private final static int DETAILED_STATUS_POLL_RATE = 3;
    public final static String MIF_EXECUTION_HOST_FILESHARE = "mif.execution.host.fileshare";
    public final static String MIF_EXECUTION_HOST_FILESHARE_REMOTE = "mif.execution.host.fileshare.remote";
    public final static String MIF_INVOKER_PROPERTIES = "mif.invoker.properties";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    if(System.getProperties().containsKey(MIF_INVOKER_PROPERTIES)) {
	        loadProperties(System.getProperty(MIF_INVOKER_PROPERTIES),System.getProperties());
	    }
		try {
			checkArguments(args, System.getProperties());
		} catch(Exception e) {
			LOG.error(e);
			printHelp();
			System.exit(ERROR);
		}
		File scriptDir = new File(args[0]);
		MIFInvoker main = createMIFInvoker();
		main.scriptFile = args[1];

		main.connectorID = args[2];
		main.scriptDirectory = scriptDir;
		main.outputDirectory = new File(".").getAbsoluteFile();
		try {
			main.taskExecutionManager.setExecutionMessageHandler(main);
			main.execute();
		} catch (MIFException e) {
			LOG.error(e);
			System.exit(ERROR);
		} catch (Exception e) {
			LOG.error(e);
			System.exit(ERROR);
		}
		System.exit(0);
	}
	/**
     * Loads properties from the file specified by filePath and adds them to the target properties 
     */
    static void loadProperties(String filePath, Properties target) {
        File file = new File(filePath);
        if(!file.exists()) {
            LOG.error(String.format("File %s does not exist.",file));
            return;
        }
        Properties props = new Properties();
        FileInputStream fis = null;
        
        try {
            fis = new FileInputStream(file);
            props.load(fis);
        } catch (Exception e) {
            LOG.error(String.format("Error when reading MIF Invoker properties file %s",file),e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
        
        
        target.putAll(props);
    }
    /**
	 * Creates MIF Invoker instance
	 * @return
	 */
	public static MIFInvoker createMIFInvoker() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext(MIF_INVOKER_SPRING_CONTEXT);
		return ctx.getBean(MIFInvoker.class);
	}
	
	/**
	 * Validates the arguments
	 * @param args
	 * @param properties
	 */
	protected static void checkArguments(String[] args, Properties properties) {
		Preconditions
				.checkArgument(args.length >= 2,
						"Not enough arguments");
		Preconditions
				.checkNotNull(properties.getProperty(MIF_BROKER_URL),
						"Property "+MIF_BROKER_URL+" must be set, e.g. tcp://localhost:61616");

		Preconditions
				.checkNotNull(properties.getProperty(MIF_REST_SERVICE_URL),
						"Property "+MIF_REST_SERVICE_URL+" must be set, e.g. http://localhost:8080/MIFServer/REST/services");
        Preconditions
        .checkNotNull(
                properties.getProperty(MIF_EXECUTION_HOST_FILESHARE),
                "Property "+MIF_EXECUTION_HOST_FILESHARE+" must be set, e.g. /usr/globals/username");
        if(StringUtils.isEmpty(properties.getProperty(MIF_EXECUTION_HOST_FILESHARE_REMOTE))) {
            LOG.warn(String.format("%s property was not set, the value from %s will be used",MIF_EXECUTION_HOST_FILESHARE_REMOTE,MIF_EXECUTION_HOST_FILESHARE));
            properties.setProperty(MIF_EXECUTION_HOST_FILESHARE_REMOTE, properties.getProperty(MIF_EXECUTION_HOST_FILESHARE));
        }
        Preconditions
		.checkNotNull(properties.getProperty(MIF_CLIENT_USER_NAME),
				"Property "+MIF_CLIENT_USER_NAME+" must be set.");
		Preconditions
		.checkNotNull(properties.getProperty(MIF_CLIENT_USER_PASSWORD),
				"Property "+MIF_CLIENT_USER_PASSWORD+" must be set.");
		File scriptDirectory = new File(args[0]);
		Preconditions.checkArgument(scriptDirectory.exists(),
				"Execution request test data directory does not exist.");
        File scriptFile = new File(scriptDirectory,args[1]);
        Preconditions.checkArgument(scriptFile.exists(),
                "Specified script file for execution does not exist.");
		userName = properties.getProperty(MIF_CLIENT_USER_NAME);
		userPassword = properties.getProperty(MIF_CLIENT_USER_PASSWORD);
        executionHostFileshare = properties.getProperty(MIF_EXECUTION_HOST_FILESHARE);
        executionHostFileshareRemote = properties.getProperty(MIF_EXECUTION_HOST_FILESHARE_REMOTE);
		
        // FIXME
        // Shared Location Manager has just been deprecated but it still is used by some of the components
        // and it uses mif.shared.files property, this is why we do the following
        properties.setProperty(SHARED_FILES, executionHostFileshare);
	}
	/**
	 * Prints help
	 */
	private static void printHelp() {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("Usage:").append("\n").append("MIFInvoker").append(" ")
				.append("request-directory").append(" ").append("path/to/script/file/within/request-directory").append(" ").append("connector-id").append(" ").append("executable")
				.append("\n").append("Required System Properties").append("\n")
				.append(MIF_BROKER_URL).append(" - MIF server broker url\n")
				.append(MIF_CLIENT_USER_NAME)
				.append(" - MIF client user name that will be used to run scripts against\n")
				.append(MIF_CLIENT_USER_PASSWORD)
				.append(" - MIF client user password that will be used to run scripts against(unecrypted)\n")
				.append("Example").append("\n")
				.append("java -D"+MIF_BROKER_URL+"=\"tcp://hendrix.mango.local:61616\"")
				.append(" -D"+MIF_EXECUTION_HOST_FILESHARE+"=\"/usr/global/my-directory\"")
				.append(" -cp ./MIFClient.jar:./lib/* com.mango.mif.client.cli.MIFInvoker")
				.append(" ./testData path/relative/to/testData/script.R R_SGE /usr/bin/Rscript");
	
		System.out.println(sBuilder.toString());
	}

	/**
	 * Executes script and waits for its completion
	 * 
	 * @throws JAXBException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws MIFException 
	 * @throws EncryptionException 
	 */
	private void execute() throws JAXBException,
			InterruptedException, IOException, MIFException, EncryptionException {
		requestUUID = sendRequest();
		System.out.println("A request has been sent to MIF. Request UUID is "
				+ requestUUID);
        monitorProgress(requestUUID);
		ResourceUtils.dumpExecutionFiles(outputDirectory, executionRequest, response);
		
		System.out.println("Results are available in :" + outputDirectory);
	}
	
    /**
     * Polls the job service for status updates for a given job
     * @param requestUUID
     * @throws InterruptedException
     * @throws JAXBException 
     */
	protected void monitorProgress(String requestUUID)
			throws InterruptedException, JAXBException {
		LOG.debug(String.format("Retrieving status for request: %s",
				requestUUID));
		DetailedStatus detailedStatus;
		while (!signal.await(DETAILED_STATUS_POLL_RATE, TimeUnit.SECONDS)) {
			String status = jobService.getJobStatus(requestUUID);
			System.out.println(String.format(
					"CURRENT STATUS of the Job with ID %s is %s", requestUUID,
					status));
			try {
				detailedStatus = jobService.getDetailedStatus(requestUUID);
			} catch (MIFException e) {
				LOG.error(String.format(
						"Couldn't retrieve detailed status for a job %s",
						requestUUID), e);
				continue;
			}
			if (detailedStatus != null) {
				System.out.println(detailedStatus.asString());
			} else {
				LOG.error(String.format(
						"Detailed status for request %s was null", requestUUID));
			}
		}
	}

	/**
	 * Prepares execution request and passes it to MIF-client components for
	 * submitting
	 * 
	 * @return
	 * @throws JAXBException
	 * @throws MalformedURLException
	 * @throws MIFException 
	 * @throws EncryptionException 
	 */
	private String sendRequest() throws 
			JAXBException, MalformedURLException, MIFException, EncryptionException {
	    
	    Map<String,String> requestAttributes = Maps.newHashMap();
	    requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), executionHostFileshare);
        requestAttributes.put(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), executionHostFileshareRemote);
	    
	    PublisherParameters publisherParameters = new PublisherParameters();
	    publisherParameters.setRootDirectory(scriptDirectory);
	    publisherParameters.setRequestAttributes(requestAttributes);
	    
        FileUtilsResourcePublisher publisher = new FileUtilsResourcePublisher(publisherParameters);
		ExecutionRequestBuilder executionRequestBuilder = createExecutionRequestBuilderForInputDirectory(publisher);
		executionRequestBuilder.setExecutionType(connectorID);
        executionRequestBuilder.setUserName(userName);
        executionRequestBuilder.setUserPassword(encrypter.encrypt(userPassword)).setSubmitAsUserMode(true);
        executionRequestBuilder.setExecutionFile(scriptFile);
        for(Map.Entry<String, String> en : requestAttributes.entrySet()) {
            executionRequestBuilder.addAttribute(en.getKey(), en.getValue());
        }
        
		executionRequest = executionRequestBuilder.getExecutionRequest();
		taskExecutionManager.submit(executionRequest);
		return executionRequest.getRequestId().toString();
	}
    /**
     * Creates execution request
     * @param requestID 
     * @return
     * @throws JAXBException 
     * @throws MIFException 
     */
    public ExecutionRequestBuilder createExecutionRequestBuilderForInputDirectory(BaseResourcePublisher resourcePublisher) throws JAXBException, MIFException {
        ExecutionRequestBuilder requestBuilder = new ExecutionRequestBuilder();
        List<File> files = ResourceUtils.listFiles(scriptDirectory, new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return true;
            }
        });
        resourcePublisher.addFiles(files);
        String requestID = resourcePublisher.publish();
        requestBuilder.setRequestId(requestID);
        requestBuilder.setExecutionFile(scriptFile);
        return requestBuilder;
    }
    
	@Override
	public void handle(ExecutionResponse executionResponse) {
		try {
			LOG.info("Received message for handling "
					+ executionResponse.getRequestId() + " for handling.");
			if (!executionResponse.getRequestId().toString()
					.equals(requestUUID))
				return;
			response = executionResponse;
			signal.countDown();
		} catch (Exception e) {
			LOG.error(e);
			System.exit(ERROR);
		}
	}

	public void setTaskExecutionManager(
			TaskExecutionManager taskExecutionManager) {
		this.taskExecutionManager = taskExecutionManager;
	}
	
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}
	
	public JobService getJobService() {
		return jobService;
	}
	
	public TaskExecutionManager getTaskExecutionManager() {
		return taskExecutionManager;
	}
    public Encrypter getEncrypter() {
        return encrypter;
    }
    public void setEncrypter(Encrypter encrypter) {
        this.encrypter = encrypter;
    }
}
