package demo.ms.serviceb.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import demo.ms.common.Person;
import demo.ms.common.PersonEvent;

class PersonEventHandlerTest {
    @Test
    void handleCreateUpserts() {
        FakeRepo repo = new FakeRepo();
        PersonEventHandler handler = new PersonEventHandler(repo);

        handler.handle(new PersonEvent(PersonEvent.Operation.CREATE, new Person("1", "A", 1)));

        assertEquals(1, repo.upserts);
        assertEquals(0, repo.deletes);
    }

    @Test
    void handleDeleteDeletes() {
        FakeRepo repo = new FakeRepo();
        PersonEventHandler handler = new PersonEventHandler(repo);

        handler.handle(new PersonEvent(PersonEvent.Operation.DELETE, new Person("1", "DELETED", 0)));

        assertEquals(0, repo.upserts);
        assertEquals(1, repo.deletes);
        assertEquals("1", repo.lastDeletedId);
    }

    @Test
    void handleNullIgnored() {
        FakeRepo repo = new FakeRepo();
        PersonEventHandler handler = new PersonEventHandler(repo);

        handler.handle(null);
        assertEquals(0, repo.upserts);
        assertEquals(0, repo.deletes);
    }

    static final class FakeRepo implements PersonRepository {
        int upserts;
        int deletes;
        String lastDeletedId;

        @Override
        public void upsert(Person person) {
            upserts++;
        }

        @Override
        public void delete(String id) {
            deletes++;
            lastDeletedId = id;
        }
    }
}
