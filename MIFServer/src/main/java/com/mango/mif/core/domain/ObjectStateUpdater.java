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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Applies list of commands on a job in a given order. Before performing updates checks list of preconditions.
 * 
 */
public class ObjectStateUpdater<T> {

    private List<Function<T, T>> commands = Lists.newArrayList();

    private List<Precondition<T>> preconditions = Lists.newArrayList();

    private void checkPreconditions(T job) {
        Preconditions.checkNotNull(job, "Object was null");
        for (Precondition<T> precondition : preconditions) {
            precondition.check(job);
        }
    }

    /**
     * Performs updates on the passed object. 
     * @param object that should be updated
     */
    public void update(T object) {
        checkPreconditions(object);
        for (Function<T, T> command : commands) {
            command.apply(object);
        }
    }

    /**
     * Adds a command to a list of command that should be applied during object update process. 
     * @param a command
     * @return instance of that updater
     */
    public ObjectStateUpdater<T> addCommand(Function<T, T> command) {
        this.commands.add(command);
        return this;
    }

    /**
     * Adds a precondition that should be fulfilled before updates are applied on the object.
     * @param precondition 
     * @return instance of that updater
     */
    public ObjectStateUpdater<T> addCondition(Precondition<T> precondition) {
        this.preconditions.add(precondition);
        return this;
    }
}
