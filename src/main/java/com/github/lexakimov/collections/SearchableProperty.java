package com.github.lexakimov.collections;

import java.util.function.Function;

/**
 * @author akimov
 * created at: 10.01.2023 19:38
 */
public interface SearchableProperty<E> {
    Function<E, Object> getFunc();
}
