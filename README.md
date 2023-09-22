# Multi-index indexed collection

[Read in Russian](README_RU.md)

It is a sandbox repository with a collection that allows you to store objects in a list and search through predefined
object properties in better than O(n) time.

## Features
- TODO


## Introduction to the problem

Suppose in your application you operate with an in-memory list of java objects:

```java
var persons = new ArrayList<Person>();
```

You often manipulate this list, for example, filter the values by some criteria.

In Java ≥ 8, we can do this with the Stream API:

```java
var johns = persons.stream().filter(p -> "john".equals(p.name())).toList();
```

This is a linear search, O(n) complexity. If your list of objects is large enough (more than 1 million objects),
frequent searches of such a list can take a long time. To speed up the search, we need to have some kind of index. For example, we can convert a list to a map:

```java
var personsByFirstName = persons.stream().collect(Collectors.toMap(Person::firstName, Function.identity()));
```

Converting a large list to a map will also take some time, but we will be able to get the element in close to constant time:

```java
var johns = personsByFirstName.get("john");
```

The HashMap implementation of the Java Collection Framework does not allow multiple values to be associated with the same key.
You can use various multi-value mappings from third-party libraries, such as commons-collections.
But using this type of collection will not give us the ability to remove an arbitrary value from a group of values
with the same key. 

And what if we need to be able to search not by one, but by an arbitrary number of criteria?


## There is a solution!

The result of solving the previously considered problems was the class `MultiIndexIndexedCollection`.

To use it you need:

1. For your object's class create an `enum` that implements interface `com.github.lexakimov.collections.IndexDefinition`:

```java
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
```

2. Create an instance of `com.github.lexakimov.collections.MultiIndexIndexedCollection`, with previously created enum as constructor argument:

```java
var collection = new MultiIndexIndexedCollection<Person>(PersonIndexDefinition.class);
```

3. Add elements to collection:

```java
collection.add(new Person("Caleb", "Dominguez", 1));
collection.add(new Person("James", "Ryan", 2));
collection.add(new Person("Jacob", "Smith", 3));
collection.add(new Person("Caleb", "Hawkins", 4));
```

4. Filter and search items quickly:

```java
var result1 = collection.searchByProperty(PersonSearchableProperties.FIRST_NAME, "Caleb");
var result2 = collection.searchByProperty(PersonSearchableProperties.LAST_NAME, "Ryan");
```

code example is here [Example.java](src%2Fmain%2Fjava%2Fcom%2Fgithub%2Flexakimov%2Fcollections%2FExample.java)

### See also

- [IndexedCollection (common-collection4)](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/collection/IndexedCollection.html)
- https://github.com/boonproject/boon
- http://rick-hightower.blogspot.com/2013/11/what-if-java-collections-and-java.html
