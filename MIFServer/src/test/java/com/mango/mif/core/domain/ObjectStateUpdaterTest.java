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
package com.mango.mif.core.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import com.google.common.base.Function;

/**
 * Test {$link ObjectStateUpdater}
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjectStateUpdaterTest {

    @Test(expected=IllegalStateException.class)
    public void update_should_throw_exception_if_at_least_one_precondition_is_not_met() {
        ObjectStateUpdater<Job> updater = new ObjectStateUpdater<Job>();
        
        updater.addCondition(new Precondition<Job>() {
            
            @Override
            public void check(Job job) {
            }
        });
        updater.addCondition(new Precondition<Job>() {
            
            @Override
            public void check(Job job) {
            }
        });
        updater.addCondition(new Precondition<Job>() {
            
            @Override
            public void check(Job job) {
                throw new IllegalStateException();
            }
        });
        updater.update(mock(Job.class));
        
        
    }

    @Test
    public void update_should_apply_commands() {
        ObjectStateUpdater<Job> updater = new ObjectStateUpdater<Job>();
        final String expected = "job-id";
        Job job = mock(Job.class);
        
        updater.addCommand(new Function<Job,Job>() {

            @Override
            public Job apply(Job input) {
                input.setJobId(expected);
                return input;
            }
            
        });
        
        
        updater.update(job);
        
        verify(job).setJobId(expected);
    }

}
