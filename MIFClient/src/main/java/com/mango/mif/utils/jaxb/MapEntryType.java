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
package com.mango.mif.utils.jaxb;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * The Class MapEntryType.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class MapEntryType<K, V> {

    /** The key. */
    private K key;
    
    /** The value. */
    private V value;

    public MapEntryType() {
    }

    public MapEntryType(Map.Entry<K, V> e) {
        key = e.getKey();
        value = e.getValue();
    }

    @XmlElement
    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @XmlElement
    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
