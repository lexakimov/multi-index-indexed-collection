package com.github.lexakimov;

import org.apache.commons.collections4.ListValuedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

    private final List<SearchableProperty<E>> propertyEnumConstants;

    private final List<E> elements;

    private final Map<SearchableProperty<E>, Function<E, ?>> getterFunctions;

    private final Map<SearchableProperty<E>, ListValuedMap<Object, Integer>> indicesMapsByProperty;

    public Collection(Class<? extends SearchableProperty<E>> propertyEnumClass) {
        Objects.requireNonNull(propertyEnumClass);
        elements = new ArrayList<>();
        getterFunctions = new HashMap<>();
        indicesMapsByProperty = new HashMap<>();
        this.propertyEnumConstants = new LinkedList<>();

        var enumConstants = propertyEnumClass.getEnumConstants();
        if (!propertyEnumClass.isEnum() || enumConstants.length == 0) {
            throw new IllegalArgumentException("class %s mus be enum with properties".formatted(propertyEnumClass));
        }

        for (SearchableProperty<E> enumConstant : enumConstants) {
            getterFunctions.put(enumConstant, enumConstant.getFunc());
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
            var value = getterFunctions.get(propertyEnumConstant).apply(element);
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


    public int size() {
        return elements.size();
    }

    public interface SearchableProperty<E> {
        Function<E, Object> getFunc();
    }
}
