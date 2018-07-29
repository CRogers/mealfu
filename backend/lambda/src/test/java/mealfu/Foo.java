package mealfu;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Foo {
    @SubTypes2
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public interface Super {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @JacksonAnnotationsInside
    @SubTypes
    public @interface SubTypes2 {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    @JacksonAnnotationsInside
    @JsonSubTypes({
            @JsonSubTypes.Type(Sub.class)
    })
    public @interface SubTypes {}

    @JsonTypeName("sub")
    public static class Sub implements Super {
        public String name;

        public Sub() { }

        public Sub(String name) {
            this.name = name;
        }
    }

    @Test
    public void test() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = objectMapper.writeValueAsString(new Sub("hello"));

        System.out.println("json = " + json);

        Super value = objectMapper.readValue(json, Super.class);

        System.out.println("value.getClass() = " + value.getClass());
    }
}
