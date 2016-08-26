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
package com.mango.mif.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.domain.ExecutionRequestBuilder;
import com.mango.mif.domain.JobStatus;
import com.mango.mif.utils.encrypt.DesEncrypter;
import com.mango.mif.utils.encrypt.Encrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * 
 * Methods used in tests
 */
public class TestsHelper {
    /**
     * Logger
     */
    public final static Logger LOG = Logger.getLogger(TestsHelper.class);

    public static final String MINIMUM_EXECUTION_REQUEST_MSG = "";

    /**
     * Creates a Job
     * @param jobId
     * @param clientRequestMsg
     * @return
     * @throws EncryptionException 
     */
    public static Job createJob(String jobId, String requestId) throws EncryptionException {
        Job job = new Job();
        job.setJobId(jobId);
        try {
            // use default encrypter
            Encrypter encrypter = new DesEncrypter(null);
            
            String executionRequest = new ExecutionRequestBuilder().setRequestId(requestId)
                    .setExecutionType("connector-id")
                    .setUserName("test-user")
                    .setUserPassword(encrypter.encrypt("test-password"))
                    .setGridHostPreamble("# dummy grid host preamble (see TestsHelper.java)")
                    .setSubmitHostPreamble("# dummy submit host preamble (see TestsHelper.java)")
                    .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "MOCK_VALUE")
                    .addAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE_REMOTE.getName(), "MOCK_VALUE")
                    .getExecutionRequestMsg();
            LOG.info("Execution request: " + executionRequest);
            job.setExecutionRequestMsg(executionRequest);
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating test job object.",e);
        }
        job.setClientRequestStatus(JobStatus.NEW.name());
        return job;
    }

    /**
     * Creates working directory
     * @return
     */
    public static File createTmpDirectory() {
        File dir = Files.createTempDir();
        dir.deleteOnExit();
        return dir;
    }

    /**
     * 
     * @return
     */
    public static File createTmpDirectory(String name) {
        Preconditions.checkNotNull(name);
        String tmpDirName = System.getProperty("java.io.tmpdir");
        Preconditions.checkNotNull(tmpDirName);
        File tmpDir = new File(tmpDirName);
        File result = new File(tmpDir,name);
        LOG.info("Created temporary directory:" + result);
        if(result.exists()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        result.mkdirs();
        result.deleteOnExit();
        return result;
    }

    /**
     * @return A suitable name for a temporary directory, which is unlikely to (but nevertheless may) exist.
     */
    public static String generateTempDirectoryPath() {
        return generateTempDirectoryPath(null);
    }

    /**
     * @param append Any suitable string which will be appended onto the directory name
     * @return A suitable name for a temporary directory, which is unlikely to (but nevertheless may) exist.
     */
    public static String generateTempDirectoryPath(String append) {
        String tmpDirName = System.getProperty("java.io.tmpdir");
        Preconditions.checkNotNull(tmpDirName);
        File temp = new File(tmpDirName, "" + System.currentTimeMillis() + (append == null ? "" : append));
        return temp.getAbsolutePath();
    }

    /**
     * Creates a directory using a given invoker helper.
     * @param path The directory to create.
     * @param invoker The invoker to create it with.
     * @return File object containig path of directory.
     * @throws ExecutionException
     */
    public static File createTmpDirectory(String path, InvokerHelper invokerHelper) throws ExecutionException {
        invokerHelper.mkdir(path, 0777);
        LOG.debug("Temp directory created: " + path);
        return new File(path);
    }
    
    /**
     * Creates a Properties object from input stream. Ensures that the input stream is closed.
     * @param inStream
     * @return properties
     * @throws IOException
     */
    public final static Properties createPropertiesFromStream(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        try {
            properties.load(inStream);
        } finally {
            IOUtils.closeQuietly(inStream);
        }
        return properties;
    }
}
