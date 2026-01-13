package demo.ms.serviceb.api;

import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

public final class PersonEventHandler {
    private final PersonRepository repository;

    public PersonEventHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public void handle(PersonEvent event) {
        if (event == null || event.getOperation() == null) {
            return;
        }

        Person person = event.getPerson();
        String id = person != null ? person.getId() : null;

        switch (event.getOperation()) {
            case CREATE, UPDATE, GET -> {
                if (person == null) {
                    return;
                }
                repository.upsert(person);
            }
            case DELETE -> {
                if (id == null || id.isBlank()) {
                    return;
                }
                repository.delete(id);
            }
        }
    }
}
