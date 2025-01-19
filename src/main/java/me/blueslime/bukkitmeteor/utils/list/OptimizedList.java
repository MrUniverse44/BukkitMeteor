package me.blueslime.bukkitmeteor.utils.list;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * OptimizedList: A self-balancing tree-based list implementation.
 * Optimized for memory usage, performance, and concurrency.
 */
@SuppressWarnings("unused")
public class OptimizedList<E> extends AbstractList<E> {

    // Root node of the tree
    private volatile CompactNode<E> root;

    // Size of the tree (number of elements)
    private volatile int size = 0;

    // Lock for thread safety
    private final StampedLock lock = new StampedLock();

    public OptimizedList() {

    }

    public OptimizedList(final List<E> list) {
        addAll(list);
    }

    public static <E> OptimizedList<E> create() {
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

    public OptimizedList<E> addElement(final E element) {
        add(element);
        return this;
    }

    public OptimizedList<E> addElements(final List<E> elements) {
        addAll(elements);
        return this;
    }

    @SafeVarargs
    public final OptimizedList<E> addElements(E... elements) {
        if (elements == null) {
            return this;
        }
        addAll(Arrays.asList(elements));
        return this;
    }

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

    @Override
    public boolean addAll(Collection<? extends E> c) {
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
     * Ensures the index is within bounds
     */
    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * Retrieves the node at a specific index
     */
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

    /**
     * Inserts an element at a specific index
     */
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

    /**
     * Removes the element at a specific index
     */
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

    /**
     * Finds the minimum node in a subtree
     */
    private CompactNode<E> findMin(CompactNode<E> node) {
        while (node.children[0] != null) {
            node = node.children[0];
        }
        return node;
    }

    /**
     * Balances the tree at the given node
     */
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

    /**
     * Performs a left rotation
     */
    private CompactNode<E> rotateLeft(CompactNode<E> node) {
        CompactNode<E> newRoot = node.children[1];
        node.children[1] = newRoot.children[0];
        newRoot.children[0] = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    /**
     * Performs a right rotation
     */
    private CompactNode<E> rotateRight(CompactNode<E> node) {
        CompactNode<E> newRoot = node.children[0];
        node.children[0] = newRoot.children[1];
        newRoot.children[1] = node;
        update(node);
        update(newRoot);
        return newRoot;
    }

    /**
     * Updates the height of a node
     */
    private void update(CompactNode<E> node) {
        node.height = (byte) (1 + Math.max(height(node.children[0]), height(node.children[1])));
    }

    /**
     * Calculates the balance factor of a node
     */
    private int getBalance(CompactNode<E> node) {
        return height(node.children[0]) - height(node.children[1]);
    }

    /**
     * Returns the height of a node
     */
    private int height(CompactNode<E> node) {
        return (node == null) ? 0 : node.height;
    }

    /**
     * Returns the size of a node
     */
    private int size(CompactNode<E> node) {
        if (node == null) {
            return 0;
        }
        int leftSize = size(node.children[0]);
        int rightSize = size(node.children[1]);
        return 1 + leftSize + rightSize;
    }

    /**
     * Streams support for functional operations
     */
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

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        stream().forEach(action);
    }

    public OptimizedList<E> map(Function<? super E, ? extends E> mapper) {
        Objects.requireNonNull(mapper);
        OptimizedList<E> mappedList = new OptimizedList<>();
        forEach(e -> mappedList.add(size(mappedList.root), mapper.apply(e)));
        return mappedList;
    }

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
