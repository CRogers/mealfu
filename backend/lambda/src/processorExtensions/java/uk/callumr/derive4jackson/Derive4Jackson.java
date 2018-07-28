package uk.callumr.derive4jackson;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import org.derive4j.processor.api.*;
import org.derive4j.processor.api.model.DataConstructor;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(ExtensionFactory.class)
public final class Derive4Jackson implements ExtensionFactory {

    public static final String JACKSON_ANNOTATION = "com.fasterxml.jackson.annotation";

    @Override
    public List<Extension> extensions(DeriveUtils deriveUtils) {
        return singletonList((adtModel, codeGenSpec) -> {

            Set<String> strictConstructors = adtModel.dataConstruction()
                    .constructors()
                    .stream()
                    .map(DataConstructor::name)
                    .map(String::toLowerCase)
                    .collect(toSet());

            return DeriveResult.result(new TypeSpecModifier(codeGenSpec)
                    .modTypes(typeSpecs ->
                            typeSpecs.stream()
                                    .map(ts -> strictConstructors.contains(ts.name.toLowerCase())
                                            ? removePrivateModifier(ts)
                                            : ts)
                                    .collect(toList()))
                    .build());
        });
    }

    private static TypeSpec removePrivateModifier(TypeSpec ts) {
        return new TypeSpecModifier(ts)
                .modModifiers(modifiers -> modifiers
                        .stream()
                        .filter(m -> m != Modifier.PRIVATE)
                        .collect(toSet()))
                .modMethods(methodSpecs -> methodSpecs.stream()
                        .map(methodSpec -> methodSpec.isConstructor()
                                ? clearParameterList(methodSpec.toBuilder())
                                        .addModifiers(Modifier.PUBLIC)
                                        .addAnnotation(ClassName.get(JACKSON_ANNOTATION, "JsonCreator"))
                                        .addParameters(methodSpec.parameters.stream()
                                                .map(parameterSpec -> parameterSpec.toBuilder()
                                                        .addAnnotation(AnnotationSpec.builder(ClassName.get(JACKSON_ANNOTATION, "JsonProperty"))
                                                                .addMember("value", "$S", parameterSpec.name)
                                                                .build())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .build()
                                : methodSpec)
                        .collect(Collectors.toList()))
                .build();
    }

    private static MethodSpec.Builder clearParameterList(MethodSpec.Builder builder) {
        try {
            Field field = builder.getClass().getDeclaredField("parameters");
            field.setAccessible(true);
            List<ParameterSpec> parameterSpecs = (List<ParameterSpec>) field.get(builder);
            parameterSpecs.clear();
            return builder;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}