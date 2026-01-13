package demo.ms.serviceb.api;

import demo.ms.common.Person;

public interface PersonRepository {
    void upsert(Person person);

    void delete(String id);
}
