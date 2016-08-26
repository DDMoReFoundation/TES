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

package com.mango.mif.core.dao.impl;

import java.util.Calendar;
import java.util.List;

import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mango.mif.connector.CommandExecutionTarget;
import com.mango.mif.connector.Connector;
import com.mango.mif.connector.impl.CommandExecutionConnector;
import com.mango.mif.core.domain.Job;
import com.mango.mif.core.services.ConnectorsRegistry;
import com.mango.mif.domain.JobStatus;

/**
 * This class interacts with Spring's HibernateTemplate to save/delete and
 * retrieve Job objects.
 */
@Repository("jobRepository")
public class JobRepository extends GenericDaoImpl<Job, String> {	
	
    private static final int MILLIS_PER_DAY = 86400000;
    
    /**
     * The number of days over which to lookup acknowledged jobs.
     */
    private static final int ACKNOWLEDGED_JOBS_DAY_RANGE = 10;
    
    private ConnectorsRegistry connectorsRegistry;

	/**
     * Constructor that sets the entity to Job.class.
     */
    public JobRepository() {
        super(Job.class);
    }
   
    @Override
    public Job get(String id) {
    	return getJob(id);
    }
    
    /**
     * Execute the given {@link Query} to fetch a {@link List} of {@link Job}s.
     * Also, for each fetched {@link Job}, populate its {@link CommandExecutionTarget}
     * from the relevant entry from the Spring-configured (aggregated from the Connectors)
     * {@link com.mango.mif.core.services.ConnectorsRegistry ConnectorsRegistry}.
     * This is required because Job.commandExecutionTarget is a Hibernate-transient property. 
     * <p>
     * @param query - {@link Query} to execute
     * @return {@link List} of {@link Job}s that match the query criteria
     */
    private List<Job> executeJobsFetchQuery(final Query query) {
    	@SuppressWarnings("unchecked")
        List<Job> jobs = query.list();
        for (final Job job : jobs) {
        	populateCommandExecutionTargetFor(job);
        }
    	return jobs;
    }
    
    private void populateCommandExecutionTargetFor(final Job job) {
		final Connector connector = this.getConnectorsRegistry().getConnectorById(job.getConnectorId());
		if (!(connector instanceof CommandExecutionConnector)) {
			throw new IllegalStateException("Connector providing connector ID \"" + job.getConnectorId()+ "\" does not provide a command execution target, or no Connector providing this execution type exists.");
		}
		job.setCommandExecutionTarget( ((CommandExecutionConnector) connector).getCommandExecutionTarget() );
    }
    
    /**
     * Retrieve persisted job records whose state is not COMPLETED on a restart to provide
     * failover functionality.
     * @return List of persisted jobs that are not completed.
     */
	public List<Job> getUncompletedJobs(){
    	
    	List<Job> jobs = null;

		String queryString = "from Job where clientRequestStatus != ? and clientRequestStatus != ? and clientRequestStatus != ? ";

		Query query = getCurrentSession().createQuery(queryString);
		
		query.setParameter(0, JobStatus.COMPLETED.toString());
		query.setParameter(1, JobStatus.CANCELLED.toString());
        query.setParameter(2, JobStatus.FAILED.toString());
		
		jobs = executeJobsFetchQuery(query);
		    	
    	return jobs;
    }
    
    
    /**
     * Retrieve persisted job records whose state is not COMPLETED on a restart to provide failure recovery.
     * @return List of persisted jobs that are not completed.
     */
	public List<Job> getUncompletedConnectorJobs(String connectorId){

    	List<Job> jobs = null;

		String queryString = "from Job where clientRequestStatus != ? and clientRequestStatus != ? and clientRequestStatus != ? and connectorId = ?";

		Query query = getCurrentSession().createQuery(queryString);
		
		query.setParameter(0, JobStatus.COMPLETED.toString());
		query.setParameter(1, JobStatus.CANCELLED.toString());
        query.setParameter(2, JobStatus.FAILED.toString());
		query.setParameter(3, connectorId);
		
		jobs = executeJobsFetchQuery(query);
		
    	return jobs;
    }

