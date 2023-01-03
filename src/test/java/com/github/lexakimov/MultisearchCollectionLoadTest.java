package com.github.lexakimov;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Collectors;
import static com.github.lexakimov.PersonSearchableProperty.FIRST_NAME;
import static com.github.lexakimov.PersonSearchableProperty.LAST_NAME;
import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MultisearchCollectionLoadTest {

    @Test
    void searchInHugeCollection() throws IOException, URISyntaxException {
        var csvFilePath = Paths.get(getClass().getClassLoader().getResource("500_000_random_names.csv").toURI());
        var plainList = new LinkedList<Person>();

        {
            var start = System.currentTimeMillis();
            for (int i = 0; i < 30; i++) {
                try (var lines = Files.lines(csvFilePath)) {
                    lines
                            .map(name -> name.split(" ", 2))
                            .map(splitName -> new Person(splitName[0], splitName[1], 10))
                            .forEach(plainList::add);
                }
            }
            out.printf("%s elements parsed from CSV for %sms%n", plainList.size(), System.currentTimeMillis() - start);
            out.println();
        }

        var uut = new MultisearchCollection<>(PersonSearchableProperty.class);
        {
            var start = System.currentTimeMillis();
            plainList.forEach(uut::addElement);
            out.printf("%s elements added to collection for %sms%n", uut.size(), System.currentTimeMillis() - start);
            out.println();
        }

        performSearchInCollection(uut, FIRST_NAME, "Kristin");
        performSearchInCollection(uut, FIRST_NAME, "Johnny");
        performSearchInCollection(uut, LAST_NAME, "Ruiz");
        performSearchInCollection(uut, LAST_NAME, "Castillo");
        performSearchInCollection(uut, FIRST_NAME, "Miller");
        performSearchInCollection(uut, FIRST_NAME, "Steven");
        out.println();

        performLinearSearch(plainList, FIRST_NAME, "Kristin");
        performLinearSearch(plainList, FIRST_NAME, "Johnny");
        performLinearSearch(plainList, LAST_NAME, "Ruiz");
        performLinearSearch(plainList, LAST_NAME, "Castillo");
        performLinearSearch(plainList, FIRST_NAME, "Miller");
        performLinearSearch(plainList, FIRST_NAME, "Steven");
        out.println();
    }

    private static void performLinearSearch(
            LinkedList<Person> plainList,
            PersonSearchableProperty property,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = plainList.stream()
                .filter(person -> property.getFunc().apply(person).equals(value))
                .collect(Collectors.toCollection(LinkedList::new));
        out.printf("[%-10s] %-5s elements found in list for %15sms%n",
                value, result.size(), System.currentTimeMillis() - start);
    }

    private static void performSearchInCollection(
            MultisearchCollection<Person> uut,
            PersonSearchableProperty property,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = assertDoesNotThrow(() -> uut.searchByProperty(property, value));
        out.printf("[%-10s] %-5s elements found in collection for %9sms%n",
                value, result.size(), System.currentTimeMillis() - start);
    }

}