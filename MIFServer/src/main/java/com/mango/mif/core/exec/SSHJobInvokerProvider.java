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
package com.mango.mif.core.exec;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.exec.ExecutionException;
import com.mango.mif.core.exec.Invoker;
import com.mango.mif.core.exec.invoker.InvokerFactory;
import com.mango.mif.core.exec.invoker.InvokerParameters;
import com.mango.mif.utils.encrypt.Encrypter;
import com.mango.mif.utils.encrypt.EncryptionException;

/**
 * @version $Revision: $ as of $Date: $
 * <p>SVN Entry : $HeadURL: $
 * <p>SVN ID    : $Id: $
 * <p>Last edited by : $Author: $
 *
 * Provides invokers to execute commands over SSH
 */
public class SSHJobInvokerProvider implements JobInvokerProvider {

    private InvokerFactory invokerFactory;
    /**
     * Invoker parameters that are injected should not be modified, instead these should be used as a template to create job specific parameters to pass to the factory
     */
    private InvokerParameters invokerParameters;
    private Encrypter encrypter;

    /* (non-Javadoc)
     * @see com.mango.mif.core.JobInvokerProvider#createInvoker(com.mango.mif.connector.domain.Job)
     */
    @Override
    public Invoker createInvoker(final Job job) throws ExecutionException {
        Preconditions.checkNotNull(invokerFactory);

        InvokerParameters jobParams = null;
        try {
            jobParams = setupParameters(job);
        } catch (EncryptionException e) {
            throw new ExecutionException("Could not create the parameters for the SSH invoker", e);
        }
        final Invoker result = invokerFactory.getSSHInvoker(jobParams);
        job.setInvoker(result);
        return result;
    }

    /**
     * Creates a parameter instance with the relevant info from the job
     * @param job the job to use when creating the params
     * @return the parameters
     * @throws EncryptionException if the encrypted password in the job cannot be decrypted
     */
    private InvokerParameters setupParameters(final Job job) throws EncryptionException {
        Preconditions.checkNotNull(invokerParameters);
        Preconditions.checkNotNull(encrypter);

        // clone here to preserve the parameters per-job
        final InvokerParameters jobParams = invokerParameters.cloneParameters();

        jobParams.setUserName(job.getUserName());
        try {
            jobParams.setClearTextPassword(encrypter.decrypt(job.getPassword()));
        } catch (EncryptionException ee) {
            // The only reason for this ridiculous construct is that we get to specify a bit more of a reason within
            // the exception - at least this way we can see what we were trying to decrypt.
            //
            throw new EncryptionException(
                String.format("Failed to decrypt job password \"%s\" (length %d)", job.getPassword(), job.getPassword().length()),
                ee);
        }

        setJobData(job, jobParams);

        return jobParams;
    }

    /**
     * Sets the SSH params from the job in the invoker params. If the job doesn't have SSH params then the defaults are used from the invoker params.
     *
     * @param job the job from which the SSH params should be retrieved. Note that if the job does not contain SSH params then they will be set using the values from the invoker params (the defaults)
     * @param jobParams the params to be configured from the jobs SSH params
     */
    private void setJobData(final Job job, final InvokerParameters jobParams) {
        Map<String, String> data = job.getData();
        Preconditions.checkNotNull(data);

        if (data.containsKey(SSHConstants.HOST_NAME)) {
            final String host = data.get(SSHConstants.HOST_NAME);
            if (!StringUtils.isBlank(host)) {
                jobParams.setHost(host);
            } else {
                data.put(SSHConstants.HOST_NAME, jobParams.getHost());
            }
        }
        if (data.containsKey(SSHConstants.PORT)) {
            final int port = Integer.valueOf(data.get(SSHConstants.PORT));
            if (port > 0) {
                jobParams.setPort(port);
            } else {
                data.put(SSHConstants.PORT, String.valueOf(jobParams.getPort()));
            }
        }
        if (data.containsKey(SSHConstants.CHARSET)) {
            final String charset = data.get(SSHConstants.CHARSET);
            if (!StringUtils.isBlank(charset)) {
                jobParams.setCharSet(charset);
            } else {
                data.put(SSHConstants.CHARSET, jobParams.getCharSet());
            }
        }
        if (data.containsKey(SSHConstants.PROTOCOLS)) {
            final String protocols = data.get(SSHConstants.PROTOCOLS);
            if (!StringUtils.isBlank(protocols)) {
                jobParams.setProtocols(protocols);
            } else {
                data.put(SSHConstants.PROTOCOLS, jobParams.getProtocols());
            }
        }
    }

    public InvokerFactory getInvokerFactory() {
        return this.invokerFactory;
    }

    public void setInvokerFactory(InvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }

    public InvokerParameters getInvokerParameters() {
        return this.invokerParameters;
    }

    public void setInvokerParameters(InvokerParameters invokerParameters) {
        this.invokerParameters = invokerParameters;
    }

    public Encrypter getEncrypter() {
        return this.encrypter;
    }

    public void setEncrypter(Encrypter encrypter) {
        this.encrypter = encrypter;
    }
}
