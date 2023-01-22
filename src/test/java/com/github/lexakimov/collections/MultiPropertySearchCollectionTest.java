package com.github.lexakimov.collections;

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

class MultiPropertySearchCollectionTest {

    @Nested
    @DisplayName("create collection")
    class Create {

        @Test
        void createWithNull() {
            assertThrows(NullPointerException.class, () -> new MultiPropertySearchCollection<>(null));
        }

        @Test
        void createWithNotEnum() {
            class BadEnum implements SearchableProperty<Person> {
                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new MultiPropertySearchCollection<>(BadEnum.class));
        }

        @Test
        void createWithEmptyEnum() {
            enum BadEnum implements SearchableProperty<Person> {
                ;

                @Override
                public Function<Person, Object> getFunc() {
                    return null;
                }
            }

            assertThrows(IllegalArgumentException.class, () -> new MultiPropertySearchCollection<>(BadEnum.class));
        }

        @Test
        void createdSuccessful() {
            var uut = assertDoesNotThrow(() -> new MultiPropertySearchCollection<>(PersonSearchableProperty.class));
            assertThat(uut.size(), equalTo(0));
            assertThat(uut.isEmpty(), equalTo(true));
        }
    }

    @Nested
    @DisplayName("add elements")
    class Add {

        @Test
        void addElements() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
            assertThat(uut.size(), equalTo(10));
        }

