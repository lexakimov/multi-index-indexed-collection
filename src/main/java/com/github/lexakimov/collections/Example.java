package com.github.lexakimov.collections;

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

        // then, define Enum class that implements MultiIndexIndexedCollection.IndexDefinition
        enum PersonIndexDefinition implements IndexDefinition<Person> {
            FIRST_NAME(Person::firstName),
            LAST_NAME(Person::lastName),
            AGE(Person::age);

            private final Function<Person, Object> func;

            PersonIndexDefinition(Function<Person, Object> func) {
                this.func = func;
            }

            @Override
            public Function<Person, Object> getFunc() {
                return func;
            }
        }

        // then, create an instance on MultiIndexIndexedCollection
        var collection = new MultiIndexIndexedCollection<Person>(PersonIndexDefinition.class);

        // put elements into collection
        collection.add(new Person("Caleb", "Dominguez", 1));
        collection.add(new Person("James", "Ryan", 2));
        collection.add(new Person("Jacob", "Smith", 3));
        collection.add(new Person("Caleb", "Hawkins", 4));


        // then, search by properties quick:

        var result1 = collection.searchByProperty(PersonIndexDefinition.FIRST_NAME, "Caleb");
        // returns first and forth element

        var result2 = collection.searchByProperty(PersonIndexDefinition.LAST_NAME, "Ryan");
        // returns second element
    }
}
