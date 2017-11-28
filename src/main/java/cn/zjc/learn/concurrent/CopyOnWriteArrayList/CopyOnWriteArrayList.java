package cn.zjc.learn.concurrent.CopyOnWriteArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import cn.zjc.learn.concurrent.ReentrantLock.ReentrantLock;

public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
	private static final long serialVersionUID = -4064541614520320888L;

	private final ReentrantLock lock = new ReentrantLock();
	private Object[] array;

	public Object[] getArray() {
		return array;
	}

	public void setArray(Object[] array) {
		this.array = array;
	}

	/** 每次新增 都会新建一个数组 */
	@Override
	public boolean add(E e) {
		lock.lock();
		try {
			Object[] elements = getArray();
			int length = elements.length;
			Object[] newElements = Arrays.copyOf(elements, length + 1);
			newElements[length] = e;
			setArray(newElements);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/** ???该方法为什么不一进去就加锁 然后调用remove(index) */
	@Override
	public boolean remove(Object o) {
		Object[] snapshot = getArray();
		int index = indexOf(o, snapshot, 0, snapshot.length);
		if (index < 0)
			return false;

		// remove(index);// 直接使用remove(index) 并发的话可能会抛异常 不能使用

		lock.lock();
		try {
			Object[] current = getArray();
			int len = current.length;
			if (snapshot != current)
				findIndex: {
					int prefix = Math.min(index, len);
					for (int i = 0; i < prefix; i++) {
						if (current[i] != snapshot[i] && eq(o, current[i])) {
							index = i;
							break findIndex;
						}
					}
					if (index >= len)
						return false;
					if (current[index] == o)
						break findIndex;
					index = indexOf(o, current, index, len);
					if (index < 0)
						return false;
				}
			Object[] newElements = new Object[len - 1];
			System.arraycopy(current, 0, newElements, 0, index);
			System.arraycopy(current, index + 1, newElements, index, len - index - 1);
			setArray(newElements);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 该方法会抛异常
	 * 
	 * @throws IndexOutOfBoundsException
	 *             {@inheritDoc}
	 */
	@Override
	public E remove(int index) {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			Object[] elements = getArray();
			int len = elements.length;
			E oldValue = get(index);
			if (index == len)
				setArray(Arrays.copyOf(elements, len - 1));
			else {
				Object[] newElements = new Object[len - 1];
				System.arraycopy(elements, 0, newElements, 0, index);
				System.arraycopy(elements, index + 1, newElements, index, len - index - 1);
				setArray(newElements);
			}
			return oldValue;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object object : c) {
			if (!contains(object)) {
				return false;
			}
		}
		return false;
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
	public void clear() {
		// TODO Auto-generated method stub

	}

	/** get操作 不加锁 访问的是原array */
	@Override
	public E get(int index) {
		Object[] elements = getArray();
		E object = (E) elements[index];
		return object;
	}

	@Override
	public E set(int index, E element) {
		lock.lock();
		try {
			E oldValue = get(index);
			if (oldValue == element) {
				return oldValue;
			}
			Object[] elements = getArray();
			Object[] newEle = Arrays.copyOf(elements, elements.length);
			newEle[index] = element;
			setArray(newEle);
			return oldValue;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void add(int index, E element) {
		lock.lock();

		try {
			Object[] srcEle = getArray();
			int len = srcEle.length;
			if (index < 0 || index > len) {
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + len);
			}
			Object[] newEle;
			if (index == len) {
				newEle = Arrays.copyOf(srcEle, len + 1);
			} else {
				newEle = new Object[len + 1];
				System.arraycopy(srcEle, 0, newEle, 0, index);// index前
				System.arraycopy(srcEle, index, newEle, index + 1, len - index);// index后
			}
			newEle[index] = element;
			setArray(newEle);
		} finally {
			lock.unlock();
		}

	}

	@Override
	public int indexOf(Object o) {
		Object[] elements = getArray();
		return indexOf(o, elements, 0, elements.length);
	}

	@Override
	public int lastIndexOf(Object o) {
		Object[] elements = getArray();
		for (int i = elements.length; i >= 0; i--) {
			Object e = elements[i];
			if (eq(o, e)) {
				return i;
			}
		}
		return -1;
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
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	private static int indexOf(Object o, Object[] elements, int index, int fence) {
		if (o == null) {
			for (int i = index; i < fence; i++)
				if (elements[i] == null)
					return i;
		} else {
			for (int i = index; i < fence; i++)
				if (o.equals(elements[i]))
					return i;
		}
		return -1;
	}

	@Override
	public int size() {
		return getArray().length;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean contains(Object o) {
		Object[] elements = getArray();
		return indexOf(o, elements, 0, elements.length) > -1;
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		Object[] elements = getArray();
		return Arrays.copyOf(elements, elements.length);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	private static boolean eq(Object o1, Object o2) {
		return (o1 == null) ? o2 == null : o1.equals(o2);
	}
}
