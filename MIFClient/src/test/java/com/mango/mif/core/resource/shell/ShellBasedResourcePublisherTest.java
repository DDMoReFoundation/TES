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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.mango.mif.client.domain.ExecutionRequestAttributeName;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.template.FreemarkerTemplateCommandBuilder;
import com.mango.mif.core.resource.ResourceCopier;

/**
 * Test for {@link ShellBasedResourcePublisher}.
 * 
 */
public class ShellBasedResourcePublisherTest {

    private static final String ROOT_DIRECTORY = "test";
    private static final String PROJ_DIRECTORY = ROOT_DIRECTORY + File.separator + " project";

    FreemarkerTemplateCommandBuilder commandBuilder;

    Invoker invoker;

    ResourceCopier resourceCopier;

    @Before
    public void setUp() {
        invoker = mock(Invoker.class);
        resourceCopier = mock(ResourceCopier.class);
        commandBuilder = mock(FreemarkerTemplateCommandBuilder.class);
    }

    @Test
    public void shouldConstructPublisher() {
        PublisherParameters shellBasedResourcePublisherParameters = new PublisherParameters();
        shellBasedResourcePublisherParameters.setCommandBuilder(commandBuilder);
        shellBasedResourcePublisherParameters.setInvoker(invoker);
        shellBasedResourcePublisherParameters.setResourceCopier(resourceCopier);
        shellBasedResourcePublisherParameters.setRootDirectory(new File(
                PROJ_DIRECTORY));
        shellBasedResourcePublisherParameters.addRequestAttribute(ExecutionRequestAttributeName.EXECUTION_HOST_FILESHARE.getName(), "mock-location");

        ShellBasedResourcePublisher shellBasedResourcePublisher = new ShellBasedResourcePublisher(
                shellBasedResourcePublisherParameters);

        assertNotNull(shellBasedResourcePublisher);
    }

    @Test(expected=RuntimeException.class)
    public void ShouldThrowExceptionWhenInvokerIsnotSetDuringPublisherConstruction() {
        PublisherParameters shellBasedResourcePublisherParameters = new PublisherParameters();
        new ShellBasedResourcePublisher(shellBasedResourcePublisherParameters);
    }

    @Test(expected=RuntimeException.class)
    public void ShouldThrowExceptionWhenCommandBuilderIsnotSetDuringPublisherConstruction() {
        PublisherParameters shellBasedResourcePublisherParameters = new PublisherParameters();
        shellBasedResourcePublisherParameters.setInvoker(invoker);
        new ShellBasedResourcePublisher(shellBasedResourcePublisherParameters);
    }

}
