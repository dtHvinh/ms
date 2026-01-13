package demo.ms.serviceb.api;

import demo.ms.common.Json;
import demo.ms.common.Person;

import java.util.Optional;

public final class RedisPersonRepository implements AutoCloseable, PersonRepository {
    private final KeyValueStore store;

    public RedisPersonRepository(KeyValueStore store) {
        if (store == null) {
            throw new IllegalArgumentException("store is required");
        }
        this.store = store;
    }

    @Override
    public void upsert(Person person) {
        if (person == null || person.getId() == null || person.getId().isBlank()) {
            throw new IllegalArgumentException("person.id is required");
        }
        store.set(key(person.getId()), Json.toJson(person));
    }

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is required");
        }
        store.del(key(id));
    }

    public Optional<Person> get(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String value = store.get(key(id));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Json.fromJson(value, Person.class));
    }

    static String key(String id) {
        return "person:" + id;
    }

    @Override
    public void close() {
        store.close();
    }
}
