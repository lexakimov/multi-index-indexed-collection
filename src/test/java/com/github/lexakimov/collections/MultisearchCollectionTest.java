package com.github.lexakimov.collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.Arrays;
import java.util.function.Function;
import static com.github.lexakimov.collections.PersonSearchableProperty.FIRST_NAME;
import static com.github.lexakimov.collections.PersonSearchableProperty.LAST_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultisearchCollectionTest {

    @Nested
    @DisplayName("create collection")
    class Create {

        @Test
        void createWithNotEnum() {
            class FakePersonSearchableProperties implements MultisearchCollection.SearchableProperty<Person> {
                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new MultisearchCollection<>(FakePersonSearchableProperties.class));
        }

        @Test
        void createWithEmptyEnum() {
            enum FakePersonSearchableProperties implements MultisearchCollection.SearchableProperty<Person> {
                ;

                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new MultisearchCollection<>(FakePersonSearchableProperties.class));
        }

        @Test
        void createdSuccessful() {
            var uut = Assertions.assertDoesNotThrow(() -> new MultisearchCollection<>(PersonSearchableProperty.class));
            assertThat(uut.size(), equalTo(0));
            assertThat(uut.isEmpty(), equalTo(true));
        }
    }

    @Nested
    @DisplayName("add elements to collection")
    class Add {

        @Test
        void addElements() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));
            assertThat(uut.size(), equalTo(10));
        }

        @Test
        void addElementsWithIntersection() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElementsWithIntersection(uut));
            assertThat(uut.size(), equalTo(10));
        }

        @Test
        void clear() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));
            assertThat(uut.size(), equalTo(10));
            uut.clear();
            assertThat(uut.size(), equalTo(0));
        }

        @Test
        void containsElement() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));
            assertTrue(uut.contains(new Person("James", "Ryan", 2)));
            assertFalse(uut.contains(new Person("Tony", "Ryan", 2)));
        }

        @Test
        void containsElementByProperty() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));
            assertTrue(uut.contains(FIRST_NAME, "Jacob"));
            assertTrue(uut.contains(LAST_NAME, null));
            assertFalse(uut.contains(FIRST_NAME, "Lex"));
        }
    }

    @Nested
    @DisplayName("search elements in collection by properties")
    class Search {

        @Test
        void searchInEmptyCollection() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            var result = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, "test"));
            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        void searchInCollectionByNullValue() {
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));

            var resultByFirstName = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, null));
            assertThat(resultByFirstName, allOf(notNullValue(), empty()));

            var resultByLastName = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(LAST_NAME, null));
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
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));

            var resultByFirstName = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, firstName));
            var resultByLastName = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(LAST_NAME, lastName));
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
            var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultisearchCollectionTest.addElementsWithIntersection(uut));

            var agesList = Arrays.stream(ages.split(",")).map(Integer::valueOf).toList();

            var resultByFirstName = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, firstName));
            assertThat(resultByFirstName, allOf(notNullValue(), hasSize(size)));

            for (int i = 0; i < resultByFirstName.size(); i++) {
                Person person = resultByFirstName.get(i);
                assertThat(person.age(), equalTo(agesList.get(i)));
            }
        }

    }

    @Nested
    @DisplayName("tests of equals() method")
    class Equals {

        @Test
        void twoEmptyAreEqual() {
            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            var uut2 = new MultisearchCollection<>(PersonSearchableProperty.class);

            assertEquals(uut1, uut2);
        }

        @Test
        void twoEmptyAreDifferent() {
            enum AnotherPersonSearchableProperties implements MultisearchCollection.SearchableProperty<Person> {
                FIRST_NAME(Person::firstName),
                LAST_NAME(Person::lastName),
                AGE(Person::age);

                private final Function<Person, Object> func;

                AnotherPersonSearchableProperties(Function<Person, Object> func) {
                    this.func = func;
                }

                @Override
                public Function<Person, Object> getFunc() {
                    return func;
                }

            }

            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            var uut2 = new MultisearchCollection<>(AnotherPersonSearchableProperties.class);

            assertNotEquals(uut1, uut2);
        }

        @Test
        void twoCollectionsAreEqual() {
            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut1.addElement(new Person("test", "test", 20));
            var uut2 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut2.addElement(new Person("test", "test", 20));

            assertEquals(uut1, uut2);
        }

        @Test
        void twoCollectionsAreDifferent() {
            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut1.addElement(new Person("test", "test", 10));
            var uut2 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut2.addElement(new Person("test", "test", 20));

            assertNotEquals(uut1, uut2);
        }

    }

    @Nested
    @DisplayName("tests of hashCode() method")
    class HashCode {

        @Test
        void hashCodesOfTwoEmptyAreEqual() {
            var hashCode1 = new MultisearchCollection<>(PersonSearchableProperty.class).hashCode();
            var hashCode2 = new MultisearchCollection<>(PersonSearchableProperty.class).hashCode();

            assertEquals(hashCode1, hashCode2);
        }

        @Test
        void hashCodesOfTwoEmptyAreDifferent() {
            enum AnotherPersonSearchableProperties implements MultisearchCollection.SearchableProperty<Person> {
                FIRST_NAME(Person::firstName),
                LAST_NAME(Person::lastName),
                AGE(Person::age);

                private final Function<Person, Object> func;

                AnotherPersonSearchableProperties(Function<Person, Object> func) {
                    this.func = func;
                }

                @Override
                public Function<Person, Object> getFunc() {
                    return func;
                }

            }

            var hashCode1 = new MultisearchCollection<>(PersonSearchableProperty.class).hashCode();
            var hashCode2 = new MultisearchCollection<>(AnotherPersonSearchableProperties.class).hashCode();

            assertNotEquals(hashCode1, hashCode2);
        }

        @Test
        void hashCodesOfTwoCollectionsAreEqual() {
            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut1.addElement(new Person("test", "test", 20));
            var uut2 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut2.addElement(new Person("test", "test", 20));

            assertEquals(uut1.hashCode(), uut2.hashCode());
        }

        @Test
        void hashCodesOfTwoCollectionsAreDifferent() {
            var uut1 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut1.addElement(new Person("test", "test", 10));
            var uut2 = new MultisearchCollection<>(PersonSearchableProperty.class);
            uut2.addElement(new Person("test", "test", 20));

            assertNotEquals(uut1.hashCode(), uut2.hashCode());
        }

    }

    @Test
    void iteratorTest() {
        var uut = new MultisearchCollection<>(PersonSearchableProperty.class);

        var iterator = uut.iterator();
        assertThat(iterator, notNullValue());
        assertFalse(iterator.hasNext());

        assertDoesNotThrow(() -> MultisearchCollectionTest.addElements(uut));

        iterator = uut.iterator();
        assertThat(iterator, notNullValue());
        for (int i = 0; i < uut.size() - 1; i++) {
            assertTrue(iterator.hasNext());
            assertThat(iterator.next(), notNullValue());
        }

        assertThat(iterator.next(), notNullValue());
        assertFalse(iterator.hasNext());
    }

    private static void addElements(MultisearchCollection<Person> uut) {
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

    private static void addElementsWithIntersection(MultisearchCollection<Person> uut) {
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