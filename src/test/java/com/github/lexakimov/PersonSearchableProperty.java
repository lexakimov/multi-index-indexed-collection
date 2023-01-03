package com.github.lexakimov;

import java.util.function.Function;

/**
 * @author akimov
 * created at: 03.01.2023 22:28
 */
enum PersonSearchableProperty implements Collection.SearchableProperty<Person> {
    FIRST_NAME(Person::firstName),
    LAST_NAME(Person::lastName),
    AGE(Person::age);

    private final Function<Person, Object> func;

    PersonSearchableProperty(Function<Person, Object> func) {
        this.func = func;
    }

    @Override
    public Function<Person, Object> getFunc() {
        return func;
    }

}
