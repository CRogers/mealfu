package mealfu.derive4j;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.derive4j.processor.api.*;
import org.derive4j.processor.api.model.DataConstructor;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(ExtensionFactory.class)
public final class MakeConstructorsPrivate implements ExtensionFactory {
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
                                ? methodSpec.toBuilder()
                                        .addModifiers(Modifier.PUBLIC)
                                        .addAnnotation(ClassName.get("com.fasterxml.jackson.annotation", "JsonCreator"))
                                        .build()
                                : methodSpec)
                        .collect(Collectors.toList()))
                .build();
    }
}