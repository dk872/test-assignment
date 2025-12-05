/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Implements a Linear Doubly Linked List to represent a number
 * in the Binary (base 2) system, based on student record book number C=15.
 * <p>
 * C = 15
 * C3 = 15 mod 3 = 0 (Linear Doubly Linked List)
 * C5 = 15 mod 5 = 0 (Base system: Binary - base 2)
 * C7 = 15 mod 7 = 1 (Additional operation: Subtraction)
 * Additional system for changeScale: (C5+1) mod 5 = 1 (Ternary - base 3)
 *
 * @author Dmytro Kulyk, IM-32, № 15
 *
 */
public class NumberListImpl implements NumberList {

    private static final int RECORD_BOOK_NUMBER = 15;
    private static final int BASE = 2; // Primary base for C5=0 (Binary)
    private static final int CHANGE_SCALE_BASE = 3; // Additional base for (C5+1)mod 5 = 1 (Ternary)

    private Node head;
    private Node tail;
    private int size;
    private final int currentBase;

    /**
     * Internal class representing a node in the Doubly Linked List.
     */
    private static class Node {
        Byte data;
        Node next;
        Node prev;

        /**
         * Creates a new Node with specified data and links to the previous and next nodes.
         * @param data The digit stored in the node.
         * @param prev The previous node in the list.
         * @param next The next node in the list.
         */
        public Node(Byte data, Node prev, Node next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }

    /**
     * Converts a BigInteger to a NumberListImpl in a specified base.
     * The list stores digits from most significant (head) to least significant (tail).
     *
     * @param value The number as BigInteger.
     * @param base The target base.
     * @return A new NumberListImpl representing the number in the specified base.
     */
    private static NumberListImpl fromDecimalBigInteger(BigInteger value, int base) {
        NumberListImpl list = new NumberListImpl(base);
        if (value.compareTo(BigInteger.ZERO) < 0) {
            // Numbers are non-negative in the assignment context.
            return list;
        }
        if (value.equals(BigInteger.ZERO)) {
            list.addLastInternal((byte) 0);
            return list;
        }

        BigInteger current = value;
        BigInteger baseBI = BigInteger.valueOf(base);

        // Uses repeated division to convert to target base
        while (current.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] division = current.divideAndRemainder(baseBI);
            byte digit = division[1].byteValue();
            // Add to head to maintain correct order (MSD first)
            list.addFirstInternal(digit);
            current = division[0];
        }
        return list;
    }

    /**
     * Converts the number stored in this list to a BigInteger (assuming the currentBase is correct).
     * @return The number as BigInteger.
     */
    private BigInteger toDecimalBigInteger() {
        if (isEmpty()) {
            return BigInteger.ZERO;
        }
        BigInteger result = BigInteger.ZERO;
        BigInteger baseBI = BigInteger.valueOf(this.currentBase);

        // Digits are stored in MSB...LSB order, so iterate from head to tail.
        Node current = head;
        while (current != null) {
            result = result.multiply(baseBI).add(BigInteger.valueOf(current.data));
            current = current.next;
        }

        return result;
    }

