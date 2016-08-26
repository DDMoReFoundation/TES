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
package com.mango.mif.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.util.ResourceUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mango.mif.connector.impl.CommandExecutionConnector;


/**
 * Represents a connector and its capabilities (supported execution types).
 * {@link ConnectorDescriptor}s are intended to be dynamically discovered
 * from Connector JAR files that have been dropped in to the connectors
 * directory in MIF.
 * 
 * @author mrogalski
 * @author mwise
 */
public class ConnectorDescriptor {

	private final static Logger LOGGER = Logger.getLogger(ConnectorDescriptor.class);

    private final Connector connector;
    private final Set<String> executionTypes;
    // Keep track of the extracted script files in order to delete them when MIF terminates
    private List<File> extractedScriptFiles = new ArrayList<File>();
    
    /**
     * Constructor
     * @param connector - connector instance
     * @param executionTypes - a set of supported execution types
     */
    public ConnectorDescriptor(final Connector connector, final Set<String> executionTypes) {
        this.connector = connector;
        this.executionTypes = executionTypes;
    }
    
    /**
     * Constructor
     * @param connector - connector instance
     * @param executionType - a single supported execution type
     */
    public ConnectorDescriptor(final Connector connector, final String executionType) {
        this.connector = connector;
        this.executionTypes = Sets.newHashSet(executionType);
    }
    
    public Set<String> getExecutionTypes() {
        return this.executionTypes;
    }

    public Connector getConnector() {
        return this.connector;
    }
    
    /**
     * Given a (normally classpath-based) customScriptsDirectory Spring-configured
     * on the bean, find all script files within this directory (including any
     * sub-directories) within the Connector JAR from which this
     * {@link ConnectorDescriptor} has been loaded. Extract them into an identical
     * relative directory structure outside of the JAR file. Finally, update the
     * Custom Scripts Directory parameter to be the <b>absolute path</b> to the
     * directory containing the extracted copies of the scripts.
     * <p>
     * Thus, FreeMarker templates, that generate shell scripts, can reference
     * and execute these custom scripts if required.
     * <p>
     * If MIF is not in "runtime" mode and thus not dynamically loading Connectors
     * via their JAR files, then the classpath-based customScriptsDirectory will
     * resolve to a physical directory within the source code directory structure.
     * No file extraction is therefore required and the Custom Scripts Directory
     * parameter is just updated to be the absolute path to this directory.
     * <p>
     * The final scenario is if the configured customScriptsDirectory property value
     * is not classpath-based. In this case, it is assumed to reference an absolute or
     * relative path that points to a directory somewhere on the local machine,
     * No extraction from JAR files is therefore done and neither is the
     * customScriptsDirectory updated with an absolute path since it is assumed that
     * this path is already resolvable as-is within a FreeMarker-generated shcll script.
     * <p>
     * @throws IOException if an error occurred during the extraction of script files
     * @throws URISyntaxException if not in "runtime" mode and unable to convert a
     * 							  classpath-based path into a physical location on the filesystem
     */
    @PostConstruct
    @VisibleForTesting
    void extractCustomScripts() throws IOException, URISyntaxException {
    	LOGGER.info("About to extract custom scripts from Connector JAR for connectorId=" + this.connector.getConnectorId() + ", if necessary...");
    	
    	if (! (this.connector instanceof CommandExecutionConnector)) {
    		LOGGER.warn("Connector with connectorId=" + this.connector.getConnectorId() + " is not a CommandExecutionConnector so no custom scripts directory parameter is available.");
    		return;
    	}
    	
    	final String customScriptsLocation = ((CommandExecutionConnector) this.connector).getCommandExecutionTarget().getCustomScriptsDirectory();
		
    	if (StringUtils.isEmpty(customScriptsLocation)) {
    		LOGGER.warn("No custom scripts directory configured for ConnectorDescriptor with connectorId=" + this.connector.getConnectorId() + "; custom scripts will not be available from FreeMarker templates.");
    		return;
    	}
    	LOGGER.debug("Custom Scripts Location = " + customScriptsLocation);
    	
    	if (!customScriptsLocation.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
    		LOGGER.warn("Custom scripts directory configured for ConnectorDescriptor with connectorId=" + this.connector.getConnectorId() + " is not classpath-based; no extraction of custom scripts will be done since it is assumed that this directory is already external to the Connector JAR, and resolvable as-is in FreeMarker-generated scripts.");
    		return;
    	}
    	
    	final String customScriptsDirPath = StringUtils.removeStart(customScriptsLocation, ResourceUtils.CLASSPATH_URL_PREFIX);
    	LOGGER.debug("Custom Scripts Directory path = " + customScriptsDirPath);
    	
    	final String updatedCustomScriptsDirectory;
    	
    	final URL customScriptsDirURL = getClass().getResource(customScriptsDirPath);
    	if (customScriptsDirURL == null) {
    	    LOGGER.warn("Custom Scripts Directory " + customScriptsDirPath + " does not exist on the classpath. i.e. does not exist in the JAR for Connector ID \"" + this.connector.getConnectorId() + "\". Perhaps there are no scripts within it; JAR files cannot have empty directories.");
    	    return;
    	}
    	final URLConnection urlConnection = customScriptsDirURL.openConnection();
    	if (urlConnection instanceof JarURLConnection) {
    		// Do the Extraction...
    	
        	final JarFile jarFile = ((JarURLConnection) urlConnection).getJarFile();
        	final File connectorsDir = new File(jarFile.getName()).getParentFile(); // Target directory to copy files into
        	
        	final Enumeration<JarEntry> jarEntries = jarFile.entries();
        	while (jarEntries.hasMoreElements()) {
        	    final JarEntry jarEntry = jarEntries.nextElement();
        	    
        	    if (jarEntry.getName().startsWith(StringUtils.removeStart(customScriptsDirPath, "/"))) {
    	    		extractCustomScript(jarFile, jarEntry, connectorsDir);
        	    }
        	}
    	
        	// If reached here without any exceptions being thrown then all the scripts have been
        	// successfully extracted; update the Custom Scripts Directory parameter to be the
        	// *absolute path* to the directory containing the extracted copies of the scripts
        	updatedCustomScriptsDirectory = new File(connectorsDir, customScriptsDirPath).getAbsolutePath();
        	
    	} else {
    		LOGGER.info("Not in 'runtime' mode utilising pre-built Connector JARs, so no extraction of custom scripts from such JARs to be done.");
    		
    		updatedCustomScriptsDirectory = new File(customScriptsDirURL.toURI()).getAbsolutePath();
    	}
    	
    	((CommandExecutionConnector) this.connector).getCommandExecutionTarget().setCustomScriptsDirectory(updatedCustomScriptsDirectory);
    	LOGGER.debug("Custom Scripts Location now updated to = " + updatedCustomScriptsDirectory);
    }
    
