package com.nfolkert.collections;

import java.util.Map;

/**
 * Taken from the Jakarta commons-collection and augmented to have generics
 *
 *
 * Defines a map that can be iterated directly without needing to create an entry set.
 * <p>
 * A map iterator is an efficient way of iterating over maps.
 * There is no need to access the entry set or cast to Map Entry objects.
 * <pre>
 * IterableMap map = new HashedMap();
 * MapIterator it = map.mapIterator();
 * while (it.hasNext()) {
 *   Object key = it.next();
 *   Object value = it.getValue();
 *   it.setValue("newValue");
 * }
 * </pre>
 *
 * @since Commons Collections 3.0
 * @version $Revision: 1.4 $ $Date: 2004/02/18 01:15:43 $
 *
 * @author Stephen Colebourne
 */
public interface IterableMap<K,V> extends Map<K,V> {

    /**
     * Obtains a <code>MapIterator</code> over the map.
     * <p>
     * A map iterator is an efficient way of iterating over maps.
     * There is no need to access the entry set or cast to Map Entry objects.
     * <pre>
     * IterableMap map = new HashedMap();
     * MapIterator it = map.mapIterator();
     * while (it.hasNext()) {
     *   Object key = it.next();
     *   Object value = it.getValue();
     *   it.setValue("newValue");
     * }
     * </pre>
     *
     * @return a map iterator
     */
    MapIterator<K,V> mapIterator();

}
