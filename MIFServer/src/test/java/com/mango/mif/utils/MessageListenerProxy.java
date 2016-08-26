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
package com.mango.mif.utils;

import javax.jms.Message;
import javax.jms.MessageListener;

import com.google.common.base.Preconditions;


/**
 * delegates message handling to the wrapped instance. Useful for registering test class as JMS message consumer.
 */
public class MessageListenerProxy implements MessageListener {

    private MessageListener delegate;
    
    @Override
    public void onMessage(Message msg) {
        Preconditions.checkNotNull(delegate, "Message listener delegate can't be null");
        delegate.onMessage(msg);
    }

    public void setDelegate(MessageListener delegate) {
        this.delegate = delegate;
    }
    
    public MessageListener getDelegate() {
        return delegate;
    }
}
