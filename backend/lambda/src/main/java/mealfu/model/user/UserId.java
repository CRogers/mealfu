package mealfu.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Splitter;
import mealfu.ids.MealfuEntityId;
import org.immutables.value.Value;
import uk.callumr.eventstore.core.EventType;

import java.util.List;

@Value.Immutable
public abstract class UserId extends MealfuEntityId<UserEvent> {

    protected abstract OAuth2Provider oAuth2Provider();

    protected abstract String sub();

    @Override
    public String identifier() {
        return oAuth2Provider().asString() + "-" + sub();
    }

    @Override
    public String entityType() {
        return "user";
    }

    @Override
    public Class<? extends UserEvent> eventClassFor(EventType eventType) {
        return UserEvents.typeNameToClass(eventType.asString());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ImmutableUserId.Builder { }

    @JsonCreator
    public static UserId parse(String stringId) {
        return MealfuEntityId.parse(s -> {
            List<String> split = Splitter.on('-').limit(2).splitToList(s);
            return builder()
                    .oAuth2Provider(OAuth2Provider.parse(split.get(0)))
                    .sub(split.get(1))
                    .build();
        }, stringId);
    }

    public static UserId google(String sub) {
        return builder()
                .oAuth2Provider(OAuth2Provider.GOOGLE)
                .sub(sub)
                .build();
    }
}

