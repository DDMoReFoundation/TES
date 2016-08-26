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
package com.mango.mif.connector.runner.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.Status;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.impl.statehandler.MockStateHandler;
import com.mango.mif.connector.runner.impl.statehandler.StateHandler;
import com.mango.mif.core.exec.ExecutionException;

/**
 * SCXML driver implementation unit test
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class SCXMLDriverImplTest {
    /**
     * Logger
     */
    private static Logger LOG = Logger.getLogger(SCXMLDriverImplTest.class);
    
    /**
     * Job runner factory producing spring-managed job runners
     */
    @Resource(name="scxmlDriver")
	private SCXMLDriverImpl scxmlDriver;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * Used to synchronise test thread and spring TaskExecutors
     */
    private CountDownLatch eventSignal;
    /**
     * states and exit events from the states
     */
    private static Map<String,String> STATES = Maps.newHashMap();
    
    private final static int SLEEP_TIMEOUT = 100;
    
    static {
    	STATES.put(JobRunnerState.PENDING.getStateName(), SCXMLDriver.NULL_EVENT);
    	STATES.put(JobRunnerState.RUNNING.getStateName(), SCXMLDriver.NULL_EVENT);
    	STATES.put(JobRunnerState.TASK_PENDING.getStateName(), JobRunnerState.TASK_PREPARING.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_PREPARING.getStateName(), JobRunnerState.TASK_SUBMITTING.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_SUBMITTING.getStateName(), JobRunnerState.TASK_PROCESSING.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_PROCESSING.getStateName(), JobRunnerState.TASK_RETRIEVING.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_RETRIEVING.getStateName(), JobRunnerState.TASK_POSTPROCESSING.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_POSTPROCESSING.getStateName(), JobRunnerState.TASK_FINISHED.getTriggeringEvent());
    	STATES.put(JobRunnerState.TASK_FINISHED.getStateName(), JobRunnerState.FINISHED.getTriggeringEvent());
    	STATES.put(JobRunnerState.FINISHED.getStateName(), SCXMLDriver.NULL_EVENT);
    	STATES.put(JobRunnerState.CANCELLED.getStateName(), SCXMLDriver.NULL_EVENT);
        STATES.put(JobRunnerState.FAILED.getStateName(), SCXMLDriver.NULL_EVENT);
    }
    
    /**
     * Tests scxml driver handlers execution
     * @throws Exception 
     */
    @DirtiesContext
    @Test
    public void shouldPassThroughStatesInTheStateMachine() throws Exception {
    	LOG.info("shouldPassThroughStatesInTheStateMachine");
    	CountDownLatch eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        
        registerNofifierOnState(JobRunnerState.FINISHED.getStateName(),eventSignal);

        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }

        scxmlDriver.setCancellationHandler(mock(SCXMLProcessingCancellationHandler.class));
        
        ((SCXMLDriver)scxmlDriver).fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());

        //thread that runs the test is notified when finished state is reached
        eventSignal.await();
        MockStateHandler handler = (MockStateHandler) stateHandlers.get(JobRunnerState.FINISHED.getStateName());

        //Must ensure that the finished state handler has been invoked
        while(!handler.isDone()) {
        	Thread.sleep(SLEEP_TIMEOUT);
        }
        
        stateHandlers.remove(JobRunnerState.PENDING.getStateName());
        stateHandlers.remove(JobRunnerState.CANCELLED.getStateName());
        stateHandlers.remove(JobRunnerState.FAILED.getStateName());
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            verify(en.getValue()).call();
        }
    }
    
    @DirtiesContext
    @Test
    public void shouldMoveFromPendingStateToCancelledState() throws Exception {
    	LOG.info("shouldMoveFromPendingStateToCancelledState");
    	eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        registerNofifierOnState(JobRunnerState.CANCELLED.getStateName(),eventSignal);


        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }
        scxmlDriver.setCancellationHandler(mock(SCXMLProcessingCancellationHandler.class));

        scxmlDriver.cancel();
        
        //thread that runs the test is notified when finished state is reached
        eventSignal.await();

        
        assertTrue(scxmlDriver.inState(JobRunnerState.CANCELLED.getStateName()));
    }
    

    @DirtiesContext
    @Test
    public void cancel_shouldNotThrowExceptionIfCurrentStateHandlerIsNull() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());

        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }
        scxmlDriver.cancel();
    }


    @DirtiesContext
    @Test
    public void cancel_shouldNotThrowExceptionIfCancellationHandlerIsNull() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());

        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }
        scxmlDriver.stateHandlerProcessingStarted(mock(StateHandler.class));
        scxmlDriver.cancel();
    }

    @DirtiesContext
    @Test
    public void cancel_shouldMoveToFailedStateIfCancellationHandlerFails() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        registerNofifierOnState(JobRunnerState.CANCELLED.getStateName(),eventSignal);
        registerNofifierOnState(JobRunnerState.FAILED.getStateName(),eventSignal);


        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }
        SCXMLProcessingCancellationHandler cancellationHandler = mock(SCXMLProcessingCancellationHandler.class);
        doThrow(mock(ExecutionException.class)).when(cancellationHandler).execute();
        
        scxmlDriver.setCancellationHandler(cancellationHandler);

        scxmlDriver.cancel();
        
        eventSignal.await();

        assertTrue("State machine was not moved to failed state", scxmlDriver.inState(JobRunnerState.FAILED.getStateName()));
        
        MockStateHandler handler = (MockStateHandler) stateHandlers.get(JobRunnerState.FAILED.getStateName());

        //Must ensure that the finished state handler has been invoked
        while(!handler.isDone()) {
            Thread.sleep(SLEEP_TIMEOUT);
        }
        
        assertTrue("State machine was not moved to final state", scxmlDriver.isInFinalState());
        assertNotNull("Error was not set on state machine", scxmlDriver.getException());
    }

    
    @DirtiesContext
    @Test
    public void getProcessingStatus_shouldBeInRunningStateForAllNonFinalSatates() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        SCXMLExecutor executor = mock(SCXMLExecutor.class);
        scxmlDriver.setEngine(executor);
        Status status = mock(Status.class);
        when(status.isFinal()).thenReturn(false);
        when(executor.getCurrentStatus()).thenReturn(status);
        assertEquals(DriverProcessingStatus.RUNNING,scxmlDriver.getProcessingStatus());
    }

    @DirtiesContext
    @Test
    public void getProcessingStatus_shouldBeInCancelledStateForSCXMLCancelledState() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        SCXMLExecutor executor = mock(SCXMLExecutor.class);
        scxmlDriver.setEngine(executor);
        Status status = mock(Status.class);
        when(status.isFinal()).thenReturn(true);
        Set<State> states = Sets.newHashSet();
        when(status.getStates()).thenReturn(states);
        State state = new State();
        state.setId(JobRunnerState.CANCELLED.getStateName());
        states.add(state);
        
        when(executor.getCurrentStatus()).thenReturn(status);
        assertEquals(DriverProcessingStatus.CANCELLED,scxmlDriver.getProcessingStatus());
    }

    @DirtiesContext
    @Test
    public void getProcessingStatus_shouldBeInFailedStateForSCXMLFailedState() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        SCXMLExecutor executor = mock(SCXMLExecutor.class);
        scxmlDriver.setEngine(executor);
        Status status = mock(Status.class);
        when(status.isFinal()).thenReturn(true);
        Set<State> states = Sets.newHashSet();
        when(status.getStates()).thenReturn(states);
        State state = new State();
        state.setId(JobRunnerState.FAILED.getStateName());
        states.add(state);
        
        when(executor.getCurrentStatus()).thenReturn(status);
        assertEquals(DriverProcessingStatus.FAILED,scxmlDriver.getProcessingStatus());
    }

    @DirtiesContext
    @Test
    public void getProcessingStatus_shouldBeInFinishedStateForSCXMLFinishedState() throws Exception {
        eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        SCXMLExecutor executor = mock(SCXMLExecutor.class);
        scxmlDriver.setEngine(executor);
        Status status = mock(Status.class);
        when(status.isFinal()).thenReturn(true);
        Set<State> states = Sets.newHashSet();
        when(status.getStates()).thenReturn(states);
        State state = new State();
        state.setId(JobRunnerState.FINISHED.getStateName());
        states.add(state);
        
        when(executor.getCurrentStatus()).thenReturn(status);
        assertEquals(DriverProcessingStatus.FINISHED,scxmlDriver.getProcessingStatus());
    }

    @DirtiesContext
    @Test
    public void shouldBeStillInCanceledStateEvenIfStartEventIsSent() throws Exception {
    	LOG.info("shouldBeStillInCanceledStateEvenIfStartEventIsSent");
    	eventSignal = new CountDownLatch(1);
        org.springframework.core.io.Resource resource =  applicationContext.getResource("classpath:META-INF/JobRunnerWithFailover.scxml");
        scxmlDriver.setSCXMLDocument(resource.getURL());
        Preconditions.checkNotNull(resource,"SCXML document does not exist.");
        
        registerNofifierOnState(JobRunnerState.CANCELLED.getStateName(),eventSignal);
        Map<String,StateHandler> stateHandlers = createStateHanders();
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }

        scxmlDriver.setCancellationHandler(mock(SCXMLProcessingCancellationHandler.class));
        scxmlDriver.cancel();

        //thread that runs the test is notified when finished state is reached
        eventSignal.await();

        ((SCXMLDriver)scxmlDriver).fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());
        assertTrue(scxmlDriver.inState(JobRunnerState.CANCELLED.getStateName()));
    }

    /**
     * Tests scxml driver handlers execution
     * @throws Exception 
     */
    @DirtiesContext
    @Test
    public void shouldPassThroughStatesInTheStateMachineWithLoops() throws Exception {
    	LOG.info("shouldPassThroughStatesInTheStateMachineWithLoops");
    	URL url = SCXMLDriverImplTest.class.getResource("JobRunnerWithFailoverAndLoops.scxml");
        scxmlDriver.setSCXMLDocument(url);
        Preconditions.checkNotNull(url,"SCXML document does not exist.");
        
        Map<String,StateHandler> stateHandlers = createStateHanders();
        
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            scxmlDriver.registerHandler(en.getValue());
        }
        
        // states specific to the definition
        HashMap<String,String> customStates = Maps.newHashMap();
        customStates.put("task-monitoring","task.monitor");
        customStates.put("task-post-monitoring","task.post.monitor");
        customStates.put("task-monitoring-finished","done.state.task-processing");

        for(Entry<String,String> en : customStates.entrySet()) {
            scxmlDriver.registerHandler(createStateHandlerMock(en.getKey(), en.getValue()));
        }
        scxmlDriver.setCancellationHandler(mock(SCXMLProcessingCancellationHandler.class));
        
    	CountDownLatch monitoringEventSignal = new CountDownLatch(3);
        registerNofifierOnState("task-monitoring",monitoringEventSignal);
        
    	CountDownLatch postMonitoringEventSignal = new CountDownLatch(4);
        registerNofifierOnState("task-post-monitoring",postMonitoringEventSignal);
        
    	CountDownLatch completedEventSignal = new CountDownLatch(1);
        registerNofifierOnState(JobRunnerState.FINISHED.getStateName(),completedEventSignal);
        
        // start the state machine processing
        ((SCXMLDriver)scxmlDriver).fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());

        //wait for the task monitoring state handler execution
        monitoringEventSignal.await();
        ((MockStateHandler)scxmlDriver.stateHandlers.get("task-monitoring")).setExitEvent("task.post.monitor");
        
        //wait for the task monitoring state handler execution
        postMonitoringEventSignal.await();
        ((MockStateHandler)scxmlDriver.stateHandlers.get("task-post-monitoring")).setExitEvent("task.monitor.finished");
        
        //wait for the task monitoring state handler execution
        completedEventSignal.await();
        
        
        MockStateHandler handler = (MockStateHandler) stateHandlers.get(JobRunnerState.FINISHED.getStateName());

        //Must ensure that the finished state handler has been invoked
        while(!handler.isDone()) {
        	Thread.sleep(SLEEP_TIMEOUT);
        }
        
        stateHandlers.remove(JobRunnerState.PENDING.getStateName());
        stateHandlers.remove(JobRunnerState.CANCELLED.getStateName());
        stateHandlers.remove(JobRunnerState.FAILED.getStateName());
        for(Entry<String,StateHandler> en : stateHandlers.entrySet()) {
            verify(en.getValue()).call();
        }
    }
    /**
     * Registers a state machine listener that on given state notifies any threads that wait on semaphore
     * @param stateId
     */
    private void registerNofifierOnState(final String stateId, final CountDownLatch eventSignal) {
        scxmlDriver.getEngine().addListener(scxmlDriver.getEngine().getStateMachine(), new SCXMLListener() {

            @Override
            public void onEntry(TransitionTarget state) {
                LOG.debug("onEntry " + state.getId());
                if(state.getId().equals(stateId)) {
                    eventSignal.countDown();
                }
            }

            @Override
            public void onExit(TransitionTarget state) {
            }

            @Override
            public void onTransition(TransitionTarget from, TransitionTarget to, Transition transition) {
            }
            
        });
    }
    
    
    /**
     * Creates mock state handlers 
     * @return
     */
    private Map<String,StateHandler> createStateHanders() {
        Map<String,StateHandler> stateHandlers = new HashMap<String,StateHandler>();
        for(Entry<String,String> state : STATES.entrySet()) {
            stateHandlers.put(state.getKey(),createStateHandlerMock(state.getKey(),state.getValue()));
        }
        return stateHandlers;
    }

    /**
     * Creates a single mock state handler that will be invoked on the given state and once it finishes it will send a given event to the state machine.
     * @param state
     * @param exitEvent
     * @return
     */
    private StateHandler createStateHandlerMock(String state, String exitEvent) {
        MockStateHandler mockHandler = new MockStateHandler(state, exitEvent);
        mockHandler.setTime(SLEEP_TIMEOUT);
        StateHandler stateHandler = spy(mockHandler);
        return stateHandler;
    }
}
