package io.github.blyznytsiaorg.bibernate.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class PersistentList<T> implements List<T> {

  private final Supplier<List<?>> collectionSupplier;

  private List<T> internalList;

  public PersistentList(Supplier<List<?>> collectionSupplier) {
    this.collectionSupplier = collectionSupplier;
  }

  private List<T> getInternalList() {
    return Optional.ofNullable(internalList).orElseGet(() -> {
      internalList = (List<T>) collectionSupplier.get();
      return internalList;
    });
  }

  @Override
  public int size() {
    return getInternalList().size();
  }

  @Override
  public boolean isEmpty() {
    return getInternalList().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return getInternalList().contains(o);
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public Iterator<T> iterator() {
    return getInternalList().iterator();
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public Object [] toArray() {
    return getInternalList().toArray();
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public <T1> T1 [] toArray(@org.jetbrains.annotations.NotNull T1[] a) {
    return getInternalList().toArray(a);
  }

  @Override
  public boolean add(T t) {
    return getInternalList().add(t);
  }

  @Override
  public boolean remove(Object o) {
    return getInternalList().remove(o);
  }

  @Override
  public boolean containsAll(@org.jetbrains.annotations.NotNull Collection<?> c) {
    return getInternalList().containsAll(c);
  }

  @Override
  public boolean addAll(@org.jetbrains.annotations.NotNull Collection<? extends T> c) {
    return getInternalList().addAll(c);
  }

  @Override
  public boolean addAll(int index, @org.jetbrains.annotations.NotNull Collection<? extends T> c) {
    return getInternalList().addAll(index, c);
  }

  @Override
  public boolean removeAll(@org.jetbrains.annotations.NotNull Collection<?> c) {
    return getInternalList().removeAll(c);
  }

  @Override
  public boolean retainAll(@org.jetbrains.annotations.NotNull Collection<?> c) {
    return getInternalList().retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<T> operator) {
    getInternalList().replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super T> c) {
    getInternalList().sort(c);
  }

  @Override
  public void clear() {
    getInternalList().clear();
  }

  @Override
  public boolean equals(Object o) {
    return getInternalList().equals(o);
  }

  @Override
  public int hashCode() {
    return getInternalList().hashCode();
  }

  @Override
  public T get(int index) {
    return getInternalList().get(index);
  }

  @Override
  public T set(int index, T element) {
    return getInternalList().set(index, element);
  }

  @Override
  public void add(int index, T element) {
    getInternalList().add(index, element);
  }

  @Override
  public T remove(int index) {
    return getInternalList().remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return getInternalList().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return getInternalList().lastIndexOf(o);
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public ListIterator<T> listIterator() {
    return getInternalList().listIterator();
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public ListIterator<T> listIterator(int index) {
    return getInternalList().listIterator(index);
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return getInternalList().subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<T> spliterator() {
    return getInternalList().spliterator();
  }

  public static <E> List<E> of() {
    return List.of();
  }

  public static <E> List<E> of(E e1) {
    return List.of(e1);
  }

  public static <E> List<E> of(E e1, E e2) {
    return List.of(e1, e2);
  }

  public static <E> List<E> of(E e1, E e2, E e3) {
    return List.of(e1, e2, e3);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4) {
    return List.of(e1, e2, e3, e4);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
    return List.of(e1, e2, e3, e4, e5);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
    return List.of(e1, e2, e3, e4, e5, e6);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
    return List.of(e1, e2, e3, e4, e5, e6, e7);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
    return List.of(e1, e2, e3, e4, e5, e6, e7, e8);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
    return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9);
  }

  public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
    return List.of(e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);
  }

  @SafeVarargs
  public static <E> List<E> of(E... elements) {
    return List.of(elements);
  }

  public static <E> List<E> copyOf(Collection<? extends E> coll) {
    return List.copyOf(coll);
  }

  @Override
  public <T1> T1[] toArray(IntFunction<T1[]> generator) {
    return getInternalList().toArray(generator);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return getInternalList().removeIf(filter);
  }

  @Override
  public Stream<T> stream() {
    return getInternalList().stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return getInternalList().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    getInternalList().forEach(action);
  }
}
