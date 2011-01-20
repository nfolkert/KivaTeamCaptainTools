/*
 *  Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.nfolkert.collections;

import org.apache.commons.collections.ResettableIterator;
import org.apache.commons.collections.iterators.AbstractIteratorDecorator;
import org.apache.commons.collections.keyvalue.AbstractMapEntry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

import com.nfolkert.utils.Pair;

/**
 * Taken from the Jakarta commons-collection and augmented to have generics
 *
 * Decorates a <code>Map</code> to ensure that the order of addition is retained
 * using a <code>List</code> to maintain order.
 * <p>
 * The order will be used via the iterators and toArray methods on the views.
 * The order is also returned by the <code>MapIterator</code>.
 * The <code>orderedMapIterator()</code> method accesses an iterator that can
 * iterate both forwards and backwards through the map.
 * In addition, non-interface methods are provided to access the map by index.
 * <p>
 * If an object is added to the Map for a second time, it will remain in the
 * original position in the iteration.
 * <p>
 * <strong>Note that ListOrderedMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class may throw
 * exceptions when accessed by concurrent threads without synchronization.
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 365406 $ $Date: 2006-01-02 19:34:53 +0000 (Mon, 02 Jan 2006) $
 *
 * @author Henri Yandell
 * @author Stephen Colebourne
 * @author Matt Benson
 */
