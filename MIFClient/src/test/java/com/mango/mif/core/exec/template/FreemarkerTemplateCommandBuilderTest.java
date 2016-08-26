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
package com.mango.mif.core.exec.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mango.mif.core.exec.ExecutionException;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * 
 * Tests {@link FreemarkerTemplateCommandBuilder}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FreemarkerTemplateCommandBuilderTest {
    private static final Logger LOG = Logger.getLogger(FreemarkerTemplateCommandBuilderTest.class);

    private FreemarkerTemplateCommandBuilder commandBuilder;

    @Mock
    FreemarkerConfigurationProvider configurationProvider;
    
    @Before
    public void setUp() throws ExecutionException {
        commandBuilder = new FreemarkerTemplateCommandBuilder();
        commandBuilder.setFreemarkerConfigurationProvider(configurationProvider);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfTemplateNotSet() throws ExecutionException {
        when(configurationProvider.getConfiguration()).thenReturn(mock(Configuration.class));
        commandBuilder.getCommand();
    }

    @Test(expected = ExecutionException.class)
    public void shouldThrowExceptionIfTemplateFileDoesNotExist() throws ExecutionException  {
        when(configurationProvider.getConfiguration()).thenReturn(mock(Configuration.class));
        commandBuilder.setTemplate("DOES-NOT-EXIST.FTL");
        commandBuilder.getCommand();
    }

    @Test
    public void shouldAddAnEntryToThePassedMap() throws ExecutionException, IOException  {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/ADD-ENTRY-TO-MAP.FTL");
        File templateLocation = FileUtils.toFile(url).getParentFile();
        Configuration freeMarkerConfig = new Configuration();
        freeMarkerConfig.setTemplateLoader(new FileTemplateLoader(templateLocation));
        when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
        
        commandBuilder.setTemplate("ADD-ENTRY-TO-MAP.FTL");
        Map<String,String> map = Maps.newHashMap();
        map.put("variable2", "variable from map");
        commandBuilder.setVariable("map", map);
        commandBuilder.setVariable("variable", " test variable ");
        commandBuilder.getCommand();
        assertNotNull(map.get("NEW-ENTRY"));
    }
    
    @Test
    public void shouldProduceATextWithDefaultValues() throws ExecutionException, IOException  {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/DEFAULT-VALUE.FTL");
        File templateLocation = FileUtils.toFile(url).getParentFile();
        Configuration freeMarkerConfig = new Configuration();
        freeMarkerConfig.setTemplateLoader(new FileTemplateLoader(templateLocation));
        when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
        
        commandBuilder.setTemplate("DEFAULT-VALUE.FTL");
        commandBuilder.setVariable("defaultValue", "defaultValue");
        commandBuilder.setVariable("emptyExpression", "");
        LOG.debug(commandBuilder.getCommand());
        assertEquals("defaultValue\nnot empty\nnot null\ndefaultValue\ndefaultValue",commandBuilder.getCommand().trim());
    }
    
    
    @Test
    public void shouldResolveTemplatesFromMultipleLocations() throws IOException, ExecutionException {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File locationWithSubdirs = FileUtils.toFile(url).getParentFile().getParentFile();
        
        url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/location/TEST.FTL");
        File locationPlain = FileUtils.toFile(url).getParentFile();
        
        TemplateLoader[] loaders = new TemplateLoader[] { new FileTemplateLoader(locationWithSubdirs), new FileTemplateLoader(locationPlain) };
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        
        Configuration freeMarkerConfig = new Configuration();
        freeMarkerConfig.setTemplateLoader(mtl);
        when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
        
        commandBuilder.setTemplate("TEST.FTL");
        String command = commandBuilder.getCommand();
        LOG.debug(command);
        assertEquals("TEMPLATE_LOCATED_DIRECTLY_IN_TEMPLATES_LOCATION",command);
        
        commandBuilder.setTemplate("/subdir/TEST_2.FTL");
        command = commandBuilder.getCommand();
        LOG.debug(command);
        assertEquals("TEMPLATE_LOCATED_IN_A_SUBDIRECTORY_OF_TEMPLATES_LOCATION",command);
    }
    
    @Test
    public void shouldResolveTemplateInclude() throws IOException, ExecutionException {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File locationWithSubdirs = FileUtils.toFile(url).getParentFile().getParentFile();
        
        url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/location/TEST.FTL");
        File locationPlain = FileUtils.toFile(url).getParentFile();
        
        TemplateLoader[] loaders = new TemplateLoader[] { new FileTemplateLoader(locationWithSubdirs), new FileTemplateLoader(locationPlain) };
        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
        
        Configuration freeMarkerConfig = new Configuration();
        freeMarkerConfig.setTemplateLoader(mtl);
        when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
        
        commandBuilder.setTemplate("TEST_IMPORT.FTL");
        String command = commandBuilder.getCommand();
        LOG.debug(command);
        assertEquals("TEMPLATE_LOCATED_DIRECTLY_IN_TEMPLATES_LOCATION TEMPLATE_LOCATED_IN_A_SUBDIRECTORY_OF_TEMPLATES_LOCATION",command);
    }
    
    @Test
    public void shouldRetrieveNewTemplateInstanceAfterASecond() throws IOException, ExecutionException, InterruptedException {
        URL template1 = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/location/TEST.FTL");
        URL template2 = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File file1 = FileUtils.toFile(template1);
        File file2 = FileUtils.toFile(template2);
        File tmpDir = null;
        try {
            tmpDir = Files.createTempDir();
            File tmpTemplate = new File(tmpDir,file1.getName());
            Files.copy(file1, tmpTemplate);
            
            Configuration freeMarkerConfig = new Configuration();
            freeMarkerConfig.setTemplateLoader(new FileTemplateLoader(tmpDir));
            freeMarkerConfig.setTemplateUpdateDelay(0);
            when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
            commandBuilder.setTemplate(tmpTemplate.getName());
            
            String beforeChange = commandBuilder.getCommand();
            LOG.debug(String.format("template before change %s", beforeChange));
            
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // to make sure that change will be captured 
            Files.copy(file2, tmpTemplate);
            
            String afterChange = commandBuilder.getCommand();
            LOG.debug(String.format("template after change %s", afterChange));
            
            assertFalse("The template was not reloaded after the change",StringUtils.equals(beforeChange,afterChange));
        } finally {
            if(tmpDir!=null) {
                FileUtils.forceDelete(tmpDir);
            }
        }
    }

    @Test
    public void shouldCacheATemplateForMoreThanFiveSeconds() throws IOException, ExecutionException, InterruptedException {
        URL template1 = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/location/TEST.FTL");
        URL template2 = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File file1 = FileUtils.toFile(template1);
        File file2 = FileUtils.toFile(template2);

        File tmpDir = null;
        try {
            tmpDir = Files.createTempDir();
            File tmpTemplate = new File(tmpDir,file1.getName());
            Files.copy(file1, tmpTemplate);
            
            Configuration freeMarkerConfig = new Configuration();
            freeMarkerConfig.setTemplateLoader(new FileTemplateLoader(tmpDir));
            freeMarkerConfig.setTemplateUpdateDelay(5);
            when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
            commandBuilder.setTemplate(tmpTemplate.getName());
            
            String beforeChange = commandBuilder.getCommand();
            LOG.debug(String.format("template before change %s", beforeChange));
            
            Thread.sleep(TimeUnit.SECONDS.toMillis(1)); // to make sure that change will be captured 
            Files.copy(file2, tmpTemplate);
            
            String afterChange = commandBuilder.getCommand();
            LOG.debug(String.format("template after change %s", afterChange));
            
            assertTrue("The template was reloaded after the change",StringUtils.equals(beforeChange,afterChange));
        } finally {
            if(tmpDir!=null) {
                FileUtils.forceDelete(tmpDir);
            }
        }
    }
    
    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfConfigurationIsNotSet() throws ExecutionException {
        commandBuilder.setFreemarkerConfigurationProvider(null);
        commandBuilder.setTemplate("TEMPLATE-THAT-DOES-NOT-EXIST");
        commandBuilder.getCommand();
        
    }
    
    @Test(expected=ExecutionException.class)
    public void resolveTemplate_shouldThrowExceptionWhenTemplateFileDoesntExist() throws IOException, ExecutionException {
        Configuration freeMarkerConfig = mock(Configuration.class);
        doThrow(IOException.class).when(freeMarkerConfig).getTemplate((String)any());
        when(configurationProvider.getConfiguration()).thenReturn(freeMarkerConfig);
        commandBuilder.resolveTemplate("NON-EXISTING-TEMPLATE");
    }

    @Test(expected=ExecutionException.class)
    public void processTemplate_shouldThrowExceptionWhenTemplateCantBeProcessedDueToIOException() throws IOException, ExecutionException, TemplateException {
        Template template = mock(Template.class);
        doThrow(IOException.class).when(template).process((Object)any(),(Writer)any());
        
        commandBuilder.setTemplate("NON-EXISTING-TEMPLATE");
        commandBuilder.processTemplate(template);
    }
    
    @Test(expected=ExecutionException.class)
    public void processTemplate_shouldThrowExceptionWhenTemplateCantBeProcessed() throws IOException, ExecutionException, TemplateException {
        Template template = mock(Template.class);
        doThrow(TemplateException.class).when(template).process((Object)any(),(Writer)any());
        
        commandBuilder.setTemplate("NON-EXISTING-TEMPLATE");
        commandBuilder.processTemplate(template);
    }
    

}
