package me.blueslime.bukkitmeteor.utils.list;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings("unused")
public class OptimizedList<E> extends AbstractList<E> {

    // Root node of the tree
    private CompactNode<E> root;

    // Size of the tree (number of elements)
    private int size = 0;

    // Lock for thread safety
    private final StampedLock lock = new StampedLock();

    /**
     * Default constructor for OptimizedList.
     */
    public OptimizedList() {

    }

    /**
     * Constructs an OptimizedList containing the elements of the specified collection.
     *
     * @param collection the collection whose elements are to be placed into this list.
     */
    public OptimizedList(final Collection<E> collection) {
        addAll(collection);
    }

    /**
     * Constructs an OptimizedList containing the elements of the specified list.
     *
     * @param list the list whose elements are to be placed into this list.
     */
    public OptimizedList(final List<E> list) {
        addAll(list);
    }

    /**
     * Constructs an OptimizedList containing the specified elements.
     *
     * @param elements the elements to be placed into this list.
     */
    @SafeVarargs
    public OptimizedList(final E... elements) {
        addAllOf(elements);
    }

    /**
     * Creates an empty OptimizedList.
     *
     * @param <E> the type of elements in the list.
     * @return a new empty OptimizedList.
     */
    public static <E> OptimizedList<E> create() {
        return new OptimizedList<>();
    }

    /**
     * Creates an empty OptimizedList for a specific class type.
     *
     * @param <E> the type of elements in the list.
     * @param clazz the class type.
     * @return a new empty OptimizedList.
     */
    public static <E> OptimizedList<E> create(Class<E> clazz) {
        return new OptimizedList<>();
    }

    /**
     * CompactNode class representing an element in the tree
     */
    private static final class CompactNode<E> {
        E value;
        CompactNode<E>[] children; // Array for left and right child
        byte height;

        @SuppressWarnings("unchecked")
        CompactNode(E value) {
            this.value = value;
            this.children = (CompactNode<E>[]) new CompactNode[2]; // Left at index 0, Right at index 1
            this.height = 1;
        }
    }

    /**
     * Adds an element to the list.
     *
     * @param element the element to add.
     * @return the current OptimizedList instance.
     */
    public OptimizedList<E> addElement(final E element) {
        add(element);
        return this;
    }

    /**
     * Adds all elements from a list to the current list.
     *
     * @param elements the list of elements to add.
     * @return the current OptimizedList instance.
     */
    public OptimizedList<E> addElements(final List<E> elements) {
        addAll(elements);
        return this;
    }

    /**
     * Adds all elements from multiple collections to the current list.
     *
     * @param collections the collections of elements to add.
     * @return the current OptimizedList instance.
     */
    @SafeVarargs
    public final OptimizedList<E> addElements(Collection<E>... collections) {
        if (collections != null) {
            for (Collection<E> collection : collections) {
                addAll(collection);
            }
        }
        return this;
    }

    /**
     * Sorts the list using the specified comparator.
     *
     * @param comparator the comparator to determine the order of the list.
     * @return the current OptimizedList instance.
     */
    public OptimizedList<E> sorting(final Comparator<? super E> comparator) {
        sort(comparator);
        return this;
    }

