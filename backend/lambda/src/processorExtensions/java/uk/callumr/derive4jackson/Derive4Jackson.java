package uk.callumr.derive4jackson;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import org.derive4j.processor.api.*;
import org.derive4j.processor.api.model.DataConstructions;
import org.derive4j.processor.api.model.DataConstructor;
import org.derive4j.processor.api.model.MultipleConstructorsSupport;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(ExtensionFactory.class)
public final class Derive4Jackson implements ExtensionFactory {

    public static final String JACKSON_ANNOTATION = "com.fasterxml.jackson.annotation";
    public static final ClassName JSON_PROPERTY = ClassName.get(JACKSON_ANNOTATION, "JsonProperty");

    @Override
    public List<Extension> extensions(DeriveUtils deriveUtils) {
        return singletonList((adtModel, codeGenSpec) -> {

            String jsonTypeNamePrefix = DataConstructions.caseOf(adtModel.dataConstruction())
                    .multipleConstructors(multipleConstructors -> MultipleConstructorsSupport.caseOf(multipleConstructors)
                            .visitorDispatch((variableElement, declaredType, list) -> jsonTypeInfoPrefixFrom(declaredType))
                            .otherwiseEmpty()
                            .flatMap(x -> x))
                    .oneConstructor(dataConstructor -> jsonTypeInfoPrefixFrom(dataConstructor.deconstructor().visitorType()))
                    .otherwiseEmpty()
                    .flatMap(x -> x)
                    .map(JsonTypeInfoPrefix::value)
                    .orElse("");

            Set<String> strictConstructors = adtModel.dataConstruction()
                    .constructors()
                    .stream()
                    .map(DataConstructor::name)
                    .map(String::toLowerCase)
                    .collect(toSet());

            CodeBlock allConstructorsArrayLiteral = codeGenSpec.typeSpecs.stream()
                    .filter(typeSpec -> strictConstructors.contains(typeSpec.name.toLowerCase()))
                    .map(typeSpec -> CodeBlock.of("$N.class", typeSpec))
                    .collect(CodeBlock.joining(",", "new Class[] {", "}"));

            FieldSpec allConstructors = FieldSpec.builder(ArrayTypeName.of(Class.class), "ALL_CONSTRUCTORS", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(allConstructorsArrayLiteral)
                    .build();

            return DeriveResult.result(new TypeSpecModifier(codeGenSpec)
                    .modTypes(typeSpecs ->
                            typeSpecs.stream()
                                    .map(ts -> strictConstructors.contains(ts.name.toLowerCase())
                                            ? removePrivateModifier(jsonTypeNamePrefix, ts)
                                            : ts)
                                    .collect(toList()))
                    .build()
                    .toBuilder()
                    .addField(allConstructors)
                    .build());
        });
    }

    private static Optional<JsonTypeInfoPrefix> jsonTypeInfoPrefixFrom(DeclaredType declaredType) {
        return Optional.ofNullable(declaredType.asElement().getAnnotation(JsonTypeInfoPrefix.class));
    }

    private static TypeSpec removePrivateModifier(String jsonTypeNamePrefix, TypeSpec ts) {
        Stream<MethodSpec> getters = ts.fieldSpecs.stream()
                .map(fieldSpec -> MethodSpec.methodBuilder(fieldSpec.name)
                        .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY)
                                .addMember("value", "$S", fieldSpec.name)
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(fieldSpec.type)
                        .addCode(CodeBlock.builder()
                                .addStatement("return $N", fieldSpec)
                                .build())
                        .build());

        return new TypeSpecModifier(ts)
                .modAnnotations(annotationSpecs -> Stream.concat(annotationSpecs.stream(), Stream.of(AnnotationSpec.builder(ClassName.get(JACKSON_ANNOTATION, "JsonTypeName"))
                                .addMember("value", "$S", jsonTypeNamePrefix + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, ts.name))
                                .build()))
                        .collect(toList()))
                .modModifiers(modifiers -> modifiers
                        .stream()
                        .filter(m -> m != Modifier.PRIVATE)
                        .collect(toSet()))
                .modMethods(methodSpecs -> Stream.concat(getters, methodSpecs.stream()
                        .map(methodSpec -> methodSpec.isConstructor()
                                ? clearParameterList(methodSpec.toBuilder())
                                        .addAnnotation(ClassName.get(JACKSON_ANNOTATION, "JsonCreator"))
                                        .addParameters(methodSpec.parameters.stream()
                                                .map(parameterSpec -> parameterSpec.toBuilder()
                                                        .addAnnotation(AnnotationSpec.builder(JSON_PROPERTY)
                                                                .addMember("value", "$S", parameterSpec.name)
                                                                .build())
                                                        .build())
                                                .collect(Collectors.toList()))
                                        .build()
                                : methodSpec))
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