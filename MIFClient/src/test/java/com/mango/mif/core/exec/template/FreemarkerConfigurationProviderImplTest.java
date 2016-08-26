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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import static org.junit.Assert.*;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;


/**
 * tests {@link FreemarkerConfigurationProviderImpl}
 */
public class FreemarkerConfigurationProviderImplTest {

    @Test
    public void setTemplateLocations_shouldSetTemplateLoadersForListOfLocations() {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File templateLocation = FileUtils.toFile(url).getParentFile().getParentFile();
        
        url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/location/TEST.FTL");
        File templateLocation2 = FileUtils.toFile(url).getParentFile();
        
        Configuration mockConfiguration = mock(Configuration.class);
        FreemarkerConfigurationProviderImpl instance = new FreemarkerConfigurationProviderImpl(mockConfiguration);
        String[] locations = new String[] { templateLocation.getAbsolutePath(), templateLocation2.getAbsolutePath() };
        instance.setTemplateLocations(locations);
        
        verify(mockConfiguration).setTemplateLoader((MultiTemplateLoader)any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setTemplateLocations_shouldThrowExceptionIfTemplateLocationCantBeUsed() {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/ADD-ENTRY-TO-MAP.FTL");
        File notExistingTemplateLocation = new File(FileUtils.toFile(url).getParentFile(),"not-existing-directory");
        Configuration mockConfiguration = mock(Configuration.class);
        FreemarkerConfigurationProviderImpl instance = new FreemarkerConfigurationProviderImpl(mockConfiguration);
        instance.setTemplateLocations(new String[] { notExistingTemplateLocation.getAbsolutePath() });
    }
    
    @Test
    public void createLoader_shouldReturnClassTemplateLoader() throws IOException {
        Configuration mockConfiguration = mock(Configuration.class);
        FreemarkerConfigurationProviderImpl instance = new FreemarkerConfigurationProviderImpl(mockConfiguration);
        TemplateLoader templateLoader = instance.createLoader("classpath:/com/mango/mif/core/exec/template/");
        assertTrue(templateLoader instanceof ClassTemplateLoader);
    }

    @Test
    public void createLoader_shouldReturnFileTemplateLoader() throws IOException {
        URL url = FreemarkerTemplateCommandBuilderTest.class.getResource("/com/mango/mif/core/exec/template/locations/locationWithSubDirs/subdir/TEST_2.FTL");
        File templateLocation = FileUtils.toFile(url).getParentFile().getParentFile();
        Configuration mockConfiguration = mock(Configuration.class);
        FreemarkerConfigurationProviderImpl instance = new FreemarkerConfigurationProviderImpl(mockConfiguration);
        TemplateLoader templateLoader = instance.createLoader(templateLocation.getAbsolutePath());
        assertTrue(templateLoader instanceof FileTemplateLoader);
    }
}
