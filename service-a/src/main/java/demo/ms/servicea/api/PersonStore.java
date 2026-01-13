package demo.ms.servicea.api;

import demo.ms.common.Person;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class PersonStore {
    private final Map<String, Person> people = new ConcurrentHashMap<>();

    public Optional<Person> get(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(people.get(id));
    }

    public Person create(Person person) {
        validatePerson(person);
        if (people.putIfAbsent(person.getId(), person) != null) {
            throw new IllegalArgumentException("Person already exists: " + person.getId());
        }
        return person;
    }

    public Person update(String id, Person updated) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (updated == null) {
            throw new IllegalArgumentException("body is required");
        }
        Person newPerson = new Person(id, updated.getName(), updated.getAge());
        validatePerson(newPerson);
        if (!people.containsKey(id)) {
            throw new IllegalArgumentException("Person not found: " + id);
        }
        people.put(id, newPerson);
        return newPerson;
    }

    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        people.remove(id);
    }

    private static void validatePerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("body is required");
        }
        if (person.getId() == null || person.getId().isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        if (person.getName() == null || person.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (person.getAge() < 0) {
            throw new IllegalArgumentException("age must be >= 0");
        }
    }
}
