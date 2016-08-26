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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class MapType.
 */
public class MapType<K, V> {

    /** The entry. */
    private List<MapEntryType<K, V>> entry = new ArrayList<MapEntryType<K, V>>();

    /**
     * Instantiates a new map type.
     */
    public MapType() {
    }

    /**
     * Instantiates a new map type.
     *
     * @param map the map
     */
    public MapType(Map<K, V> map) {
        for (Map.Entry<K, V> e : map.entrySet()) {
            entry.add(new MapEntryType<K, V>(e));
        }
    }

    /**
     * Gets the entry.
     *
     * @return the entry
     */
    public List<MapEntryType<K, V>> getEntry() {
        return entry;
    }

    /**
     * Sets the entry.
     *
     * @param entry the entry
     */
    public void setEntry(List<MapEntryType<K, V>> entry) {
        this.entry = entry;
    }
}
