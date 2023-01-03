package com.github.lexakimov;

import java.util.function.Function;

/**
 * @author akimov
 * created at: 03.01.2023 20:37
 */
public class Example {

    public static void main(String[] args) {

        // first, define a dto class
        record Person(String firstName, String lastName, int age) {

        }

        // then, define Enum class that implements MultisearchCollection.SearchableProperty with properties
        enum PersonSearchableProperties implements MultisearchCollection.SearchableProperty<Person> {
            FIRST_NAME(Person::firstName),
            LAST_NAME(Person::lastName),
            AGE(Person::age);

            private final Function<Person, Object> func;

            PersonSearchableProperties(Function<Person, Object> func) {
                this.func = func;
            }

            @Override
            public Function<Person, Object> getFunc() {
                return func;
            }
        }

        // then, create an instance on MultisearchCollection
        var collection = new MultisearchCollection<Person>(PersonSearchableProperties.class);

        // put elements into collection
        collection.addElement(new Person("Caleb", "Dominguez", 1));
        collection.addElement(new Person("James", "Ryan", 2));
        collection.addElement(new Person("Jacob", "Smith", 3));
        collection.addElement(new Person("Caleb", "Hawkins", 4));


        // then, search by properties quick:

        var result1 = collection.searchByProperty(PersonSearchableProperties.FIRST_NAME, "Caleb");
        // returns first and forth element

        var result2 = collection.searchByProperty(PersonSearchableProperties.LAST_NAME, "Ryan");
        // returns second element
    }
}