        @Test
        void addElementsWithIntersection() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElementsWithIntersection(uut));
            assertThat(uut.size(), equalTo(10));
        }

        @Test
        void addNullElement() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.add(null));
        }
    }

    @Nested
    @DisplayName("get size")
    class Size {

        @Test
        void sizeByProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElementsWithIntersection(uut));

            assertThat(uut.size(FIRST_NAME, "Caleb"), equalTo(2));
            assertThat(uut.size(FIRST_NAME, "Jacob"), equalTo(3));
        }

        @Test
        void sizeByPropertyNullValue() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElementsWithIntersection(uut));
            uut.add(new Person(null, "Hawkins", 4));
            uut.add(new Person(null, "Mcguire", 5));

            assertThat(uut.size(FIRST_NAME, null), equalTo(2));
        }
    }

    @Test
    void clear() {
        var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
        assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
        assertThat(uut.size(), equalTo(10));
        uut.clear();
        assertThat(uut.size(), equalTo(0));
    }

    @Nested
    @DisplayName("check that contains element")
    class Contains {

        @Test
        void containsElement() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
            assertTrue(uut.contains(new Person("James", "Ryan", 2)));
            assertFalse(uut.contains(new Person("Tony", "Ryan", 2)));
        }

        @Test
        void containsElementByNullProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.contains(null, "Jacob"));
        }

        @Test
        void containsElementByProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
            assertTrue(uut.contains(FIRST_NAME, "Jacob"));
            assertTrue(uut.contains(LAST_NAME, null));
            assertFalse(uut.contains(FIRST_NAME, "Lex"));
        }
    }

    @Nested
    @DisplayName("remove element(s)")
    class Remove {

        @Test
        void removeByProperty_null() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.remove(null, "Jacob"));
        }

        @Test
        void removeByProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
            assertThat(uut.size(), equalTo(10));
            assertTrue(uut.contains(FIRST_NAME, "Jacob"));

            assertTrue(uut.remove(FIRST_NAME, "Jacob"));
            assertThat(uut.size(), equalTo(9));
            assertFalse(uut.contains(FIRST_NAME, "Jacob"));

            assertFalse(uut.remove(FIRST_NAME, "Jacob"));
            assertThat(uut.size(), equalTo(9));
        }
    }

    @Nested
    @DisplayName("search elements by properties")
    class Search {

        @Test
        void searchByNullProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.searchByProperty(null, "test"));
        }

        @Test
        void searchInEmptyCollection() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            var result = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, "test"));
            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        void searchByNullValue() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));

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
        void searchWhenNoIntersections(String firstName, String lastName, int age) {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));

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
        void searchWhenHasIntersections(String firstName, int size, String ages) {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElementsWithIntersection(uut));

            var agesList = Arrays.stream(ages.split(",")).map(Integer::valueOf).toList();

            var resultByFirstName = assertDoesNotThrow(() -> uut.searchByProperty(FIRST_NAME, firstName));
            assertThat(resultByFirstName, allOf(notNullValue(), hasSize(size)));

            for (int i = 0; i < resultByFirstName.size(); i++) {
                Person person = resultByFirstName.get(i);
                assertThat(person.age(), equalTo(agesList.get(i)));
            }
        }

    }

    @Nested
    @DisplayName("iterate over elements")
    class Iteration {

        @Test
        void iteratorTest() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);

            var iterator = uut.iterator();
            assertThat(iterator, notNullValue());
            assertFalse(iterator.hasNext());

            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));

            iterator = uut.iterator();
            assertThat(iterator, notNullValue());
            for (int i = 0; i < uut.size() - 1; i++) {
                assertTrue(iterator.hasNext());
                assertThat(iterator.next(), notNullValue());
            }

            assertThat(iterator.next(), notNullValue());
            assertFalse(iterator.hasNext());
        }

        @Test
        void iteratorByNullProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.iterator(null));
        }

        @Test
        void listByNullProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.list(null));
        }

        @Test
        void streamByNullProperty() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertThrows(NullPointerException.class, () -> uut.stream(null));
        }

        @Test
        void listIsUnmodifiable() {
            var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            assertDoesNotThrow(() -> MultiPropertySearchCollectionTest.addElements(uut));
            var list = uut.list();
            assertThat(list, hasSize(10));
            assertThrows(UnsupportedOperationException.class, list::clear);
        }
    }

    @Nested
    @DisplayName("equals() method")
    class Equals {

        @Test
        void twoEmptyAreEqual() {
            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            var uut2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);

            assertEquals(uut1, uut2);
        }

        @Test
        void twoEmptyAreDifferent() {
            enum AnotherPersonSearchableProperties implements SearchableProperty<Person> {
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

            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            var uut2 = new MultiPropertySearchCollection<>(AnotherPersonSearchableProperties.class);

            assertNotEquals(uut1, uut2);
        }

        @Test
        void twoCollectionsAreEqual() {
            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut1.add(new Person("test", "test", 20));
            var uut2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut2.add(new Person("test", "test", 20));

            assertEquals(uut1, uut2);
        }

        @Test
        void twoCollectionsAreDifferent() {
            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut1.add(new Person("test", "test", 10));
            var uut2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut2.add(new Person("test", "test", 20));

            assertNotEquals(uut1, uut2);
        }
    }

    @Nested
    @DisplayName("hashCode() method")
    class HashCode {

        @Test
        void hashCodesOfTwoEmptyAreEqual() {
            var hashCode1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class).hashCode();
            var hashCode2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class).hashCode();

            assertEquals(hashCode1, hashCode2);
        }

        @Test
        void hashCodesOfTwoEmptyAreDifferent() {
            enum AnotherPersonSearchableProperties implements SearchableProperty<Person> {
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

            var hashCode1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class).hashCode();
            var hashCode2 = new MultiPropertySearchCollection<>(AnotherPersonSearchableProperties.class).hashCode();

            assertNotEquals(hashCode1, hashCode2);
        }

        @Test
        void hashCodesOfTwoCollectionsAreEqual() {
            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut1.add(new Person("test", "test", 20));
            var uut2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut2.add(new Person("test", "test", 20));

            assertEquals(uut1.hashCode(), uut2.hashCode());
        }

        @Test
        void hashCodesOfTwoCollectionsAreDifferent() {
            var uut1 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut1.add(new Person("test", "test", 10));
            var uut2 = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
            uut2.add(new Person("test", "test", 20));

            assertNotEquals(uut1.hashCode(), uut2.hashCode());
        }

    }

    private static void addElements(MultiPropertySearchCollection<Person> uut) {
        uut.add(new Person("Caleb", "Dominguez", 1));
        uut.add(new Person("James", "Ryan", 2));
        uut.add(new Person("Jacob", "Smith", 3));
        uut.add(new Person("Kelsey", "Hawkins", 4));
        uut.add(new Person("Karen", "Mcguire", 5));
        uut.add(new Person("Colleen", null, 6));
        uut.add(new Person("Crystal", "Carey", 7));
        uut.add(new Person("John", "King", 8));
        uut.add(new Person("Stephanie", "Chen", 9));
        uut.add(new Person("Justin", "Fuller", 10));
    }

    private static void addElementsWithIntersection(MultiPropertySearchCollection<Person> uut) {
        uut.add(new Person("Caleb", "Dominguez", 1));
        uut.add(new Person("James", "Ryan", 2));
        uut.add(new Person("Jacob", "Smith", 3));
        uut.add(new Person("Kelsey", "Hawkins", 4));
        uut.add(new Person("Caleb", "Mcguire", 5));
        uut.add(new Person("Colleen", "Dominguez", 6));
        uut.add(new Person("Crystal", "Carey", 7));
        uut.add(new Person("John", "King", 8));
        uut.add(new Person("Jacob", "Dominguez", 9));
        uut.add(new Person("Jacob", "Fuller", 10));
    }

}