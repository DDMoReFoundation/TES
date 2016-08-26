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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.TemplatedCommandBuilder;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * A command builder that uses Freemarker to build up commands
 */
public class FreemarkerTemplateCommandBuilder extends TemplatedCommandBuilder {

    private FreemarkerConfigurationProvider configurationProvider;

    /**
     * Creates a freemarker template command builder from nothing.
     * 
     * As you will see elsewhere (in the CommandBuilder class, for example) there are some variables
     * (like SHELL) which we just absolutely, utterly need.  What we do here is to provide default
     * fallback values for these variables.
     *
     * These values can be overridden by Spring (which effectively they will be in the CTOR below,
     * where the map values come directly from Spring) or they can come from request attributes
     * which are, confusingly, handled in the parent class.
     */
    public FreemarkerTemplateCommandBuilder() throws ExecutionException {
        setContext(CommandBuilderContextBuilder.createInitContext());
    }
    /**
     * Creates a command builder with the given initial context
     * @param initialContext
     * @throws ExecutionException
     */
    public FreemarkerTemplateCommandBuilder(Map<String, Object> initialContext) throws ExecutionException {
        this();
        for (Entry<String, Object> en : initialContext.entrySet()) {
            setVariable(en.getKey(), en.getValue());
        }
    }

    /**
     * Expand the template into a command.
     */
    @Override
    public String getCommand() throws ExecutionException {
        Preconditions.checkState(!StringUtils.isBlank(getTemplate()), "Command template is not set or is blank.");
        Preconditions.checkState(configurationProvider != null, "Freemarker configuration provider is not set");
        Preconditions.checkState(getContext()!=null, "Command builder context was null");
        Template template = resolveTemplate(getTemplate());
        return processTemplate(template);
    }

    /**
     * @param template
     * @return a processed template
     * @throws ExecutionException if template couldn't be processed
     */
    String processTemplate(Template template) throws ExecutionException {
        StringWriter writer = new StringWriter();
        try {
            Map<String,Object> wrappedContext = wrapContextVariables();
            template.process(wrappedContext, writer);
        } catch (IOException ioe) {
            throw new ExecutionException(String.format("Caught IO exception while processing template %s ", template.getName()), ioe);
        } catch (TemplateException te) {
            throw new ExecutionException(String.format("Caught TemplateException while processing template %s ", template.getName()), te);
        }
        return writer.toString();
    }

    /**
     * wraps variables in the context so they can be injected into Freemarker context
     * @return
     */
    private Map<String,Object> wrapContextVariables() {
        Preconditions.checkNotNull(getContext(),"Context was not set on the command builder");
        final Map<String,Object> wrappedVariables = Maps.newHashMap();
        for(Map.Entry<String, Object> en :getContext().getVariables().entrySet()) {
            BeansWrapper beansWrapper = BeansWrapper.getDefaultInstance();
            beansWrapper.setExposureLevel(BeansWrapper.EXPOSE_SAFE);
            try {
                TemplateModel model = beansWrapper.wrap(en.getValue());
                wrappedVariables.put(en.getKey(), model);
            } catch (TemplateModelException e) {
                throw new IllegalStateException(String.format("Failed to wrap object %s for freemarker",en.getValue().getClass().getCanonicalName() ), e);
            }
        }
        return wrappedVariables;
    }
    
    /**
     * @param templateName
     * @return freemarker template
     * @throws ExecutionException  if freemarker template couldn't be resolved
     * 
     */
    Template resolveTemplate(String templateName) throws ExecutionException {
        Template template;
        Configuration configuration = configurationProvider.getConfiguration();
        try {
            template = configuration.getTemplate(templateName);
        } catch (IOException e) {
            throw new ExecutionException(String.format("Caught IO exception while processing template %s.", templateName), e);
        }
        if (template == null) {
            throw new ExecutionException(String.format("Unable to retrieve template for %s ", templateName));
        }
        return template;
    }

    @Override
    public String toString() {
        String resolved;
        try {
            resolved = String.format("Template: [ %s ] Template Loader: [ %s ] Command: [ %s ]", getTemplate(), getFreemarkerConfigurationProvider().getConfiguration()
                    .getTemplateLoader(), getCommand());
        } catch (Exception ignored) {
            resolved = String.format("Template: [ %s ] Template Loader: [ %s ] Could not be resolved. ", getTemplate(), getFreemarkerConfigurationProvider().getConfiguration()
                    .getTemplateLoader());
        }

        return resolved;
    }

    public FreemarkerConfigurationProvider getFreemarkerConfigurationProvider() {
        return configurationProvider;
    }

    public void setFreemarkerConfigurationProvider(FreemarkerConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }
}
