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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.mango.mif.client.api.rest.FileList;
import com.mango.mif.client.api.rest.FileSystemItem;
import com.mango.mif.client.api.rest.JobServiceException;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.ce.CeInvoker;
import com.mango.mif.core.exec.invoker.InvokerHelper;
import com.mango.mif.core.exec.invoker.InvokerHelperFactory;
import com.mango.mif.core.services.JobManagementService;
import com.mango.mif.exception.MIFException;
import com.mango.mif.utils.SystemPropertiesPreservingTestStub;
import com.mango.mif.utils.TestPropertiesConfigurator;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * Integration Test for {@ JobServiceImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest extends SystemPropertiesPreservingTestStub {

    protected static final Log logger = LogFactory.getLog(JobServiceTest.class);

    /**
     * RESTful service implementation under test.
     */
    @InjectMocks
    protected JobServiceImpl jobService = new JobServiceImpl();

    private InvokerHelper invokerHelper;

    @Mock
    Job job;

    @Mock
    JobManagementService jobManagementService;

    @Mock
    InvokerHelperFactory invokerHelperFactory;

    @Mock
    MessageContext messageContext;

    @Mock
    UriInfo uriInfo;

    private static final String JOB_ID = "REQUEST_ID";

    private Invoker invoker;

    private static final String JOB_DIRECTORY = "/tmp/job";

    @BeforeClass
    public static void beforeAll() throws ExecutionException, IOException {
        TestPropertiesConfigurator.initProperties();
    }

    @Before
    public void setUp() throws EncryptionException {
        invoker = new CeInvoker();
        invokerHelper = new InvokerHelper(invoker);
        jobService.setTailFileMaxSizeInBytes(1000);
        when(jobManagementService.getJob(JOB_ID)).thenReturn(job);
        when(job.getJobDirectory()).thenReturn(new File(JOB_DIRECTORY));
        when(job.getInvoker()).thenReturn(invoker);
        when(invokerHelperFactory.create(invoker)).thenReturn(invokerHelper);

        try {
            // Setup the test file structure
            invokerHelper.runAndReportFailures(buildCreateTestFilesCommand());
        } catch (ExecutionException e) {
            logger.error("Exception during test setup - problem with Invoker: ", e);
            fail("Exception during test setup: " + e.getMessage());
        }
    }

    @Test
    public void shouldReturnFileLinks() throws JobServiceException, MIFException {
        FileList fileLinks = null;

        fileLinks = jobService.getFileItemsList(invokerHelper, JOB_DIRECTORY, "folder1", "REQUEST_URI", "folder1");

        assertNotNull(fileLinks);
        boolean folder2Found = containsFile("folder2", fileLinks);
        assertTrue(folder2Found);
    }

    /**
     * @param string
     * @param fileLinks
     * @return
     */
    private boolean containsFile(final String fileName, FileList fileLinks) {
        return Collections2.filter(fileLinks.getFileSystemItems(), new Predicate<FileSystemItem>() {

            public boolean apply(FileSystemItem item) {
                return fileName.equals(item.getName());
            };
        }).size() > 0;
    }

    @Test
    public void shouldReturnFileContent() throws MIFException {
        byte[] content = null;

        content = jobService.getTailedFileContents(JOB_ID, JOB_DIRECTORY + "/folder1/folder2/2bFile");

        assertNotNull(content);
        assertTrue(content.length>0);
    }

    private String buildCreateTestFilesCommand() {

        StringBuilder sb = new StringBuilder();
        sb.append("mkdir \"" + JOB_DIRECTORY + "");
        sb.append("\"; cd \"");
        sb.append(JOB_DIRECTORY);
        sb.append("\"; mkdir \"folder1");
        sb.append("\"; cd \"");
        sb.append("folder1");
        sb.append("\"; mkdir \"folder2");
        sb.append("\"; cd \"");
        sb.append("folder2");
        sb.append("\"; dir > \"2aFile");
        sb.append("\"; dir > \"2bFile");
        sb.append("\"; dir > \"2cFile");
        sb.append("\"; mkdir \"folder3");
        sb.append("\"; cd \"");
        sb.append("folder3");
        sb.append("\"; dir > \"3aFile");
        sb.append("\"; dir > \"3bFile");
        sb.append("\"; dir > \"3cFile\";");

        return sb.toString();
    }

    private String buildDeleteTestFilesCommand() {

        StringBuilder sb = new StringBuilder();
        sb.append("rm -Rf \"" + JOB_DIRECTORY + "\"");
        return sb.toString();
    }

    @After
    public void tearDown() {
        //Destroy test file structure and /tmp file that's used to test the tail method of the service
        try {
            invokerHelper.runAndReportFailures(buildDeleteTestFilesCommand());
        } catch (ExecutionException e) {
            logger.error("Exception during test teardown - problem with Invoker: ", e);
            fail("Exception during test teardown: " + e.getMessage());
        }
    }

}
