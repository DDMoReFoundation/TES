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

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.jms.IllegalStateException;

import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.Status;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
import org.springframework.core.task.AsyncTaskExecutor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.mango.mif.connector.runner.DriverProcessingStatus;
import com.mango.mif.connector.runner.impl.statehandler.StateHandler;

/**
 * Default implementation of the SCXMLDriver interface.
 * 
 * Uses SCXMLListener to schedule StateHandlers for execution using the State Handlers Task Executor
 * 
 * IMPORTANT!!! 
 * Instances of that class can be concurrently used by even three threads! By really cautious when modifying that class and make sure that
 * no assumptions are made on order of executed methods.
 * 
 */
public class SCXMLDriverImpl extends AbstractStateMachine implements SCXMLDriver {
	
    private AsyncTaskExecutor stateHandlersExecutor;
    protected ConcurrentMap<String, StateHandler> stateHandlers = Maps.newConcurrentMap();
    private Exception exception;
    private volatile boolean finalState = false;
    private volatile StateHandler currentStateHandler;
    private SCXMLProcessingCancellationHandler cancellationHandler;
    
    @Override
    public void setSCXMLDocument(URL scxmlDocument) throws IllegalStateException {
        if(engine!=null) {
            throw new IllegalStateException("State machine already initialized.");
        }

        initialize(scxmlDocument);
        getEngine().addListener(getEngine().getStateMachine(), new StateHandlerDispatcher());
    }
    
    /**
     * 
     * @param state
     * @return true if the state machine is in the given state 
     */
    public boolean inState(String state) {
        Status status = getEngine().getCurrentStatus();
        @SuppressWarnings("unchecked")
        Set<TransitionTarget> set = status.getStates();
        return inState(state,set );
    }
    
    /**
     * Test whether expected state is found in given state or its parents
     */
    private boolean inState(String expected, TransitionTarget state) {
        if(state==null) return false;
        if(state.getId().equals(expected)) return true;
        return inState(expected,state.getParent());
            
    }
    /**
     * Checks whether the given expected state is found in given set of states or their parents
     * @param expected
     * @param states
     * @return
     */
    private boolean inState(String expected, Set<TransitionTarget> states) {
        Iterator<TransitionTarget> iterator = states.iterator();
        while(iterator.hasNext()) {
            TransitionTarget st = iterator.next();
            if(inState(expected,st)) return true;
        }
        return false;
    }
    /**
     * Return all current states
     * @param states
     * @return
     */
    private Set<String> getStates(Set<TransitionTarget> states) {
        HashSet<String> result = new HashSet<String>();
        Iterator<TransitionTarget> iterator = states.iterator();
        while(iterator.hasNext()) {
            TransitionTarget st = iterator.next();
            result.addAll(getSubStates(st));
        }
        return result;
    }

    /**
     * Return all current states
     * @param states
     * @return
     */
    private Set<String> getSubStates(TransitionTarget state) {
        HashSet<String> result = new HashSet<String>();
        if(state==null) return result;
        result.add(state.getId());
        result.addAll(getSubStates((State)state.getParent()));
        return result;
        
    }
    @Override
    public synchronized void stateHandlerProcessingCompleted(final StateHandler sHandler) throws IllegalStateException {
        try {
            Status status = getEngine().getCurrentStatus();
            @SuppressWarnings("unchecked")
            Set<TransitionTarget> set = status.getStates();
            
            if(inState(sHandler.getState(),set )) {
                complete(sHandler);
            } else {
                String errMsg = String.format("The state machine is in different state than the one expected by state handler. Expected state = '%s'. State machine was in states: %s ",sHandler.getState(),getStates(set));
                LOG.error(errMsg);
                throw new IllegalStateException(errMsg);
            }
        } finally {

            currentStateHandler = null;
        }
    }

    private void complete(final StateHandler stateHandler) {
        String exitEvent = stateHandler.complete();
        if(exitEvent!=null) {
            fireEventInternal(exitEvent);
        } else {
            if(engine.getCurrentStatus().isFinal()) {
                LOG.debug("Reached final state");
                this.finalState = true;
            } else {
                LOG.debug("Exit Event is null but did not reach final state. Apparently some external component is expected to trigger transition to next state.");
            }
        }
    }

    @Override
    public synchronized void fireEvent(String event) {
        fireEventInternal(event);
    }

