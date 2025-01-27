package me.blueslime.bukkitmeteor.utils.list;

import me.blueslime.bukkitmeteor.implementation.Implementer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes"})
public class OptimizedList<E> extends AbstractList<E> implements Implementer {
    private final AtomicReference<Node<E>[]> elementsRef;

    public OptimizedList() {
        this.elementsRef = new AtomicReference<>(new Node[0]);
    }

    public OptimizedList(Collection<E> collection) {
        this.elementsRef = new AtomicReference<>(new Node[0]);
        addAll(collection);
    }

    @SafeVarargs
    public OptimizedList(E... elements) {
        this.elementsRef = new AtomicReference<>(new Node[0]);
        addAll(Arrays.asList(elements));

    }

    public static <E> OptimizedList<E> create() {
        return new OptimizedList<>();
    }

    @SuppressWarnings("unused")
    public static <E> OptimizedList<E> create(Class<? extends E> type) {
        return new OptimizedList<>();
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

    @Override
    public E get(int index) {
        Node<E>[] currentArray = elementsRef.get();
        if (index >= currentArray.length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + currentArray.length);
        }
        return currentArray[index].getElement();
    }

    @Override
    public int size() {
        return elementsRef.get().length;
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Node<E>[] currentArray = elementsRef.get();
        for (Node<E> node : currentArray) {
            action.accept(node.getElement());
        }
    }

    private void filterElements(Node<E>[] nodes, OptimizedList<E> filtered, Predicate<E> predicate) {
        for (Node<E> node : nodes) {
            if (node.getElement() == null) {
                continue;
            }
            if (!predicate.test(node.getElement())) {
                continue;
            }
            filtered.add(
                node.getElement()
            );
        }
    }

    public OptimizedList<E> filter(Predicate<E> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");

        OptimizedList<E> filtered = new OptimizedList<>();
        Node<E>[] currentArray = elementsRef.get();

        filterElements(currentArray, filtered, predicate);
        return filtered;
    }

    public Optional<E> findFirst() {
        if (isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getFirst());
    }

    public Optional<E> find(Predicate<E> predicate) {
        Objects.requireNonNull(predicate, "Predicate cannot be null");
        Node<E>[] elements = elementsRef.get();

        return findElement(elements, predicate);
    }

    private Optional<E> findElement(Node<E>[] nodes, Predicate<E> predicate) {
        for (Node<E> node : nodes) {
            if (!predicate.test(node.getElement())) {
                continue;
            }
            return Optional.of(node.getElement());
        }
        return Optional.empty();
    }

    public <R> OptimizedList<R> map(Function<E, R> mapper) {
        Objects.requireNonNull(mapper, "Function cannot be null");

        Node<E>[] elements = elementsRef.get();

        OptimizedList<R> mapped = new OptimizedList<>();
        mapElements(elements, mapped, mapper);
        return mapped;
    }

    private <R> void mapElements(Node<E>[] nodes, OptimizedList<R> mapped, Function<E, R> mapper) {
        for (Node<E> node : nodes) {
            if (node.getElement() == null) {
                continue;
            }
            mapped.add(
                mapper.apply(node.getElement())
            );
        }
    }

    @Override
    public boolean contains(Object o) {
        Node<E>[] currentArray = elementsRef.get();
        for (Node<E> node : currentArray) {
            if (node.getElement().equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean add(E element) {
        add(element, 0);
        return true;
    }

    public void add(E element, int priority) {
        Node<E>[] currentArray;
        Node<E>[] newArray;

        do {
            currentArray = elementsRef.get();
            newArray = Arrays.copyOf(currentArray, currentArray.length + 1);
            newArray[currentArray.length] = new Node<>(element, priority);
        } while (!elementsRef.compareAndSet(currentArray, newArray));
    }

    @Override
    public boolean remove(Object o) {
        Node<E>[] currentArray;
        Node<E>[] newArray;

        do {
            currentArray = elementsRef.get();
            int index = indexOf(currentArray, o);
            if (index == -1) {
                return false;
            }
            newArray = new Node[currentArray.length - 1];
            System.arraycopy(currentArray, 0, newArray, 0, index);
            System.arraycopy(currentArray, index + 1, newArray, index, currentArray.length - index - 1);
        } while (!elementsRef.compareAndSet(currentArray, newArray));

        return true;
    }

    public OptimizedList<E> shuffle() {
        Node<E>[] currentArray;
        Node<E>[] shuffledArray;
        Random random = fetch(Random.class);

        do {
            currentArray = elementsRef.get();
            shuffledArray = Arrays.copyOf(currentArray, currentArray.length);

            boolean validShuffle;
            do {
                validShuffle = true;

                for (int i = 0; i < shuffledArray.length; i++) {
                    int randomIndex = random.nextInt(shuffledArray.length);
                    Node<E> temp = shuffledArray[i];
                    shuffledArray[i] = shuffledArray[randomIndex];
                    shuffledArray[randomIndex] = temp;
                }

                for (int i = 0; i < shuffledArray.length; i++) {
                    if (shuffledArray[i] == currentArray[i]) {
                        validShuffle = false;
                        break;
                    }
                }
            } while (!validShuffle);

        } while (!elementsRef.compareAndSet(currentArray, shuffledArray));
        return this;
    }

    public OptimizedList<E> sorting(Comparator<? super E> comparator) {
        Node<E>[] currentArray;
        Node<E>[] sortedArray;

        do {
            currentArray = elementsRef.get();
            sortedArray = Arrays.copyOf(currentArray, currentArray.length);
            Arrays.sort(sortedArray, (a, b) -> comparator.compare(a.getElement(), b.getElement()));
        } while (!elementsRef.compareAndSet(currentArray, sortedArray));
        return this;
    }

    public void sortByPriority() {
        Node<E>[] currentArray;
        Node<E>[] sortedArray;

        do {
            currentArray = elementsRef.get();
            sortedArray = Arrays.copyOf(currentArray, currentArray.length);
            Arrays.sort(sortedArray, Comparator.comparingInt(Node::getPriority));
        } while (!elementsRef.compareAndSet(currentArray, sortedArray));
    }

    @Override
    public Object @NotNull [] toArray() {
        Node<E>[] currentArray = elementsRef.get();
        Object[] result = new Object[currentArray.length];
        for (int i = 0; i < currentArray.length; i++) {
            result[i] = currentArray[i].getElement();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T @NotNull [] toArray(T[] a) {
        Node<E>[] currentArray = elementsRef.get();
        if (a.length < currentArray.length) {
            return (T[]) Arrays.copyOf(toArray(), currentArray.length, a.getClass());
        }
        System.arraycopy(toArray(), 0, a, 0, currentArray.length);
        if (a.length > currentArray.length) {
            a[currentArray.length] = null;
        }
        return a;
    }

    private int indexOf(Node<E>[] array, Object element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].getElement().equals(element)) {
                return i;
            }
        }
        return -1;
    }

    private static class Node<E> {
        private final E element;
        private final int priority;

        public Node(E element, int priority) {
            this.element = element;
            this.priority = priority;
        }

        public E getElement() {
            return element;
        }

        public int getPriority() {
            return priority;
        }
    }
}