public class ListOrderedMap<K,V>
        implements OrderedMap<K,V>, Serializable
{
    protected transient Map<K,V> map;

    /** Serialization version */
    private static final long serialVersionUID = 2728177751851003750L;

    /**
     * Internal list to hold the sequence of objects
     */
    protected List<K> insertOrder;

    /**
     * Factory method to create an ordered map.
     * <p>
     * An <code>ArrayList</code> is used to retain order.
     *
     * @param map  the map to decorate, must not be null
     * @throws IllegalArgumentException if map is null
     */
    /*public static <K,V>  OrderedMap decorate(Map<K,V> map) {
        return new ListOrderedMap<K,V>(map);
    }*/

    //-----------------------------------------------------------------------
    /**
     * Constructs a new empty <code>ListOrderedMap</code> that decorates
     * a <code>HashMap</code>.
     *
     * @since Commons Collections 3.1
     */
    public ListOrderedMap() {
        this(new HashMap<K,V>());
    }

    public Pair<List<K>,List<V>> toParallelArrays()
    {
        List<K> keyList = new ArrayList<K>(keyList());
        List<V> valList = new ArrayList<V>(keyList.size());
        for (K key: keyList)
            valList.add(get(key));

        return new Pair<List<K>, List<V>>(keyList, valList);
    }

    public ListOrderedMap( final int initialCapacity) {
        this(new HashMap<K,V>(initialCapacity), initialCapacity);
    }

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws IllegalArgumentException if map is null
     */
    protected ListOrderedMap(Map<K,V> map)
    {
        this( map, -1);
    }

    private ListOrderedMap(Map<K,V> map, final int initialListCapacity)
    {
        this.map = map;
        insertOrder = initialListCapacity < 0 ? new ArrayList<K>() : new ArrayList<K>(initialListCapacity);
        insertOrder.addAll(getMap().keySet());
    }

    //-----------------------------------------------------------------------
    /**
     * Write the map out using a custom routine.
     *
     * @param out  the output stream
     * @throws IOException
     * @since Commons Collections 3.1
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(map);
        out.writeObject(insertOrder);
    }

    /**
     * Read the map in using a custom routine.
     *
     * @param in  the input stream
     * @throws IOException
     * @throws ClassNotFoundException
     * @since Commons Collections 3.1
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        map = (Map<K,V>) in.readObject();
        insertOrder = (List<K>) in.readObject();
    }

    // Implement OrderedMap
    //-----------------------------------------------------------------------
    public MapIterator<K,V> mapIterator() {
        return orderedMapIterator();
    }

    public OrderedMapIterator<K,V> orderedMapIterator() {
        return new ListOrderedMapIterator<K,V>(this);
    }

    /**
     * Gets the first key in this map by insert order.
     *
     * @return the first key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    public K firstKey() {
        if (size() == 0) {
            throw new NoSuchElementException("Map is empty");
        }
        return insertOrder.get(0);
    }

    /**
     * Gets the last key in this map by insert order.
     *
     * @return the last key currently in this map
     * @throws NoSuchElementException if this map is empty
     */
    public K lastKey() {
        if (size() == 0) {
            throw new NoSuchElementException("Map is empty");
        }
        return insertOrder.get(size() - 1);
    }

    /**
     * Gets the next key to the one specified using insert order.
     * This method performs a list search to find the key and is O(n).
     *
     * @param key  the key to find previous for
     * @return the next key, null if no match or at start
     */
    public K nextKey(K key)
    {
        int index = insertOrder.indexOf(key);
        if (index >= 0 && index < size() - 1) {
            return insertOrder.get(index + 1);
        }
        return null;
    }

    /**
     * Gets the previous key to the one specified using insert order.
     * This method performs a list search to find the key and is O(n).
     *
     * @param key  the key to find previous for
     * @return the previous key, null if no match or at start
     */
    public K previousKey(K key) {
        int index = insertOrder.indexOf(key);
        if (index > 0) {
            return insertOrder.get(index - 1);
        }
        return null;
    }

    //-----------------------------------------------------------------------
    public V put(K key, V value) {
        if (getMap().containsKey(key)) {
            // re-adding doesn't change order
            return getMap().put((K)key, (V)value);
        } else {
            // first add, so add to both map and list
            V result = getMap().put((K)key, (V)value);
            insertOrder.add((K)key);
            return result;
        }
    }


    public void putAll(Map<? extends K, ? extends V> map)
    {
        final Iterator<? extends Entry<? extends K, ? extends V>> it = map.entrySet().iterator();
        while (it.hasNext())
        {
            final Entry<? extends K, ? extends V> entry = it.next();
            put(entry.getKey(), entry.getValue());
        }
    }


    public V remove(Object key) {
        V result = getMap().remove(key);
        insertOrder.remove(key);
        return result;
    }

    public void clear() {
        getMap().clear();
        insertOrder.clear();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets a view over the keys in the map.
     * <p>
     * The Collection will be ordered by object insertion into the map.
     *
     * @see #keyList()
     * @return the fully modifiable collection view over the keys
     */
    public Set<K> keySet() {
        return new KeySetView(this);
    }

    /**
     * Gets a view over the keys in the map as a List.
     * <p>
     * The List will be ordered by object insertion into the map.
     * The List is unmodifiable.
     *
     * @see #keySet()
     * @return the unmodifiable list view over the keys
     * @since Commons Collections 3.2
     */
    public List<K> keyList() {
        return Collections.unmodifiableList(insertOrder);
    }

    /**
     * Gets a view over the values in the map.
     * <p>
     * The Collection will be ordered by object insertion into the map.
     * <p>
     * From Commons Collections 3.2, this Collection can be cast
     * to a list, see {@link #valueList()}
     *
     * @see #valueList()
     * @return the fully modifiable collection view over the values
     */
    public Collection<V> values() {
        return new ValuesView<K,V>(this);
    }

    /**
     * Gets a view over the values in the map as a List.
     * <p>
     * The List will be ordered by object insertion into the map.
     * The List supports remove and set, but does not support add.
     *
     * @see #values()
     * @return the partially modifiable list view over the values
     * @since Commons Collections 3.2
     */
    public List<V> valueList() {
        return new ValuesView<K,V>(this);
    }

    /**
     * Gets a view over the entries in the map.
     * <p>
     * The Set will be ordered by object insertion into the map.
     *
     * @return the fully modifiable set view over the entries
     */
    public Set<Map.Entry<K,V>> entrySet() {
        return new EntrySetView<K,V>(this, this.insertOrder);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the Map as a string.
     *
     * @return the Map as a String
     */
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        final StringBuffer buf = new StringBuffer(200);
        buf.append('{');
        boolean first = true;
        final Iterator<Entry<K, V>> it = entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<K,V> entry = it.next();
            K key = entry.getKey();
            V value = entry.getValue();
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(key == this ? "(this Map)" : key);
            buf.append('=');
            buf.append(value == this ? "(this Map)" : value);
        }
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the key at the specified index.
     *
     * @param index  the index to retrieve
     * @return the key at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public K get(int index) {
        return insertOrder.get(index);
    }

    /**
     * Gets the value at the specified index.
     *
     * @param index  the index to retrieve
     * @return the key at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public V getValue(int index) {
        return get(insertOrder.get(index));
    }

    public V get( final Object key )
    {
        return map.get(key);
    }

    /**
     * Gets the index of the specified key.
     *
     * @param key  the key to find the index of
     * @return the index, or -1 if not found
     */
    public int indexOf(K key) {
        return insertOrder.indexOf(key);
    }

    /**
     * Sets the value at the specified index.
     *
     * @param index  the index of the value to set
     * @return the previous value at that index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @since Commons Collections 3.2
     */
    public V setValue(int index, V value) {
        K key = insertOrder.get(index);
        return (V) put(key, value);
    }

    /**
     * Puts a key-value mapping into the map at the specified index.
     * <p>
     * If the map already contains the key, then the original mapping
     * is removed and the new mapping added at the specified index.
     * The remove may change the effect of the index. The index is
     * always calculated relative to the original state of the map.
     * <p>
     * Thus the steps are: (1) remove the existing key-value mapping,
     * then (2) insert the new key-value mapping at the position it
     * would have been inserted had the remove not ocurred.
     *
     * @param index  the index at which the mapping should be inserted
     * @param key  the key
     * @param value  the value
     * @return the value previously mapped to the key
     * @throws IndexOutOfBoundsException if the index is out of range
     * @since Commons Collections 3.2
     */
    public V put(int index, K key, V value) {
        Map<K,V> m = getMap();
        if (m.containsKey(key)) {
            V result = m.remove(key);
            int pos = insertOrder.indexOf(key);
            insertOrder.remove(pos);
            if (pos < index) {
                index--;
            }
            insertOrder.add(index, key);
            m.put(key, value);
            return result;
        } else {
            insertOrder.add(index, key);
            m.put(key, value);
            return null;
        }
    }

    /**
     * Removes the element at the specified index.
     *
     * @param index  the index of the object to remove
     * @return the removed value, or <code>null</code> if none existed
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public V remove(int index) {
        return remove(get(index));
    }

    /**
     * Gets an unmodifiable List view of the keys which changes as the map changes.
     * <p>
     * The returned list is unmodifiable because changes to the values of
     * the list (using {@link java.util.ListIterator#set(Object)}) will
     * effectively remove the value from the list and reinsert that value at
     * the end of the list, which is an unexpected side effect of changing the
     * value of a list.  This occurs because changing the key, changes when the
     * mapping is added to the map and thus where it appears in the list.
     * <p>
     * An alternative to this method is to use the better named
     * {@link #keyList()} or {@link #keySet()}.
     *
     * @see #keyList()
     * @see #keySet()
     * @return The ordered list of keys.
     */
    public List<K> asList() {
        return keyList();
    }

    //-----------------------------------------------------------------------
    static class ValuesView<K,V> extends AbstractList<V> {
        private final ListOrderedMap<K,V> parent;

        ValuesView(ListOrderedMap<K,V> parent) {
            super();
            this.parent = parent;
        }

        public int size() {
            return this.parent.size();
        }

        public boolean contains(Object value) {
            return this.parent.containsValue(value);
        }

        public void clear() {
            this.parent.clear();
        }

        public Iterator<V> iterator() {
            return new AbstractIteratorDecorator(parent.entrySet().iterator()) {
                public Object next() {
                    return ((Map.Entry) iterator.next()).getValue();
                }
            };
        }

        public V get(int index) {
            return this.parent.getValue(index);
        }

        public V set(int index, V value) {
            return this.parent.setValue(index, value);
        }

        public V remove(int index) {
            return this.parent.remove(index);
        }
    }

    //-----------------------------------------------------------------------
    static class KeySetView extends AbstractSet {
        private final ListOrderedMap parent;

        KeySetView(ListOrderedMap parent) {
            super();
            this.parent = parent;
        }

        public int size() {
            return this.parent.size();
        }

        public boolean contains(Object value) {
            return this.parent.containsKey(value);
        }

        public void clear() {
            this.parent.clear();
        }

        public Iterator iterator() {
            return new AbstractIteratorDecorator(parent.entrySet().iterator()) {
                public Object next() {
                    return ((Map.Entry) super.next()).getKey();
                }
            };
        }
    }

    //-----------------------------------------------------------------------
    static class EntrySetView< K,V>  extends AbstractSet< Map.Entry< K,V>> {
        private final ListOrderedMap parent;
        private final List insertOrder;
        private Set< Map.Entry< K, V>> entrySet;

        public EntrySetView(ListOrderedMap parent, List insertOrder) {
            super();
            this.parent = parent;
            this.insertOrder = insertOrder;
        }

        private Set getEntrySet() {
            if (entrySet == null) {
                entrySet = parent.getMap().entrySet();
            }
            return entrySet;
        }

        public int size() {
            return this.parent.size();
        }
        public boolean isEmpty() {
            return this.parent.isEmpty();
        }

        public boolean contains(Object obj) {
            return getEntrySet().contains(obj);
        }

        public boolean containsAll(Collection coll) {
            return getEntrySet().containsAll(coll);
        }

        public boolean remove(Object obj) {
            if (obj instanceof Map.Entry == false) {
                return false;
            }
            if (getEntrySet().contains(obj)) {
                Object key = ((Map.Entry) obj).getKey();
                parent.remove(key);
                return true;
            }
            return false;
        }

        public void clear() {
            this.parent.clear();
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return getEntrySet().equals(obj);
        }

        public int hashCode() {
            return getEntrySet().hashCode();
        }

        public String toString() {
            return getEntrySet().toString();
        }

        public Iterator< Map.Entry< K, V>> iterator() {
            return new ListOrderedIterator(parent, insertOrder);
        }
    }

    //-----------------------------------------------------------------------
    static class ListOrderedIterator extends AbstractIteratorDecorator {
        private final ListOrderedMap parent;
        private Object last = null;

        ListOrderedIterator(ListOrderedMap parent, List insertOrder) {
            super(insertOrder.iterator());
            this.parent = parent;
        }

        public Object next() {
            last = super.next();
            return new ListOrderedMapEntry(parent, last);
        }

        public void remove() {
            super.remove();
            parent.getMap().remove(last);
        }
    }

    //-----------------------------------------------------------------------
    static class ListOrderedMapEntry extends AbstractMapEntry {
        private final ListOrderedMap parent;

        ListOrderedMapEntry(ListOrderedMap parent, Object key) {
            super(key, null);
            this.parent = parent;
        }

        public Object getValue() {
            return parent.get(key);
        }

        public Object setValue(Object value) {
            return parent.getMap().put(key, value);
        }
    }

    //-----------------------------------------------------------------------
    static class ListOrderedMapIterator<K,V> implements OrderedMapIterator, ResettableIterator {
        private final ListOrderedMap<K,V> parent;
        private ListIterator<K> iterator;
        private K last = null;
        private boolean readable = false;

        ListOrderedMapIterator(ListOrderedMap<K,V> parent) {
            super();
            this.parent = parent;
            this.iterator = parent.insertOrder.listIterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            last = iterator.next();
            readable = true;
            return last;
        }

        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        public Object previous() {
            last = iterator.previous();
            readable = true;
            return last;
        }

        public void remove() {
            if (readable == false) {
                throw new IllegalStateException("remove() can only be called once after next()");
            }
            iterator.remove();
            parent.map.remove(last);
            readable = false;
        }

        public Object getKey() {
            if (readable == false) {
                throw new IllegalStateException("getKey() can only be called after next() and before remove()");
            }
            return last;
        }

        public Object getValue() {
            if (readable == false) {
                throw new IllegalStateException("getValue() can only be called after next() and before remove()");
            }
            return parent.get(last);
        }

        public Object setValue(Object value) {
            if (readable == false) {
                throw new IllegalStateException("setValue() can only be called after next() and before remove()");
            }
            return parent.map.put(last, (V)value);
        }

        public void reset()
        {
            iterator = parent.insertOrder.listIterator();
            last = null;
            readable = false;
        }

        public String toString() {
            if (readable == true) {
                return "Iterator[" + getKey() + "=" + getValue() + "]";
            } else {
                return "Iterator[]";
            }
        }
    }

    /**
     * Gets the map being decorated.
     *
     * @return the decorated map
     */
    protected Map<K,V> getMap() {
        return map;
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public int size() {
        return map.size();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        return map.equals(object);
    }

    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty()
    {
        return map.isEmpty();
    }
}
