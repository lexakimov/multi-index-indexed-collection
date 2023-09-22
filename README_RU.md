# Multi-index indexed collection

Это sandbox-репозиторий с коллекцией, позволяющей хранить объекты в списке и осуществлять поиск по заранее определенным
свойствам объектов за время, лучшее чем O(n).

## Введение в проблему

Предположим, в своем приложении вы оперируете in-memory списком java объектов:

```java
var persons = new ArrayList<Person>();
```

Вы часто обращаетесь с этому списку, например отфильтровываете значения по какому-то критерию.
В Java >= 8 мы можем сделать это при помощи Stream API:

```java
var johns = persons.stream().filter(p -> "john".equals(p.name())).toList();
```

Это линейный поиск, сложность O(n). Если ваш список объектов достаточно большой (более 1 млн объектов), частый
поиск по такому списку может занимать много времени.
Для ускорения поиска нам нужно иметь какой-то индекс. Например, мы можем преобразовать список в отображение:

```java
var personsByFirstName = persons.stream().collect(Collectors.toMap(Person::firstName, Function.identity()));
```

Преобразование большого списка в отображение тоже займет какое-то время, но у нас будет возможность получать элемент за
время, близкое к постоянному.

```java
var johns = personsByFirstName.get("john");
```

Реализация HashMap из состава Java Collection Framework не позволяет ассоциировать с одним ключом несколько значений.
Можно воспользоваться различными multi-value отображениями из сторонних библиотек, например commons-collections.
Но использование такого типа коллекции не даст нам возможность удалить произвольное значение из группы значений с одним ключом.
И что если нам надо иметь возможность поиска не по одному, а по произвольному количеству критериев.

## Решение выше описанных проблем:

Итогом решения ранее рассмотренных проблем стал класс `MultiIndexIndexedCollection`. Для его использования нужно:

Создайте для вашего класса `enum` реализующий интерфейс `com.github.lexakimov.collections.IndexDefinition`:

```java
enum PersonSearchableProperties implements SearchableProperty<Person> {
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
```

Создайте экземпляр класса `com.github.lexakimov.collections.MultiIndexIndexedCollection`, передав в конструктор ранее созданный enum-класс:

```java
var collection = new MultiIndexIndexedCollection<Person>(PersonSearchableProperties.class);
```

Добавляйте в коллекцию элементы:

```java
collection.add(new Person("Caleb", "Dominguez", 1));
collection.add(new Person("James", "Ryan", 2));
collection.add(new Person("Jacob", "Smith", 3));
collection.add(new Person("Caleb", "Hawkins", 4));
```

При это мы всегда сможете отфильтровать объекты по ранее объявленным критериям:
```java
var result1 = collection.searchByProperty(PersonSearchableProperties.FIRST_NAME, "Caleb");
var result2 = collection.searchByProperty(PersonSearchableProperties.LAST_NAME, "Ryan");
```


### см. также
- [IndexedCollection (common-collection4)](https://commons.apache.org/proper/commons-collections/apidocs/org/apache/commons/collections4/collection/IndexedCollection.html)
- https://github.com/boonproject/boon
- http://rick-hightower.blogspot.com/2013/11/what-if-java-collections-and-java.html
