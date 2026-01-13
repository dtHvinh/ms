package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

class PersonEventHandlerEdgeTest {

    @Test
    void ignoresMissingPersonOnUpsertOperations() {
        FakeRepo repo = new FakeRepo();
        PersonEventHandler handler = new PersonEventHandler(repo);

        handler.handle(new PersonEvent(PersonEvent.Operation.CREATE, null));
        handler.handle(new PersonEvent(PersonEvent.Operation.UPDATE, null));
        handler.handle(new PersonEvent(PersonEvent.Operation.GET, null));

        assertEquals(0, repo.upserts);
        assertEquals(0, repo.deletes);
    }

    @Test
    void ignoresDeleteWhenIdBlank() {
        FakeRepo repo = new FakeRepo();
        PersonEventHandler handler = new PersonEventHandler(repo);

        handler.handle(new PersonEvent(PersonEvent.Operation.DELETE, new Person("", "X", 0)));

        assertEquals(0, repo.deletes);
    }

    static final class FakeRepo implements PersonRepository {
        int upserts;
        int deletes;

        @Override
        public void upsert(Person person) {
            upserts++;
        }

        @Override
        public void delete(String id) {
            deletes++;
        }
    }
}
