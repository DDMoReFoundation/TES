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
package com.mango.mif.core.resource;

import java.io.File;
import java.util.List;

import com.mango.mif.core.exec.CommandBuilder;
import com.mango.mif.core.exec.Invoker;

/**
 * The Class ResourceCopierValueObject.
 * This acts as a Object that is passed for the creation of the Resource Copier.
 */
public class ResourceCopierParameters {
    /**
     * Destination directory where the result files should be copied into
     */
    protected File destDirectory;

    /**
     * Destination directory where the result files should be copied into
     */
    protected File sourceDirectory;

    /** The Regular Expression pattern to ignore file list. */
    private List<String> fileRegexPatternIgnoreList;

    /** The Directory ignore list. */
    private List<String> directoryIgnoreList;

    /** The command builder. */
    private CommandBuilder commandBuilder;

    /** The invoker to execute the command. */
    Invoker invoker;

    private File filesListing;


    public File getDestDirectory() {
        return destDirectory;
    }

    public void setDestDirectory(File destDirectory) {
        this.destDirectory = destDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public CommandBuilder getCommandBuilder() {
        return commandBuilder;
    }

    public void setCommandBuilder(CommandBuilder commandBuilder) {
        this.commandBuilder = commandBuilder;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public List<String> getFileRegexPatternIgnoreList() {
        return fileRegexPatternIgnoreList;
    }

    public void setFileRegexPatternIgnoreList(
            List<String> fileRegexPatternIgnoreList) {
        this.fileRegexPatternIgnoreList = fileRegexPatternIgnoreList;
    }

    public List<String> getDirectoryIgnoreList() {
        return directoryIgnoreList;
    }

    public void setDirectoryIgnoreList(List<String> directoryIgnoreList) {
        this.directoryIgnoreList = directoryIgnoreList;
    }

    public void setFilesListing(File filesListing) {
        this.filesListing = filesListing;
    }

    public File getFilesListing() {
        return filesListing;
    }
}