    /**
     * Sorts the list in place using the specified comparator.
     *
     * @param comparator the comparator to determine the order of the list.
     */
    @Override
    public void sort(Comparator<? super E> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");

        long stamp = lock.writeLock();
        try {
            List<E> elements = new ArrayList<>(size);
            inOrderTraversal(root, elements::add);

            elements.sort(comparator);

            root = null;
            size = 0;
            for (E element : elements) {
                add(size, element);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Adds all specified elements to the list.
     *
     * @param elements the elements to add.
     * @return the current OptimizedList instance.
     */
    @SafeVarargs
    public final OptimizedList<E> addElements(E... elements) {
        addAllOf(elements);
        return this;
    }

    /**
     * Retrieves the element at the specified index.
     *
     * @param index the index of the element to retrieve.
     * @return the element at the specified index.
     */
    @Override
    public E get(int index) {
        long stamp = lock.tryOptimisticRead();
        CompactNode<E> node;
        try {
            checkIndex(index);
            node = getNode(root, index);
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    node = getNode(root, index);
                } finally {
                    lock.unlockRead(stamp);
                }
            }
        } finally {
            if (stamp != 0 && lock.isReadLocked()) {
                lock.unlockRead(stamp);
            }
        }
        return node.value;
    }

    /**
     * Returns the number of elements in the list.
     *
     * @return the size of the list.
     */
    @Override
    public int size() {
        long stamp = lock.tryOptimisticRead();
        int currentSize = size;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                currentSize = size;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return currentSize;
    }

    /**
     * Adds an element at the specified position in the list.
     *
     * @param index the index at which the element is to be inserted.
     * @param element the element to insert.
     */
    @Override
    public void add(int index, E element) {
        long stamp = lock.writeLock();
        try {
            if (index < 0 || index > size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
            root = insert(root, index, element);
            size++;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Removes the element at the specified position in the list.
     *
     * @param index the index of the element to remove.
     * @return the removed element.
     */
    @Override
    public E remove(int index) {
        long stamp = lock.writeLock();
        try {
            checkIndex(index);
            CompactNode<E> removed = new CompactNode<>(null);
            root = remove(root, index, removed);
            size--;
            return removed.value;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Adds all elements from a collection to the list.
     *
     * @param c the collection containing elements to add.
     * @return true if the list changed as a result of the operation.
     */
    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        Objects.requireNonNull(c);
        long stamp = lock.writeLock();
        try {
            for (E element : c) {
                add(size, element);
            }
            return true;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Adds all specified elements to the list.
     *
     * @param elements the elements to add.
     */
    @SafeVarargs
    public final void addAllOf(@NotNull E... elements) {
        Objects.requireNonNull(elements);
        long stamp = lock.writeLock();
        try {
            for (E element : elements) {
                add(size, element);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private CompactNode<E> getNode(CompactNode<E> node, int index) {
        int leftSize = size(node.children[0]);
        if (index < leftSize) {
            return getNode(node.children[0], index);
        } else if (index > leftSize) {
            return getNode(node.children[1], index - leftSize - 1);
        } else {
            return node;
        }
    }

    private CompactNode<E> insert(CompactNode<E> node, int index, E value) {
        if (node == null) {
            return new CompactNode<>(value);
        }
        int leftSize = size(node.children[0]);
        if (index <= leftSize) {
            node.children[0] = insert(node.children[0], index, value);
        } else {
            node.children[1] = insert(node.children[1], index - leftSize - 1, value);
        }
        return rebalance(node);
    }

    private CompactNode<E> remove(CompactNode<E> node, int index, CompactNode<E> removed) {
        int leftSize = size(node.children[0]);
        if (index < leftSize) {
            node.children[0] = remove(node.children[0], index, removed);
        } else if (index > leftSize) {
            node.children[1] = remove(node.children[1], index - leftSize - 1, removed);
        } else {
            removed.value = node.value;
            if (node.children[0] == null || node.children[1] == null) {
                return (node.children[0] != null) ? node.children[0] : node.children[1];
            } else {
                CompactNode<E> successor = findMin(node.children[1]);
                node.value = successor.value;
                node.children[1] = remove(node.children[1], 0, new CompactNode<>(null));
            }
        }
        return rebalance(node);
    }

    private CompactNode<E> findMin(CompactNode<E> node) {
        while (node.children[0] != null) {
            node = node.children[0];
        }
        return node;
    }

    private CompactNode<E> rebalance(CompactNode<E> node) {
        update(node);
        int balance = getBalance(node);
        if (balance > 1) {
            if (getBalance(node.children[0]) < 0) {
                node.children[0] = rotateLeft(node.children[0]);
            }
            return rotateRight(node);
        }
        if (balance < -1) {
            if (getBalance(node.children[1]) > 0) {
                node.children[1] = rotateRight(node.children[1]);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private CompactNode<E> rotateLeft(CompactNode<E> node) {
        CompactNode<E> newRoot = node.children[1];
        node.children[1] = newRoot.children[0];
        newRoot.children[0] = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    private CompactNode<E> rotateRight(CompactNode<E> node) {
        CompactNode<E> newRoot = node.children[0];
        node.children[0] = newRoot.children[1];
        newRoot.children[1] = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    private void update(CompactNode<E> node) {
        node.height = (byte) (1 + Math.max(height(node.children[0]), height(node.children[1])));
    }

    private int getBalance(CompactNode<E> node) {
        return height(node.children[0]) - height(node.children[1]);
    }

    private int height(CompactNode<E> node) {
        return (node == null) ? 0 : node.height;
    }

    private int size(CompactNode<E> node) {
        if (node == null) {
            return 0;
        }
        int leftSize = size(node.children[0]);
        int rightSize = size(node.children[1]);
        return 1 + leftSize + rightSize;
    }

    /**
     * Returns a sequential stream with the elements of this list.
     *
     * @return a stream with the elements of this list.
     */
    @Override
    public Stream<E> stream() {
        long stamp = lock.readLock();
        try {
            List<E> elements = new ArrayList<>(size);
            inOrderTraversal(root, elements::add);
            return elements.stream();
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Performs the given action for each element of the list.
     *
     * @param action the action to be performed for each element.
     */
    @Override
    @SuppressWarnings("SimplifyStreamApiCallChains")
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        stream().forEach(action);
    }

    /**
     * Maps the elements of the list using the specified function.
     *
     * @param mapper the function to apply to each element.
     * @return a new OptimizedList with the mapped elements.
     */
    public OptimizedList<E> map(Function<? super E, ? extends E> mapper) {
        Objects.requireNonNull(mapper);
        OptimizedList<E> mappedList = new OptimizedList<>();
        forEach(e -> mappedList.add(size(mappedList.root), mapper.apply(e)));
        return mappedList;
    }

    /**
     * Searches for an element in the list that matches the given predicate.
     * This method is thread-safe and leverages the stream() method of the list.
     *
     * @param predicate the condition to match elements against
     * @return an Optional containing the found element or empty if no element matches
     */
    public Optional<E> find(Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate, "Predicate must not be null");

        long stamp = lock.readLock();
        try {
            return stream().filter(predicate).findFirst();
        } finally {
            lock.unlockRead(stamp);
        }
    }


    /**
     * Filters the elements of the list using the specified predicate.
     *
     * @param predicate the predicate to apply to each element.
     * @return a new OptimizedList with the filtered elements.
     */
    public OptimizedList<E> filter(Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate);
        OptimizedList<E> filteredList = new OptimizedList<>();
        forEach(e -> {
            if (predicate.test(e)) {
                filteredList.add(size(filteredList.root), e);
            }
        });
        return filteredList;
    }

    /**
     * Reduces the elements of the list using the specified accumulator.
     *
     * @param accumulator the function to combine the elements.
     * @return an Optional containing the result of the reduction.
     */
    public Optional<E> reduce(BinaryOperator<E> accumulator) {
        Objects.requireNonNull(accumulator);
        return stream().reduce(accumulator);
    }

    private void inOrderTraversal(CompactNode<E> node, Consumer<? super E> action) {
        if (node == null) {
            return;
        }
        inOrderTraversal(node.children[0], action);
        action.accept(node.value);
        inOrderTraversal(node.children[1], action);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list.
     */
    @Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<>() {
            private final List<E> snapshot = new ArrayList<>(OptimizedList.this);
            private final Iterator<E> iter = snapshot.iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public E next() {
                return iter.next();
            }
        };
    }
}
