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

public class Pair<FIRST, SECOND> {

    private FIRST first;
    private SECOND second;

    private Pair(FIRST first, SECOND second) {
        this.first = first;
        this.second = second;
    }

    public static <FIRST, SECOND> Pair<FIRST, SECOND> of(FIRST first, SECOND second) {
        return new Pair<FIRST, SECOND>(first, second);
    }

    public static <FIRST, SECOND> Pair<FIRST, SECOND> empty() {
        return new Pair<FIRST, SECOND>(null, null);
    }

    /**
     * Caveat programmer: you must call this function only after assignments to first
     * and second have been made.  If you set first and second, call this function, then
     * change first and second, the hashcode will be wrong.
     */
    @Override
    public int hashCode() {
        return 31 * hashcode(first) + hashcode(second);
    }

    private static int hashcode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        return equal(first, ((Pair<?, ?>) obj).first) && equal(second, ((Pair<?, ?>) obj).second);
    }

    private boolean equal(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            // if both are null, they are equal, otherwise not.
            return (o1 == null && o2 == null);
        }
        return o1.equals(o2);
    }

    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    public void setFirst(FIRST first) {
        this.first = first;
    }

    public void setSecond(SECOND second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }
}
