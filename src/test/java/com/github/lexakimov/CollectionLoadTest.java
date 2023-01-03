package com.github.lexakimov;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.github.lexakimov.CollectionLoadTest.PersonSearchableProperty.FIRST_NAME;
import static com.github.lexakimov.CollectionLoadTest.PersonSearchableProperty.LAST_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CollectionLoadTest {

    @Test
    void searchInHugeCollection() throws IOException, URISyntaxException {

        var csvFilePath = Paths.get(getClass().getClassLoader().getResource("500_000_random_names.csv").toURI());

        var plainList = new LinkedList<Person>();

        // parse csv file
        try (var lines = Files.lines(csvFilePath)) {
            lines
                    .map(name -> name.split(" ", 2))
                    .map(splitName -> new Person(splitName[0], splitName[1], 10))
                    //.limit(10_000)
                    .forEach(plainList::add);
        }

        var start1 = System.currentTimeMillis();
        var uut = new Collection<>(PersonSearchableProperty.class);
        plainList.forEach(uut::addElement);
        System.out.printf("%s elements added to collection for %s%n", uut.size(),
                Duration.ofMillis(System.currentTimeMillis() - start1));


        performSearchInCollection(uut, FIRST_NAME, "Kristin");
        performLinearSearch(plainList, Person::firstName, "Kristin");
        System.out.println();

        performLinearSearch(plainList, Person::firstName, "Johnny");
        performSearchInCollection(uut, FIRST_NAME, "Johnny");
        System.out.println();


        performLinearSearch(plainList, Person::lastName, "Ruiz");
        performSearchInCollection(uut, LAST_NAME, "Ruiz");
        System.out.println();

        performSearchInCollection(uut, LAST_NAME, "Castillo");
        performLinearSearch(plainList, Person::lastName, "Castillo");
        System.out.println();

        performSearchInCollection(uut, FIRST_NAME, "Miller");
        performLinearSearch(plainList, Person::firstName, "Miller");
        System.out.println();

        performSearchInCollection(uut, FIRST_NAME, "Steven");
        performLinearSearch(plainList, Person::firstName, "Steven");
        System.out.println();
    }

    private static void performLinearSearch(
            LinkedList<Person> plainList,
            Function<Person, String> getterMethod,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = plainList.stream()
                .filter(person -> getterMethod.apply(person).equals(value))
                .collect(Collectors.toCollection(LinkedList::new));
        System.out.printf("[%-10s] %-5s elements found in list for %15sms%n",
                value, result.size(), System.currentTimeMillis() - start);
    }

    private static void performSearchInCollection(
            Collection<Person> uut,
            PersonSearchableProperty property,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = assertDoesNotThrow(() -> uut.searchByProperty(property, value));
        System.out.printf("[%-10s] %-5s elements found in collection for %9sms%n",
                value, result.size(), System.currentTimeMillis() - start);
    }


    record Person(String firstName, String lastName, int age) {

    }

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
}