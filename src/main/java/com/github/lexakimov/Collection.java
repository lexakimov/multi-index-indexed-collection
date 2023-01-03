package com.github.lexakimov;

import org.apache.commons.collections4.ListValuedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import static org.apache.commons.collections4.MultiMapUtils.newListValuedHashMap;

/**
 * @author akimov
 * created at: 03.01.2023 18:00
 */
public class Collection<E> {

    private final List<E> elements;

    private final List<SearchableProperty<E>> propertyEnumConstants;

    private final Map<SearchableProperty<E>, Function<E, ?>> getValueFunctions;

    private final Map<SearchableProperty<E>, ListValuedMap<Object, Integer>> indicesMapsByProperty;

    public Collection(Class<? extends SearchableProperty<E>> propertyEnumClass) {
        Objects.requireNonNull(propertyEnumClass);
        var enumConstants = propertyEnumClass.getEnumConstants();
        if (!propertyEnumClass.isEnum() || enumConstants.length == 0) {
            throw new IllegalArgumentException("class %s mus be enum with properties".formatted(propertyEnumClass));
        }

        this.elements = new ArrayList<>();
        this.getValueFunctions = new HashMap<>();
        this.indicesMapsByProperty = new HashMap<>();
        this.propertyEnumConstants = new LinkedList<>();

        for (SearchableProperty<E> enumConstant : enumConstants) {
            this.getValueFunctions.put(enumConstant, enumConstant.getFunc());
            this.propertyEnumConstants.add(enumConstant);
        }
    }

    public void addElement(E element) {
        var elementIndex = elements.size();
        updateIndices(element, elementIndex);
        elements.add(element);
    }

    private void updateIndices(E element, int elementIndex) {
        for (SearchableProperty<E> propertyEnumConstant : propertyEnumConstants) {
            var value = getValueFunctions.get(propertyEnumConstant).apply(element);
            var indexMap = indicesMapsByProperty.computeIfAbsent(propertyEnumConstant, k -> newListValuedHashMap());
            indexMap.put(value, elementIndex);
        }
    }

    public List<E> searchByProperty(SearchableProperty<E> property, Object value) {

        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return Collections.emptyList();
        }

        var elementsIndices = indexMap.get(value);
        var result = new ArrayList<E>(elementsIndices.size());

        elementsIndices.forEach(i -> result.add(elements.get(i)));

        return result;
    }

    public interface SearchableProperty<E> {
        Function<E, Object> getFunc();
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void clear() {
        indicesMapsByProperty.clear();
        elements.clear();
    }

    public boolean contains(E o) {
        return elements.contains(o);
    }

    public boolean contains(SearchableProperty<E> property, Object value) {
        var indexMap = indicesMapsByProperty.getOrDefault(property, null);
        if (indexMap == null) {
            return false;
        }
        var elementsIndices = indexMap.get(value);
        return !elementsIndices.isEmpty();
    }

    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Collection<?> that = (Collection<?>) o;

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
