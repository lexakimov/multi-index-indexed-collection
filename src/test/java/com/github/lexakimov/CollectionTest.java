package com.github.lexakimov;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Arrays;
import java.util.function.Function;
import static com.github.lexakimov.CollectionTest.PersonSearchableProperties.FIRST_NAME;
import static com.github.lexakimov.CollectionTest.PersonSearchableProperties.LAST_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionTest {

    @Nested
    @DisplayName("create collection")
    class Create {

        @Test
        void createWithNotEnum() {
            class FakePersonSearchableProperties implements Collection.SearchableProperty<Person> {

                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new Collection<>(FakePersonSearchableProperties.class));
        }

        @Test
        void createWithEmptyEnum() {
            enum FakePersonSearchableProperties implements Collection.SearchableProperty<Person> {
                ;

                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new Collection<>(FakePersonSearchableProperties.class));
        }

        @Test
        void createdSuccessful() {
            var uut = assertDoesNotThrow(() -> new Collection<>(PersonSearchableProperties.class));
            assertThat(uut.size(), equalTo(0));
        }
    }


    @Nested
    @DisplayName("add elements to collection")
    class Add {

        @Test
        void addElements() {
            var uut = new Collection<>(PersonSearchableProperties.class);
            assertDoesNotThrow(() -> CollectionTest.addElements(uut));
            assertThat(uut.size(), equalTo(10));
        }

        @Test
        void addElementsWithIntersection() {
            var uut = new Collection<>(PersonSearchableProperties.class);
            assertDoesNotThrow(() -> CollectionTest.addElementsWithIntersection(uut));
            assertThat(uut.size(), equalTo(10));
        }
    }

    @Nested
    @DisplayName("search elements in collection by properties")
    class Search {

        @Test
        void searchInEmptyCollection() {
            var uut = new Collection<>(PersonSearchableProperties.class);
            var result = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, "test"));
            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        void searchInCollectionByNullValue() {
            var uut = new Collection<>(PersonSearchableProperties.class);
            assertDoesNotThrow(() -> CollectionTest.addElements(uut));

            var resultByFirstName = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, null));
            assertThat(resultByFirstName, allOf(notNullValue(), empty()));

            var resultByLastName = assertDoesNotThrow(() -> uut.searchByProperty(LAST_NAME, null));
            assertThat(resultByLastName, allOf(notNullValue(), hasSize(1)));
            assertThat(resultByLastName.get(0).age(), equalTo(6));
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                Caleb,     Dominguez, 1
                James,     Ryan, 2
                Jacob,     Smith, 3
                Kelsey,    Hawkins, 4
                Karen,     Mcguire, 5
                Colleen,   null, 6
                Crystal,   Carey, 7
                John,      King, 8
                Stephanie, Chen, 9
                Justin,    Fuller, 10""", nullValues = "null")
        void searchInCollection_noIntersections(String firstName, String lastName, int age) {
            var uut = new Collection<>(PersonSearchableProperties.class);
            assertDoesNotThrow(() -> CollectionTest.addElements(uut));

            var resultByFirstName = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, firstName));
            var resultByLastName = assertDoesNotThrow(() -> uut.searchByProperty(LAST_NAME, lastName));
            assertThat(resultByFirstName, allOf(notNullValue(), hasSize(1)));
            assertThat(resultByLastName, allOf(notNullValue(), hasSize(1)));

            assertThat(resultByFirstName.get(0).age(), equalTo(age));
            assertThat(resultByLastName.get(0).age(), equalTo(age));
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                Caleb, 2, '1,5'
                Jacob, 3, '3,9,10'""")
        void searchInCollection_withIntersections(String firstName, int size, String ages) {
            var uut = new Collection<>(PersonSearchableProperties.class);
            assertDoesNotThrow(() -> CollectionTest.addElementsWithIntersection(uut));

            var agesList = Arrays.stream(ages.split(",")).map(Integer::valueOf).toList();

            var resultByFirstName = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, firstName));
            assertThat(resultByFirstName, allOf(notNullValue(), hasSize(size)));

            for (int i = 0; i < resultByFirstName.size(); i++) {
                Person person = resultByFirstName.get(i);
                assertThat(person.age(), equalTo(agesList.get(i)));
            }
        }

    }

    record Person(String firstName, String lastName, int age) {

    }

    enum PersonSearchableProperties implements Collection.SearchableProperty<Person> {
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

    private static void addElements(Collection<Person> uut) {
        uut.addElement(new Person("Caleb", "Dominguez", 1));
        uut.addElement(new Person("James", "Ryan", 2));
        uut.addElement(new Person("Jacob", "Smith", 3));
        uut.addElement(new Person("Kelsey", "Hawkins", 4));
        uut.addElement(new Person("Karen", "Mcguire", 5));
        uut.addElement(new Person("Colleen", null, 6));
        uut.addElement(new Person("Crystal", "Carey", 7));
        uut.addElement(new Person("John", "King", 8));
        uut.addElement(new Person("Stephanie", "Chen", 9));
        uut.addElement(new Person("Justin", "Fuller", 10));
    }

    private static void addElementsWithIntersection(Collection<Person> uut) {
        uut.addElement(new Person("Caleb", "Dominguez", 1));
        uut.addElement(new Person("James", "Ryan", 2));
        uut.addElement(new Person("Jacob", "Smith", 3));
        uut.addElement(new Person("Kelsey", "Hawkins", 4));
        uut.addElement(new Person("Caleb", "Mcguire", 5));
        uut.addElement(new Person("Colleen", "Dominguez", 6));
        uut.addElement(new Person("Crystal", "Carey", 7));
        uut.addElement(new Person("John", "King", 8));
        uut.addElement(new Person("Jacob", "Dominguez", 9));
        uut.addElement(new Person("Jacob", "Fuller", 10));
    }

}