    /**
     * Private constructor used internally to set the base of the number list.
     * @param base The base of the number representation.
     */
    private NumberListImpl(int base) {
        this.currentBase = base;
        this.size = 0;
    }

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this(BASE);
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by loading a **decimal** number
     * from a file, defined in string format. The number is then converted to the list's base (Binary, Base 2).
     * If the file is not found or contains invalid data, the list remains empty.
     *
     * @param file - file where the decimal number is stored.
     */
    public NumberListImpl(File file) {
        this(); // Initialize with default base (BASE=2)
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null) {
                // Read the first line and trim whitespace
                sb.append(line.trim());
            }
        } catch (IOException e) {
            // File not found or other IO error, list remains empty.
            return;
        }

        String value = sb.toString();
        // Check if the content is a valid positive decimal number (non-empty, only digits, non-negative)
        if (value.isEmpty() || !value.matches("\\d+") || value.startsWith("-")) {
            // Invalid input, list remains empty
            return;
        }

        try {
            BigInteger decimalValue = new BigInteger(value);
            // Convert the decimal value to the list's base (Binary: BASE=2)
            NumberListImpl temp = fromDecimalBigInteger(decimalValue, this.currentBase);
            this.head = temp.head;
            this.tail = temp.tail;
            this.size = temp.size;
            temp.head = temp.tail = null; // Detach nodes from temp to prevent accidental clear
        } catch (NumberFormatException e) {
            // Invalid input, list remains empty
        }
    }


    /**
     * Constructs new <tt>NumberListImpl</tt> by converting a **decimal** number
     * provided in string notation to the list's base (Binary, Base 2).
     * If the string is invalid (null, empty, non-digit, or negative), the list remains empty.
     *
     * @param value - number in string notation (decimal).
     */
    public NumberListImpl(String value) {
        this(); // Initialize with default base (BASE=2)
        if (value == null || value.isEmpty() || !value.matches("\\d+") || value.startsWith("-")) {
            // Invalid input, list remains empty
            return;
        }

        try {
            BigInteger decimalValue = new BigInteger(value);
            // Convert the decimal value to the list's base (Binary: BASE=2)
            NumberListImpl temp = fromDecimalBigInteger(decimalValue, this.currentBase);
            this.head = temp.head;
            this.tail = temp.tail;
            this.size = temp.size;
            temp.head = temp.tail = null; // Detach nodes from temp
        } catch (NumberFormatException e) {
            // Invalid input, list remains empty
        }
    }


    /**
     * Saves the number, stored in the list, into the specified file
     * in **decimal** scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        String decimalString = toDecimalString();
        try (PrintWriter pw = new PrintWriter(file)) {
            pw.write(decimalString);
        } catch (IOException e) {
            // Handle IO exception if needed
        }
    }


    /**
     * Returns the student's record book number.
     *
     * @return student's record book number (15).
     */
    public static int getRecordBookNumber() {
        return RECORD_BOOK_NUMBER;
    }


    /**
     * Returns a new <tt>NumberListImpl</tt> which represents the same number
     * in the additional scale of notation (Ternary, Base 3).
     * The original list remains unchanged.
     *
     * @return <tt>NumberListImpl</tt> in Ternary (Base 3) scale of notation.
     */
    public NumberListImpl changeScale() {
        BigInteger decimalValue = this.toDecimalBigInteger();
        // Convert from Binary (BASE=2) to Ternary (CHANGE_SCALE_BASE=3)
        return fromDecimalBigInteger(decimalValue, CHANGE_SCALE_BASE);
    }


    /**
     * Returns a new <tt>NumberListImpl</tt> which represents the result of
     * the additional operation: **Subtraction** (list1 - arg).
     * If the result is negative, an empty list is returned.
     * The original list and the argument list remain unchanged.
     *
     * @param arg - second argument (subtrahend) of the additional operation.
     *
     * @return result of subtraction in the current base (Binary, Base 2), or an empty list if negative.
     * @throws IllegalArgumentException if the argument is not of type NumberListImpl.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        if (!(arg instanceof NumberListImpl)) {
            throw new IllegalArgumentException("Argument must be of type NumberListImpl.");
        }

        BigInteger val1 = this.toDecimalBigInteger();
        BigInteger val2 = ((NumberListImpl) arg).toDecimalBigInteger();

        // Subtraction: list1 - list2
        BigInteger resultValue = val1.subtract(val2);

        // Only positive or zero results are returned as a NumberListImpl
        if (resultValue.compareTo(BigInteger.ZERO) < 0) {
            return new NumberListImpl(this.currentBase); // Return empty list for negative result
        }

        // Convert the result back to the original list's base (BASE=2)
        return fromDecimalBigInteger(resultValue, this.currentBase);
    }


    /**
     * Returns the string representation of the number stored in the list
     * in **decimal** scale of notation.
     *
     * @return string representation in **decimal** scale.
     */
    public String toDecimalString() {
        return toDecimalBigInteger().toString();
    }


    /**
     * Returns the string representation of the number stored in the list
     * in its **current base** (Binary, Base 2).
     *
     * @return string representation in the current base.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Node current = head;
        while (current != null) {
            sb.append(current.data.byteValue());
            current = current.next;
        }
        return sb.toString();
    }


    /**
     * Compares this NumberListImpl object with the specified object for equality.
     * Two lists are considered equal if they are of the same class, have the same size,
     * the same base, and contain the same sequence of elements (digits).
     *
     * @param o The object to compare with.
     * @return true if the lists are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberListImpl that = (NumberListImpl) o;

        if (this.size != that.size) return false;
        if (this.currentBase != that.currentBase) return false;

        Node currentThis = this.head;
        Node currentThat = that.head;
        while (currentThis != null) {
            if (!currentThis.data.equals(currentThat.data)) {
                return false;
            }
            currentThis = currentThis.next;
            currentThat = currentThat.next;
        }
        return true;
    }

    /**
     * Returns the number of elements (digits) in this list.
     *
     * @return the number of elements in this list.
     */
    @Override
    public int size() {
        return size;
    }


    /**
     * Returns true if this list contains no elements.
     *
     * @return true if this list is empty, false otherwise.
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }


    /**
     * Returns true if this list contains the specified element (digit).
     *
     * @param o element whose presence in this list is to be tested.
     * @return true if this list contains the specified element, false otherwise.
     */
    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte b)) return false;
        Node current = head;
        while (current != null) {
            if (current.data.equals(b)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }


    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list.
     */
    @Override
    public Iterator<Byte> iterator() {
        return new Itr(0);
    }

    /**
     * Internal implementation of the standard Iterator for the Doubly Linked List.
     */
    private class Itr implements Iterator<Byte> {
        private Node lastReturned;
        private Node next;
        private int nextIndex;

        Itr(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public Byte next() {
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.data;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();

            NumberListImpl.this.remove(lastReturned);
            lastReturned = null;
            if (next == null)
                nextIndex = size;
            else
                nextIndex--;
        }
    }

    /**
     * Returns the Node at the specified position in this list.
     * @param index index of the Node to return.
     * @return the Node at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    private Node node(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);

        Node x;
        if (index < (size >> 1)) { // Search from head (faster for first half)
            x = head;
            for (int i = 0; i < index; i++)
                x = x.next;
        } else { // Search from tail (faster for second half)
            x = tail;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
        }
        return x;
    }

    /**
     * Inserts the specified element into the list between the previous and next nodes.
     * Updates head/tail pointers if necessary.
     * @param prevNode The node preceding the new element.
     * @param element The element to be stored in the new node.
     * @param nextNode The node succeeding the new element.
     */
    private void link(Node prevNode, Byte element, Node nextNode) {
        Node newNode = new Node(element, prevNode, nextNode);

        if (prevNode == null) {
            head = newNode;
        } else {
            prevNode.next = newNode;
        }

        if (nextNode == null) {
            tail = newNode;
        } else {
            nextNode.prev = newNode;
        }
        size++;
    }

    /**
     * Unlinks a non-null node x from the list.
     * @param x The node to be unlinked.
     * @return The data contained in the unlinked node.
     */
    private Byte unlink(Node x) {
        Byte element = x.data;
        Node next = x.next;
        Node prev = x.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.data = null;
        size--;
        return element;
    }

    /**
     * Inserts the specified element at the beginning of the list (internal use).
     * @param e the element to add.
     */
    private void addFirstInternal(Byte e) {
        final Node f = head;
        final Node newNode = new Node(e, null, f);
        head = newNode;
        if (f == null)
            tail = newNode;
        else
            f.prev = newNode;
        size++;
    }

    /**
     * Appends the specified element to the end of this list (internal use).
     * @param e the element to add.
     */
    private void addLastInternal(Byte e) {
        final Node l = tail;
        final Node newNode = new Node(e, l, null);
        tail = newNode;
        if (l == null)
            head = newNode;
        else
            l.next = newNode;
        size++;
    }

    /**
     * Removes the specified internal node from the list.
     *
     * @param x The node to remove.
     */
    private void remove(Node x) {
        if (x == null) return;
        unlink(x);
    }


    /**
     * Returns an array containing all of the elements in this list in proper sequence.
     *
     * @return an array containing all of the elements in this list.
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node x = head; x != null; x = x.next)
            result[i++] = x.data;
        return result;
    }


    /**
     * This method is explicitly NOT required by the assignment and returns null.
     *
     * @param a The array into which the elements of the list are to be stored.
     * @return null, as per assignment requirement.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        // This method is explicitly NOT required by the assignment
        return null;
    }


    /**
     * Appends the specified element (digit) to the end of this list.
     * Throws an exception if the element is null or not a valid digit for the current base.
     *
     * @param e element to be appended to this list.
     * @return true (as specified by Collection.add(E)).
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if the specified element is not a valid digit for the current base.
     */
    @Override
    public boolean add(Byte e) {
        if (e == null) throw new NullPointerException("Null elements are not permitted.");
        // Validate digit for the current base
        if (e < 0 || e >= currentBase) {
            throw new IllegalArgumentException("Digit " + e + " is invalid for base " + currentBase);
        }
        addLastInternal(e);
        return true;
    }


    /**
     * Removes the first occurrence of the specified element (digit) from this list, if it is present.
     *
     * @param o element to be removed from this list, if present.
     * @return true if this list contained the specified element, false otherwise.
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) return false;
        Node current = head;
        while (current != null) {
            if (o.equals(current.data)) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }


    /**
     * Returns true if this list contains all of the elements of the specified collection.
     *
     * @param c collection to be checked for containment in this list.
     * @return true if this list contains all of the elements of the specified collection, false otherwise.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e))
                return false;
        }
        return true;
    }


    /**
     * Appends all of the elements in the specified collection to the end of this list,
     * in the order that they are returned by the specified collection's iterator.
     *
     * @param c collection containing elements to be added to this list.
     * @return true if this list changed as a result of the call.
     */
    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        return addAll(size, c);
    }


    /**
     * Inserts all of the elements in the specified collection into this list,
     * starting at the specified position.
     *
     * @param index index at which to insert the first element from the specified collection.
     * @param c collection containing elements to be added to this list.
     * @return true if this list changed as a result of the call.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if any element in the collection is null.
     * @throws IllegalArgumentException if any element is not a valid digit for the current base.
     */
    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        if (c.isEmpty()) return false;

        Object[] a = c.toArray();

        Node pred, succ;
        if (index == size) {
            succ = null;
            pred = tail;
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        // Loop to add all elements from the collection
        for (Object o : a) {
            if (o == null) throw new NullPointerException("Null elements are not permitted.");
            Byte e = (Byte) o;
            // Validate digit for the current base
            if (e < 0 || e >= currentBase) {
                throw new IllegalArgumentException("Digit " + e + " is invalid for base " + currentBase);
            }
            Node newNode = new Node(e, pred, null);
            if (pred == null) {
                head = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;
            size++;
        }

        if (succ == null) {
            tail = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }
        return true;
    }


    /**
     * Removes from this list all of its elements that are also contained in the specified collection.
     *
     * @param c collection containing elements to be removed from this list.
     * @return true if this list changed as a result of the call.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        if (c != null && !c.isEmpty()) {
            Node current = head;
            while (current != null) {
                Node next = current.next;
                if (c.contains(current.data)) {
                    unlink(current);
                    modified = true;
                }
                current = next;
            }
        }
        return modified;
    }


    /**
     * Retains only the elements in this list that are contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this list.
     * @return true if this list changed as a result of the call.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            if (!c.contains(current.data)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }


    /**
     * Removes all of the elements from this list. The list will be empty after this call returns.
     */
    @Override
    public void clear() {
        // Clear references to allow garbage collection
        Node current = head;
        while (current != null) {
            Node next = current.next;
            current.data = null;
            current.next = null;
            current.prev = null;
            current = next;
        }
        head = tail = null;
        size = 0;
    }


    /**
     * Returns the element (digit) at the specified position in this list.
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public Byte get(int index) {
        return node(index).data;
    }


    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index index of the element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if the specified element is not a valid digit for the current base.
     */
    @Override
    public Byte set(int index, Byte element) {
        if (element == null) throw new NullPointerException("Null elements are not permitted.");
        // Validate digit for the current base
        if (element < 0 || element >= currentBase) {
            throw new IllegalArgumentException("Digit " + element + " is invalid for base " + currentBase);
        }
        Node x = node(index);
        Byte oldVal = x.data;
        x.data = element;
        return oldVal;
    }


    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent elements to the right.
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws IndexOutOfBoundsException if the index is out of range.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if the specified element is not a valid digit for the current base.
     */
    @Override
    public void add(int index, Byte element) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        if (element == null) throw new NullPointerException("Null elements are not permitted.");
        // Validate digit for the current base
        if (element < 0 || element >= currentBase) {
            throw new IllegalArgumentException("Digit " + element + " is invalid for base " + currentBase);
        }

        if (index == size) {
            addLastInternal(element);
        } else {
            link(node(index).prev, element, node(index));
        }
    }


    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left.
     *
     * @param index the index of the element to be removed.
     * @return the element previously at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public Byte remove(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        return unlink(node(index));
    }


    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     *
     * @param o element to search for.
     * @return the index of the first occurrence of the specified element in this list, or -1 if not found.
     */
    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte b)) return -1;
        int index = 0;
        Node current = head;
        while (current != null) {
            if (current.data.equals(b)) {
                return index;
            }
            index++;
            current = current.next;
        }
        return -1;
    }


    /**
     * Returns the index of the last occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     *
     * @param o element to search for.
     * @return the index of the last occurrence of the specified element in this list, or -1 if not found.
     */
    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte b)) return -1;
        int index = size - 1;
        Node current = tail;
        while (current != null) {
            if (current.data.equals(b)) {
                return index;
            }
            index--;
            current = current.prev;
        }
        return -1;
    }


    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     *
     * @return a list iterator over the elements in this list.
     */
    @Override
    public ListIterator<Byte> listIterator() {
        return new ListItr(0);
    }


    /**
     * Returns a list iterator over the elements in this list (in proper sequence),
     * starting at the specified position in the list.
     *
     * @param index index of the first element to be returned from the list iterator.
     * @return a list iterator over the elements in this list, starting at the specified position.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    @Override
    public ListIterator<Byte> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        return new ListItr(index);
    }

    /**
     * Internal implementation of the standard ListIterator for the Doubly Linked List.
     */
    private class ListItr implements ListIterator<Byte> {
        private Node lastReturned;
        private Node next;
        private int nextIndex;

        ListItr(int index) {
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public Byte next() {
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.data;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public Byte previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? tail : next.prev;
            nextIndex--;
            return lastReturned.data;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();

            Node lastNext = lastReturned.next;
            NumberListImpl.this.remove(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
        }

        public void set(Byte e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (e == null) throw new NullPointerException("Null elements are not permitted.");
            // Validate digit for the current base
            if (e < 0 || e >= currentBase) {
                throw new IllegalArgumentException("Digit " + e + " is invalid for base " + currentBase);
            }
            lastReturned.data = e;
        }

        public void add(Byte e) {
            if (e == null) throw new NullPointerException("Null elements are not permitted.");
            // Validate digit for the current base
            if (e < 0 || e >= currentBase) {
                throw new IllegalArgumentException("Digit " + e + " is invalid for base " + currentBase);
            }
            lastReturned = null;
            if (next == null) {
                addLastInternal(e);
            } else {
                link(next.prev, e, next);
            }
            nextIndex++;
        }
    }


    /**
     * Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, exclusive.
     * Returns a new NumberListImpl containing the elements of the sublist.
     *
     * @param fromIndex low endpoint (inclusive) of the subList.
     * @param toIndex high endpoint (exclusive) of the subList.
     * @return a new NumberListImpl representing the specified range within this list.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value.
     */
    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", size: " + size);
        }
        // Returns a copy as a new NumberListImpl.
        NumberListImpl newList = new NumberListImpl(this.currentBase);
        Node current = node(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            newList.addLastInternal(current.data);
            current = current.next;
        }
        return newList;
    }


    /**
     * Exchanges two list elements, specified by indexes, by swapping their data.
     * Indexes starts from 0.
     *
     * @param index1 - index of the first element.
     * @param index2 - index of the second element.
     * @return <tt>true</tt> if operation was successful, otherwise <tt>false</tt>.
     */
    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            return false;
        }
        if (index1 == index2) return true;

        // Swap data instead of nodes for simplicity in a linear list
        Node node1 = node(index1);
        Node node2 = node(index2);

        Byte temp = node1.data;
        node1.data = node2.data;
        node2.data = temp;

        return true;
    }


    /**
     * Sorts elements (digits) of the list in ascending order (Bubble Sort used for simplicity).
     */
    @Override
    public void sortAscending() {
        if (size < 2) return;

        // Simple bubble sort for custom linked list by swapping data
        boolean swapped;
        Node current;
        Node last = null;

        do {
            swapped = false;
            current = head;
            while (current.next != last) {
                if (current.data.compareTo(current.next.data) > 0) {
                    // Swap data
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
            last = current;
        } while (swapped);
    }


    /**
     * Sorts elements (digits) of the list in descending order (Bubble Sort used for simplicity).
     */
    @Override
    public void sortDescending() {
        if (size < 2) return;

        // Simple bubble sort for custom linked list by swapping data
        boolean swapped;
        Node current;
        Node last = null;

        do {
            swapped = false;
            current = head;
            while (current.next != last) {
                if (current.data.compareTo(current.next.data) < 0) {
                    // Swap data
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
            last = current;
        } while (swapped);
    }


    /**
     * Performs a left cyclic shift on the elements (digits) in the current list.
     * The first element moves to the last position.
     */
    @Override
    public void shiftLeft() {
        if (size < 2) return;

        // Move head's data to the end
        Byte first = head.data;
        Node current = head;

        // Shift data one position to the left (current gets data from current.next)
        while (current.next != null) {
            current.data = current.next.data;
            current = current.next;
        }
        // The last node gets the original first element's data
        current.data = first;
    }


    /**
     * Performs a right cyclic shift on the elements (digits) in the current list.
     * The last element moves to the first position.
     */
    @Override
    public void shiftRight() {
        if (size < 2) return;

        // Move tail's data to the beginning
        Byte last = tail.data;
        Node current = tail;

        // Shift data one position to the right (current gets data from current.prev)
        while (current.prev != null) {
            current.data = current.prev.data;
            current = current.prev;
        }
        // The first node gets the original last element's data
        current.data = last;
    }
}
