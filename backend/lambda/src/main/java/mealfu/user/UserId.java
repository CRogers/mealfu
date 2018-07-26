package mealfu.user;

import org.immutables.value.Value;

@Value.Immutable
public interface UserId {
    String asString();

    class Builder extends ImmutableUserId.Builder { }

    static Builder builder() {
        return new Builder();
    }

    static UserId of(String userId) {
        return builder().asString(userId).build();
    }
}

