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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AutoService(ExtensionFactory.class)
public final class Derive4Jackson implements ExtensionFactory {

    private static final String JACKSON_ANNOTATION = "com.fasterxml.jackson.annotation";
    private static final ClassName JSON_CREATOR = ClassName.get(JACKSON_ANNOTATION, "JsonCreator");
    private static final ClassName JSON_TYPE_NAME = ClassName.get(JACKSON_ANNOTATION, "JsonTypeName");
    private static final ClassName JSON_PROPERTY = ClassName.get(JACKSON_ANNOTATION, "JsonProperty");
    private static final ClassName JACKSON_ANNOTATIONS_INSIDE = ClassName.get(JACKSON_ANNOTATION, "JacksonAnnotationsInside");
    private static final ClassName JSON_SUB_TYPES = ClassName.get(JACKSON_ANNOTATION, "JsonSubTypes");
    private static final ClassName JSON_SUB_TYPES_TYPE = ClassName.get(JACKSON_ANNOTATION, "JsonSubTypes", "Type");

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
                    .map(JsonTypeNamePrefix::value)
                    .orElse("");

            Set<String> strictConstructors = adtModel.dataConstruction()
                    .constructors()
                    .stream()
                    .map(DataConstructor::name)
                    .map(String::toLowerCase)
                    .collect(toSet());

            List<TypeSpec> allStrictConstructors = codeGenSpec.typeSpecs.stream()
                    .filter(typeSpec -> strictConstructors.contains(typeSpec.name.toLowerCase()))
                    .collect(Collectors.toList());

            Function<TypeSpec, String> jsonTypeName = ts -> jsonTypeNamePrefix + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, ts.name);

            ParameterSpec typeNameParam = ParameterSpec.builder(String.class, "typeName").build();

            MethodSpec typeNameToClass = MethodSpec.methodBuilder("typeNameToClass")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(TypeName.get(adtModel.typeConstructor().declaredType()))))
                    .addParameter(typeNameParam)
                    .addCode(CodeBlock.of("switch($N) {", typeNameParam))
                    .addCode(allStrictConstructors.stream()
                            .map(typeSpec -> CodeBlock.of("\n\tcase $S: return $N.class;", jsonTypeName.apply(typeSpec), typeSpec))
                            .collect(CodeBlock.joining("")))
                    .addCode(CodeBlock.of("\n\tdefault: throw new $1T($2N + $3S);\n}\n", IllegalArgumentException.class, typeNameParam, "is not a valid type name"))
                    .build();

            TypeSpec subtypes = TypeSpec.annotationBuilder("AllJsonSubTypes")
                    .addAnnotation(AnnotationSpec.builder(Retention.class)
                            .addMember("value", "$T.RUNTIME", RetentionPolicy.class)
                            .build())
                    .addAnnotation(JACKSON_ANNOTATIONS_INSIDE)
                    .addAnnotation(AnnotationSpec.builder(JSON_SUB_TYPES)
                            .addMember("value", allStrictConstructors.stream()
                                    .map(typeSpec -> CodeBlock.of("@$1T($2N.class)", JSON_SUB_TYPES_TYPE, typeSpec))
                                    .collect(CodeBlock.joining(", ", "{", "}")))
                            .build())
                    .addModifiers(Modifier.PUBLIC)
                    .build();

            return DeriveResult.result(new TypeSpecModifier(codeGenSpec)
                    .modTypes(typeSpecs ->
                            typeSpecs.stream()
                                    .map(ts -> strictConstructors.contains(ts.name.toLowerCase())
                                            ? removePrivateModifier(jsonTypeName, ts)
                                            : ts)
                                    .collect(toList()))
                    .build()
                    .toBuilder()
                    .addType(subtypes)
                    .addMethod(typeNameToClass)
                    .build());
        });
    }

    private static Optional<JsonTypeNamePrefix> jsonTypeInfoPrefixFrom(DeclaredType declaredType) {
        return Optional.ofNullable(declaredType.asElement().getAnnotation(JsonTypeNamePrefix.class));
    }

    private static TypeSpec removePrivateModifier(Function<TypeSpec, String> jsonTypeName, TypeSpec ts) {
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
                .modAnnotations(annotationSpecs -> Stream.concat(annotationSpecs.stream(), Stream.of(AnnotationSpec.builder(JSON_TYPE_NAME)
                                .addMember("value", "$S", jsonTypeName.apply(ts))
                                .build()))
                        .collect(toList()))
                .modModifiers(modifiers -> modifiers
                        .stream()
                        .filter(m -> m != Modifier.PRIVATE)
                        .collect(toSet()))
                .modMethods(methodSpecs -> Stream.concat(getters, methodSpecs.stream()
                        .map(methodSpec -> methodSpec.isConstructor()
                                ? clearParameterList(methodSpec.toBuilder())
                                        .addAnnotation(JSON_CREATOR)
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