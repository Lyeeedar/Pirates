package com.Lyeeedar.Util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *   Collection type a bit like ArrayList but does not preserve the order
 *   of its entities, speedwise it is very good, especially suited for games.
 */

public class Bag<E> implements List<E>, Serializable {
	
	private static final long serialVersionUID = 9141471894142964687L;
	
	private Object[] data;
	public int size = 0;

	/**
	 * Constructs an empty Bag with an initial capacity of ten.
	 *
	 */
	public Bag() {
		this(10);
	}

	/**
	 * Constructs an empty Bag with the specified initial capacity.
	 *
	 * @param capacity the initial capacity of Bag
	 */
	public Bag(int capacity) {
		data = new Object[capacity];
	}

	/**
	 * Removes the element at the specified position in this Bag.
	 * does this by overwriting it with the last element then removing 
	 * last element
	 * 
	 * @param index the index of element to be removed
	 * @return element that was removed from the Bag
	 */
	@SuppressWarnings("unchecked")
	public E remove(int index) {
		E o = (E) data[index]; // make copy of element to remove so it can be returned
		data[index] = data[--size]; // overwrite item to remove with last element
		data[size] = null; // null last element, so gc can do its work
		return o;
	}

	/**
	 * Removes the first occurrence of the specified element from this Bag,
	 * if it is present.  If the Bag does not contain the element, it is
	 * unchanged. does this by overwriting it was last element then removing 
	 * last element
	 * 
	 * @param o element to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 */
	@Override
	public boolean remove(Object o) {
		for (int i = 0; i < size; i++) {
			if (o == data[i]) {
				data[i] = data[--size]; // overwrite item to remove with last element
				data[size] = null; // null last element, so gc can do its work
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes from this Bag all of its elements that are contained in the
	 * specified Bag.
	 *
	 * @param bag Bag containing elements to be removed from this Bag
	 * @return {@code true} if this Bag changed as a result of the call
	 */
	public boolean removeAll(Bag<E> bag) {
		boolean modified = false;

		for (int i = 0; i < bag.size; i++) {
			Object o1 = bag.get(i);

			for (int j = 0; j < size; j++) {
				Object o2 = data[j];

				if (o1 == o2) {
					remove(j);
					j--;
					modified = true;
					break;
				}
			}
		}

		return modified;
	}

	/**
	 * Returns the element at the specified position in Bag.
	 *
	 * @param  index index of the element to return
	 * @return the element at the specified position in bag
	 */
	@SuppressWarnings("unchecked")
	public E get(int index) {
		return (E) data[index];
	}

	/**
	 * Returns true if this list contains no elements.
	 *
	 * @return true if this list contains no elements
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Adds the specified element to the end of this bag.
	 * if needed also increases the capacity of the bag.
	 *
	 * @param o element to be added to this list
	 * @return 
	 */
	@Override
	public boolean add(E o) {   
		// if size greater than data capacity increase capacity
		if(size == data.length) {
			grow();
		}

		data[size++] = o;
		return true;
	}

	private void grow() {
		Object[] oldData = data;
		int newCapacity = (oldData.length * 3) / 2 + 1;
		data = new Object[newCapacity];
		System.arraycopy(oldData, 0, data, 0, oldData.length);
	}
	
	public void set(Bag<E> other)
	{
		data = new Object[other.data.length];
		System.arraycopy(other.data, 0, data, 0, other.data.length);
		size = other.size;
	}

	/**
	 * Removes all of the elements from this bag. The bag will
	 * be empty after this call returns.
	 */
	public void clear() {
		// null all elements so gc can clean up
		for (int i = 0; i < size; i++) {
			data[i] = null;
		}

		size = 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new BagIterator(0, size);
	}
	
	class BagIterator implements Iterator<E>
	{
		int pos = 0;
		int size = 0;
		
		public BagIterator(){}
		public BagIterator(int pos, int size)
		{
			this.pos = pos;
			this.size = size;
		}

		@Override
		public boolean hasNext() {
			return (pos < size);
		}

		@SuppressWarnings("unchecked")
		@Override
		public E next() {
			return (E) data[pos++];
		}

		@Override
		public void remove() {
			Bag.this.remove(--pos);
			size--;
		}
	}
	
	  /**
     * Save the state of the <tt>ArrayList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>ArrayList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException{
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();

        // Write out array length
        s.writeInt(data.length);

        // Write out all elements in the proper order.
        for (int i=0; i<size; i++)
            s.writeObject(data[i]);
    }

    /**
     * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();

        // Read in array length and allocate array
        int arrayLength = s.readInt();
        Object[] a = data = new Object[arrayLength];

        // Read in all elements in the proper order.
        for (int i=0; i<size; i++)
            a[i] = s.readObject();
    }
    
    public void sort()
    {
    	Arrays.sort(data);
    }

	@Override
	public void add(int index, E element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public E set(int index, E element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}