/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sais.fantasyfesta.core;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.AbstractListModel;

/**
 *
 * @author Romulus
 */
public class MyListModel extends AbstractListModel {

  private Vector<Object> delegate = new Vector<Object>();

  @Override
  public Object getElementAt(int index) {
    return delegate.elementAt(index);
  }

  public void copyInto(Object anArray[]) {
    delegate.copyInto(anArray);
  }

  public void trimToSize() {
    delegate.trimToSize();
  }

  public void ensureCapacity(int minCapacity) {
    delegate.ensureCapacity(minCapacity);
  }

  public void setSize(int newSize) {
    int oldSize = delegate.size();
    delegate.setSize(newSize);
    if (oldSize > newSize) {
      fireIntervalRemoved(this, newSize, oldSize - 1);
    } else if (oldSize < newSize) {
      fireIntervalAdded(this, oldSize, newSize - 1);
    }
  }

  public int capacity() {
    return delegate.capacity();
  }

  public int size() {
    return delegate.size();
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public Enumeration<?> elements() {
    return delegate.elements();
  }

  public boolean contains(Object elem) {
    return delegate.contains(elem);
  }

  public int indexOf(Object elem) {
    return delegate.indexOf(elem);
  }

  public int lastIndexOf(Object elem) {
    return delegate.lastIndexOf(elem);
  }

  public int lastIndexOf(Object elem, int index) {
    return delegate.lastIndexOf(elem, index);
  }

  public Object elementAt(int index) {
    return delegate.elementAt(index);
  }

  public Object firstElement() {
    return delegate.firstElement();
  }

  public Object lastElement() {
    return delegate.lastElement();
  }

  public void setElementAt(Object obj, int index) {
    delegate.setElementAt(obj, index);
    fireContentsChanged(this, index, index);
  }

  public void removeElementAt(int index) {
    delegate.removeElementAt(index);
    fireIntervalRemoved(this, index, index);
  }

  public void insertElementAt(Object obj, int index) {
    delegate.insertElementAt(obj, index);
    fireIntervalAdded(this, index, index);
  }

  public void addElement(Object obj) {
    int index = delegate.size();
    delegate.addElement(obj);
    fireIntervalAdded(this, index, index);
  }

  public void addElementwithoutFire(Object obj) {
    int index = delegate.size();
    delegate.addElement(obj);
  }

  public void fire() {
    fireIntervalAdded(this, delegate.size(), delegate.size());
  }

  public boolean removeElement(Object obj) {
    int index = indexOf(obj);
    boolean rv = delegate.removeElement(obj);
    if (index >= 0) {
      fireIntervalRemoved(this, index, index);
    }
    return rv;
  }

  public void removeAllElements() {
    int index1 = delegate.size() - 1;
    delegate.removeAllElements();
    if (index1 >= 0) {
      fireIntervalRemoved(this, 0, index1);
    }
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  public Object[] toArray() {
    Object[] rv = new Object[delegate.size()];
    delegate.copyInto(rv);
    return rv;
  }

  public Object get(int index) {
    return delegate.elementAt(index);
  }

  public Object set(int index, Object element) {
    Object rv = delegate.elementAt(index);
    delegate.setElementAt(element, index);
    fireContentsChanged(this, index, index);
    return rv;
  }

  public void add(int index, Object element) {
    delegate.insertElementAt(element, index);
    fireIntervalAdded(this, index, index);
  }

  public Object remove(int index) {
    Object rv = delegate.elementAt(index);
    delegate.removeElementAt(index);
    fireIntervalRemoved(this, index, index);
    return rv;
  }

  public void clear() {
    int index1 = delegate.size() - 1;
    delegate.removeAllElements();
    if (index1 >= 0) {
      fireIntervalRemoved(this, 0, index1);
    }
  }

  public void removeRange(int fromIndex, int toIndex) {
    if (fromIndex > toIndex) {
      throw new IllegalArgumentException("fromIndex must be <= toIndex");
    }
    for (int i = toIndex; i >= fromIndex; i--) {
      delegate.removeElementAt(i);
    }
    fireIntervalRemoved(this, fromIndex, toIndex);
  }

  @Override
  public int getSize() {
    return delegate.size();
  }
}

