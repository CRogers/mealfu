package uk.callumr.eventstore.jooq;

public enum JooqUtils {
    ;

    public static void noLogo() {
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }
}