    @Override
    public void registerHandler(StateHandler stateHandler) {
        stateHandlers.put(stateHandler.getState(), stateHandler);
        stateHandler.setSCXMLDriver(this);
    }

    @Override
    public void setCancellationHandler(SCXMLProcessingCancellationHandler stateHandler) {
        cancellationHandler = stateHandler;
    }

    @Override
    public StateHandler unregisterHandler(StateHandler stateHandler) {
        return stateHandlers.remove(stateHandler.getState());
    }

    @Override
    public StateHandler unregisterHandler(String state) {
        return stateHandlers.remove(state);
    }
    /**
     * Handler responsible for handling transition events and scheduling appropriate
     * Status Handlers
     * @author mrogalski
     *
     */
    class StateHandlerDispatcher implements SCXMLListener {

        @Override
        public void onEntry(TransitionTarget state) {
            LOG.debug(getSubStates(state));
            dispatch(state.getId());
        }

        @Override
        public void onExit(TransitionTarget state) {
        }

        @Override
        public void onTransition(TransitionTarget from, TransitionTarget to, Transition transition) {
        }
        
    }
    
    /**
     * Dispatch the execution to the appropriate handler
     * @param state
     */
    private synchronized void dispatch(String state) {
        LOG.debug(String.format("Handle state %s",state));
        
        StateHandler handler = stateHandlers.get(state);
        Preconditions.checkNotNull(handler,String.format("Handler for state %s not found!",state));
        
        LOG.debug(String.format("Dispatching execution to the state handler %s.",handler.getState()));
        stateHandlersExecutor.submit(handler);
    }

    /**
     * Returns copy of the internal state handlers registry
     * @return
     */
    public Map<String, StateHandler> getStateHandlers() {
        HashMap<String, StateHandler> result = Maps.newHashMap();
        result.putAll(stateHandlers);
        return result;
    }

    @Override
    public DriverProcessingStatus getProcessingStatus() {
        if(!engine.getCurrentStatus().isFinal()) return DriverProcessingStatus.RUNNING;
        @SuppressWarnings("unchecked")
        Set<State> set = getEngine().getCurrentStatus().getStates();
        State state = set.iterator().next();
        if(state.getId().equals(JobRunnerState.FAILED.getStateName())) return DriverProcessingStatus.FAILED;
        if(state.getId().equals(JobRunnerState.CANCELLED.getStateName())) return DriverProcessingStatus.CANCELLED;
        return DriverProcessingStatus.FINISHED;
    }

    @Override
    public void stateHandlerProcessingFailed(StateHandler stateHandler) {
        try {
            exception = stateHandler.getStateHandlerException();
            String exitEvent = stateHandler.failed();
            if(exitEvent!=null) {
                fireEventInternal(exitEvent);
            } else {
                if(engine.getCurrentStatus().isFinal()) {
                    LOG.debug("Reached final state");
                    this.finalState = true;
                } else {
                    LOG.debug("Exit Event is null but did not reach final state. Apparently some external component is expected to trigger transition to next state.");
                }
            }
        } finally {
            currentStateHandler = null;
        }
    }
    
    public void setStateHandlersExecutor(AsyncTaskExecutor stateHandlersExecutor) {
        this.stateHandlersExecutor = stateHandlersExecutor;
    }
    
    @Override
    public Exception getException() {
        return exception;
    }
    
    @Override
    public boolean isInFinalState() {
        return finalState;
    }

    @Override
    public void stateHandlerProcessingStarted(StateHandler currentStateHandler) {
        this.currentStateHandler = currentStateHandler;
    }

    @Override
    public synchronized void cancel() {
        try {
            if(currentStateHandler!=null) {
                currentStateHandler.cancelProcessing();
            }

            if(cancellationHandler!=null) {
                cancellationHandler.execute();
            } else {
                LOG.warn("Cancellation handler was not set");
            }
            
            fireEvent(JobRunnerState.CANCELLED.getTriggeringEvent());
        } catch(Exception e) {
            LOG.error("Error when running cancellation handler",e);
            exception = e;
            fireEvent(JobRunnerState.FAILED.getTriggeringEvent());
        }
    }

    @Override
    public void start() {
        fireEvent(JobRunnerState.RUNNING.getTriggeringEvent());
    }

    @Override
    public SCXMLProcessingCancellationHandler getCancellationHandler() {
        return cancellationHandler;
    }

}