    /**
     * Reads a job record from the database, given it's job id.  This is required during
     * job state persistence. 
     * @param jobId the job uid of the job that was passed to the connector dispatcher. 
     * @return Job corresponding to the specified job id.
     */
	public Job getJob(String jobId){
    	
    	Job job = null;
		String queryString = "from Job where jobId = ?";
		Query query = getCurrentSession().createQuery(queryString);
		query.setParameter(0, jobId);
		List<Job> jobs = executeJobsFetchQuery(query);
    	
    	if (jobs.size() > 0) {
    		job = (Job)jobs.get(0);    		
    		return job;
    	}
    	
    	return null;
    }
    
    /**
     * Reads a job record from the database and locks the row. 
     * 
     * @param jobId the job uid of the job that was passed to the connector dispatcher. 
     * @return Job corresponding to the specified job id.
     */
    public Job getJobForUpdate(String jobId){
        Job job = (Job) getCurrentSession().get(Job.class, jobId, LockOptions.UPGRADE);
        populateCommandExecutionTargetFor(job);
        return job;
    }    
    
    /**
     * Acknowledge a job by setting the acknowledged property on the Job.
     * @param jobId
     */
    public void acknowledgeJob(String jobId) {
    	
    	Job job = getJob(jobId);
    	
    	if (null != job) {
    		job.setAcknowledged(Calendar.getInstance().getTimeInMillis());
    		save(job);
    	}
    }
    
    /**
     * Gets all jobs whose acknowledged field is not null and has an acknowledged
     * date before the date provided but after upToTime minus a configurable number of days.
     * @return List of acknowledged jobs identified within the time frame
     */
    public String[] getAcknowledgedJobsToDate(Long uptoTime){

    	Long fromDate = uptoTime - (ACKNOWLEDGED_JOBS_DAY_RANGE * MILLIS_PER_DAY);
	    String queryString = "select JOB_ID from JOBS where TIME_ACKNOWLEDGED is not null and TIME_ACKNOWLEDGED < ? and TIME_ACKNOWLEDGED > ?";
	    Query query = getCurrentSession().createSQLQuery(queryString);
	    query.setParameter(0, uptoTime);
		query.setParameter(1, fromDate );

		// Explicitly don't use executeJobsFetchQuery() method here since we are not fetching Job objects		
		@SuppressWarnings("unchecked")
        List<Object> results = query.list();
		String[] jobIds = new String[results.size()];
		jobIds = (String[]) query.list().toArray(jobIds);

	    return jobIds;
    }
    
    /**
     *  Takes a date and return all jobs whos acknowledged field is not null.
     * @param dateTimeInMillis the date on which we look for acknowledged jobs.
     * @return List of acknowledged jobs for date specified.
     */
    public List<Job> getAcknowledgedJobsForDate(Long dateTimeInMillis){

    	long timeInMillisAtMidnight = getDayStart(dateTimeInMillis);
    	
    	List<Job> jobs = null;
		String queryString = "from Job job where job.acknowledged is not null and job.acknowledged > ? and job.acknowledged < ?";
		Query query = getCurrentSession().createQuery(queryString);
		query.setParameter(0, timeInMillisAtMidnight);
		query.setParameter(1, timeInMillisAtMidnight + MILLIS_PER_DAY);
		jobs = executeJobsFetchQuery(query);
		return jobs;
    }

    /**
     * Gets the time at midnight of the day of the linux datetime in millis passed in.
     * @param dateTimeInMillis the datetime in millis for which we want the datetime 
     * in millis at midnight the same day.
     * @return long linus datetime in millis at midnight.
     */
	private long getDayStart(Long dateTimeInMillis) {
		//Get the time at midnight of this linux datetime
    	Calendar dateTime = Calendar.getInstance();
    	dateTime.setTimeInMillis(dateTimeInMillis);
    	dateTime.set(Calendar.HOUR_OF_DAY, 0);
    	dateTime.set(Calendar.MINUTE, 0);
    	dateTime.set(Calendar.SECOND, 0);
    	dateTime.set(Calendar.MILLISECOND, 0);
    	long timeInMillisAtMidnight = dateTime.getTimeInMillis();
		return timeInMillisAtMidnight;
	}
    
    @Override
    public Job save(Job job) {
    	return super.save(job);
    }
    
    private Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

	public ConnectorsRegistry getConnectorsRegistry() {
	    return connectorsRegistry;
    }

	public void setConnectorsRegistry(ConnectorsRegistry connectorsRegistry) {
	    this.connectorsRegistry = connectorsRegistry;
    }
    
}