    /**
     * Extract one script file, or directory, from the Connector JAR into a specified
     * target directory (the connectors directory).
     * <p>
     * @param jarFile - the {@link JarFile} that represents the Connector JAR
     * @param jarEntry - the {@link JarEntry} that represents the script file (or a parent
     * 					 directory thereof) within the {@link JarFile}
     * @param targetDir - the target directory to copy the script file into or in which to
     * 					  create the (sub)directory, maintaining the correct relative path
     * @throws IOException if unable to access the {@link JarFile} or {@link JarEntry} within,
     * 					   or unable to create the {@link File} within in the target directory
     */
    private void extractCustomScript(final JarFile jarFile, final JarEntry jarEntry, final File targetDir) throws IOException {
    	final File destFile = new File(targetDir, jarEntry.getName());
    	LOGGER.info("Extracting " + jarEntry.getName() + " to location " + destFile);
    	if (jarEntry.isDirectory()) {
    		destFile.mkdirs();
    	} else { // Must be a file
    		this.extractedScriptFiles.add(destFile);
    		
    		InputStream inStr = null;
    		FileOutputStream outStr = null;
    		try {
    			inStr = jarFile.getInputStream(jarEntry); // get the input stream
    			outStr = new FileOutputStream(destFile);
    			IOUtils.copy(inStr, outStr); // write contents of the input stream to the output stream
    		} finally {
    			if (outStr != null) {
    				outStr.close();
    			}
				if (inStr != null) {
					inStr.close();
				}
    		}
    	}
    }
    
    /**
     * Tidy up the extracted script files so that once MIF has terminated (<b>if shutdown correctly</b>),
     * only the Connector JAR files remain in the connectors directory.
     * <p>
     * Implementation notes: For each {@link File} that we created as the extraction of a script
     * file from the Connector JAR, we don't just {@link File#delete()} the file itself, but
     * recursively attempt to delete parent directories until we cannot because the directory is
     * not empty. Eventually, after all individual script files have been deleted, this 'stop'
     * directory will be the connectors directory itself which will never be empty and thus
     * delete-able since it holds the connector JAR files themselves. By attempting this
     * recursive parent-directory delete on each and every file, we ensure that eventually
     * (after all {@link ConnectorsDescriptor}s have been shutdown) the structure of
     * remaining empty directories is removed.
     */
    @PreDestroy
    @VisibleForTesting
    void deleteExtractedCustomScripts() {
    	for (File f : this.extractedScriptFiles) {
    		LOGGER.info("Deleting extracted script file: " + f);
    		for ( ; ; f = f.getParentFile() ) {
    			if (!f.delete()) {
    				break;
    			}
    		}
    	}
    }
    
}
