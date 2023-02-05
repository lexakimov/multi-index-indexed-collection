package com.github.lexakimov.collections;

import com.github.lexakimov.omm.footprint.FootprintProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.github.lexakimov.collections.PersonSearchableProperty.FIRST_NAME;
import static com.github.lexakimov.collections.PersonSearchableProperty.LAST_NAME;
import static java.lang.System.out;

class MultiPropertySearchCollectionLoadTest {

    @Test
    void searchInHugeCollection() throws IOException, URISyntaxException {
        var csvFilePath = Paths.get(getClass().getClassLoader().getResource("500_000_random_names.csv").toURI());
        var plainList = new ArrayList<Person>();

        {
            var start = System.currentTimeMillis();
            for (int i = 0; i < 30; i++) {
                try (var lines = Files.lines(csvFilePath)) {
                    lines
                            .map(name -> {
                                var spaceIndex = name.indexOf(' ');
                                return new Person(name.substring(0, spaceIndex), name.substring(spaceIndex + 1), 10);
                            })
                            .forEach(plainList::add);
                }
            }
            out.printf("%s elements parsed from CSV for %sms%n", plainList.size(), System.currentTimeMillis() - start);
        }

        var uut = new MultiPropertySearchCollection<>(PersonSearchableProperty.class);
        {
            var start = System.currentTimeMillis();
            plainList.forEach(uut::add);
            out.printf("%s elements added to collection for %sms%n", uut.size(), System.currentTimeMillis() - start);
            out.println();
        }

        performSearchInCollection(uut, FIRST_NAME, "Kristin");
        performLinearSearch(plainList, FIRST_NAME, "Kristin");

        performSearchInCollection(uut, FIRST_NAME, "Johnny");
        performLinearSearch(plainList, FIRST_NAME, "Johnny");

        performSearchInCollection(uut, LAST_NAME, "Ruiz");
        performLinearSearch(plainList, LAST_NAME, "Ruiz");

        performSearchInCollection(uut, LAST_NAME, "Castillo");
        performLinearSearch(plainList, LAST_NAME, "Castillo");

        performSearchInCollection(uut, FIRST_NAME, "Miller");
        performLinearSearch(plainList, FIRST_NAME, "Miller");

        performSearchInCollection(uut, FIRST_NAME, "Steven");
        performLinearSearch(plainList, FIRST_NAME, "Steven");
    }

    private static void performLinearSearch(
            List<Person> plainList,
            PersonSearchableProperty property,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = plainList.stream()
                .filter(person -> Objects.equals(property.getFunc().apply(person), value))
                .collect(Collectors.toCollection(LinkedList::new));
        out.printf("  LINEAR SEARCH: %10s [%-10s] %6s elements found for %3s ms%n",
                property.name(), value, result.size(), System.currentTimeMillis() - start);
    }

    private static void performSearchInCollection(
            MultiPropertySearchCollection<Person> uut,
            PersonSearchableProperty property,
            String value
    ) {
        var start = System.currentTimeMillis();
        var result = Assertions.assertDoesNotThrow(() -> uut.searchByProperty(property, value));
        out.printf("EXTENDED SEARCH: %10s [%-10s] %6s elements found for %3s ms%n",
                property.name(), value, result.size(), System.currentTimeMillis() - start);
    }

}