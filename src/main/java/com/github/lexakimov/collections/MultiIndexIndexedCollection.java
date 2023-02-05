package com.github.lexakimov.collections;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author akimov
 * created at: 03.01.2023 18:00
 */
public class MultiIndexIndexedCollection<E> {

    private final List<E> elements = new ArrayList<>();

    private final List<IndexDefinition<E>> propertyEnumConstants = new LinkedList<>();

    private final Map<IndexDefinition<E>, Function<E, ?>> getValueFunctions = new HashMap<>();

    /**
     * MAP[PROPERTY: MAP[PROPERTY_VALUE: LIST[indices of elements...]]]
     */
    private final Map<IndexDefinition<E>, Map<Object, IntArrayList>> indicesMapsByProperty = new HashMap<>();

    public MultiIndexIndexedCollection(Class<? extends IndexDefinition<E>> searchablePropertyEnumClass) {
        Objects.requireNonNull(searchablePropertyEnumClass);
        if (!searchablePropertyEnumClass.isEnum()) {
            var message = "%s must be enum that extends %s".formatted(searchablePropertyEnumClass,
                    IndexDefinition.class.getName());
            throw new IllegalArgumentException(message);
        }

        var enumConstants = searchablePropertyEnumClass.getEnumConstants();
        if (enumConstants.length == 0) {
            throw new IllegalArgumentException(
                    "enum %s must contains at least 1 enumeration value".formatted(searchablePropertyEnumClass));
        }

        for (IndexDefinition<E> enumConstant : enumConstants) {
            this.getValueFunctions.put(enumConstant, enumConstant.getFunc());
            this.propertyEnumConstants.add(enumConstant);
        }
    }

    public boolean add(E element) {
        Objects.requireNonNull(element);
        var elementIndex = elements.size();
        updateIndices(element, elementIndex);
        return elements.add(element);
    }

    private void updateIndices(E element, int elementIndex) {
        for (IndexDefinition<E> propertyEnumConstant : propertyEnumConstants) {
            var value = getValueFunctions.get(propertyEnumConstant).apply(element);
            var indexMap = indicesMapsByProperty.computeIfAbsent(propertyEnumConstant, k -> new HashMap<>());
            indexMap.computeIfAbsent(value, o -> new IntArrayList()).add(elementIndex);
        }
    }

    public boolean contains(E o) {
        return elements.contains(o);
    }

    public boolean contains(IndexDefinition<E> property, Object value) {
        Objects.requireNonNull(property);
        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return false;
        }
        var elementsIndices = indexMap.get(value);
        return elementsIndices != null && !elementsIndices.isEmpty();
    }

    public List<E> searchByProperty(IndexDefinition<E> property, Object value) {
        Objects.requireNonNull(property);
        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return Collections.emptyList();
        }

        var elementsIndices = indexMap.get(value);
        if (elementsIndices == null || elementsIndices.isEmpty()) {
            return Collections.emptyList();
        }

        var result = new ArrayList<E>(elementsIndices.size());

        elementsIndices.forEach(i -> result.add(elements.get(i)));

        return result;
    }

//        TODO conjunction search
//        TODO передавать comparator

    public Iterator<E> iterator() {
        return elements.iterator();
    }

    public Iterator<E> iterator(IndexDefinition<E> property) {
        Objects.requireNonNull(property);
        throw new UnsupportedOperationException("method does not implemented yet");
    }

    public Iterator<E> stream(IndexDefinition<E> property) {
        Objects.requireNonNull(property);
        throw new UnsupportedOperationException("method does not implemented yet");
    }

    public List<E> list() {
        return Collections.unmodifiableList(elements);
    }

    public List<E> list(IndexDefinition<E> property) {
        Objects.requireNonNull(property);
        throw new UnsupportedOperationException("method does not implemented yet");
    }

    /**
     * TODO подумать над алгоритмом
     */
    public boolean remove(IndexDefinition<E> property, Object value) {
        Objects.requireNonNull(property);
        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return false;
        }

        var elementsIndices = indexMap.get(value);
        if (elementsIndices.isEmpty()) {
            return false;
        }

        var result = new ArrayList<E>(elementsIndices.size());

        elementsIndices.forEach(i -> result.add(elements.get(i)));

        return true;
    }

    public void clear() {
        indicesMapsByProperty.clear();
        elements.clear();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public int size() {
        return elements.size();
    }

    public int size(IndexDefinition<E> property, Object value) {
        Objects.requireNonNull(property);
        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return 0;
        }

        var elementsIndices = indexMap.get(value);
        return elementsIndices.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiIndexIndexedCollection<?> that = (MultiIndexIndexedCollection<?>) o;

        if (!elements.equals(that.elements)) return false;
        if (!propertyEnumConstants.equals(that.propertyEnumConstants)) return false;
        if (!getValueFunctions.equals(that.getValueFunctions)) return false;
        return indicesMapsByProperty.equals(that.indicesMapsByProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, propertyEnumConstants, getValueFunctions, indicesMapsByProperty);
    }
}
