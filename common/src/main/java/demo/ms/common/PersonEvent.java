package demo.ms.common;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class PersonEvent {
    public enum Operation {
        GET,
        CREATE,
        UPDATE,
        DELETE
    }

    private final Operation operation;
    private final Person person;

    @JsonCreator
    public PersonEvent(
            @JsonProperty("operation") Operation operation,
            @JsonProperty("person") Person person) {
        this.operation = operation;
        this.person = person;
    }

    public Operation getOperation() {
        return operation;
    }

    public Person getPerson() {
        return person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonEvent that))
            return false;
        return operation == that.operation && Objects.equals(person, that.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, person);
    }
}
