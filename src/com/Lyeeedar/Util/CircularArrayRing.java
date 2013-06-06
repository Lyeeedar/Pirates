/*******************************************************************************
 * Copyright (c) 2013 Philip Collin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Philip Collin - initial API and implementation
 ******************************************************************************/
package com.Lyeeedar.Util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * A Circular Array Ring Data Structure implementation written by me.
 * @author Lyeeedar
 *
 * @param <E>
 */
public class CircularArrayRing<E> extends AbstractCollection<E> implements Ring<E> {

	private E[] ring;
	private int head;
	private boolean filled = false;

	@SuppressWarnings("unchecked")
	public CircularArrayRing(int i) 
	{
		ring = (E[]) new Object[i];
		head = 0;
	}

	@SuppressWarnings("unchecked")
	public CircularArrayRing()
	{
		ring = (E[]) new Object[20];
		head = 0;
	}

	public boolean add(E e) {
		ring[head] = e;
		head++;
		if (head == ring.length)
		{
			head = 0;
			if (!filled)
			{
				filled = true;
			}
		}
		return false;
	}
	
	public E peekAndMove()
	{
		E e = ring[head];
		head++;
		if (head == ring.length)
		{
			head = 0;
			if (!filled)
			{
				filled = true;
			}
		}
		return e;
	}

	public E get(int index) throws IndexOutOfBoundsException {
		if (index >= ring.length)
		{
			throw new IndexOutOfBoundsException();
		}
		else if ((!filled) && (index > head))
		{
			throw new IndexOutOfBoundsException();
		}
		else if (index < 0)
		{
			throw new IndexOutOfBoundsException();
		}
		index = head-index-1;
		if (index < 0)
		{
			index = ring.length + index;
		}

		return ring[index];
	}
	
	private RingIterator<E> iterator = new RingIterator<E>(this);
	public RingIterator<E> staticIterator()
	{
		iterator.reset();
		return iterator;
	}

	public Iterator<E> iterator() {
		return new RingIterator<E>(this);
	}

	public int size() {
		return (!filled) ? head : ring.length;
	}
	
	private static final class RingIterator<E> implements Iterator<E>, Iterable<E>
	{
		private final CircularArrayRing<E> car;
		private int pos;
		private int nextPos;
		
		public RingIterator(CircularArrayRing<E> car)
		{
			this.car = car;
			
			pos = car.head-1;
		}
		
		public void reset()
		{
			pos = car.head-1;
			nextPos = 0;
		}
		
		public boolean hasNext() {
			nextPos = pos - 1;
			if (nextPos < 0)
			{
				nextPos = car.ring.length - 1 ;
			}

			return !(nextPos == car.head);
		}  

		public E next() {  
			if(hasNext())
			{
				pos--;
				if (pos < 0)
				{
					pos = car.ring.length - 1;
				}
				return car.ring[pos];
			}
			else
			{
				throw new NoSuchElementException();
			}

		}  

		public void remove() {  
			throw new UnsupportedOperationException(); 
		}

		@Override
		public Iterator<E> iterator() {
			return this;
		}
	}
}

interface Ring<E> extends Collection<E> {

    public E get(int index) throws IndexOutOfBoundsException;

    public Iterator<E> iterator();

    public int size();
}
