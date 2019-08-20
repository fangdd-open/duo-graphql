package com.fangdd.graphql.scalar;

import graphql.Assert;
import graphql.Internal;
import graphql.language.*;
import graphql.schema.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 扩展Scalar Type，源码来自：https://github.com/graphql-java/graphql-java-extended-scalars
 *
 * @author xuwenzhen
 * @date 2019/7/16
 */
@Internal
public class ObjectScalar extends GraphQLScalarType {


    public ObjectScalar() {
        this("Object", "An object scalar");
    }

    ObjectScalar(String name, String description) {
        super(name, description, new Coercing<Object, Object>() {
            @Override
            public Object serialize(Object input) throws CoercingSerializeException {
                return input;
            }

            @Override
            public Object parseValue(Object input) throws CoercingParseValueException {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) throws CoercingParseLiteralException {
                return parseLiteral(input, Collections.emptyMap());
            }

            @Override
            public Object parseLiteral(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
                if (!(input instanceof Value)) {
                    throw new CoercingParseLiteralException(
                            "Expected AST type 'StringValue' but was '" + input.getClass().getName() + "'."
                    );
                }
                if (input instanceof NullValue) {
                    return null;
                }
                if (input instanceof FloatValue) {
                    return ((FloatValue) input).getValue();
                }
                if (input instanceof StringValue) {
                    return ((StringValue) input).getValue();
                }
                if (input instanceof IntValue) {
                    return ((IntValue) input).getValue();
                }
                if (input instanceof BooleanValue) {
                    return ((BooleanValue) input).isValue();
                }
                if (input instanceof EnumValue) {
                    return ((EnumValue) input).getName();
                }
                if (input instanceof VariableReference) {
                    String varName = ((VariableReference) input).getName();
                    return variables.get(varName);
                }
                if (input instanceof ArrayValue) {
                    List<Value> values = ((ArrayValue) input).getValues();
                    return values.stream()
                            .map(v -> parseLiteral(v, variables))
                            .collect(Collectors.toList());
                }
                if (input instanceof ObjectValue) {
                    List<ObjectField> values = ((ObjectValue) input).getObjectFields();
                    Map<String, Object> parsedValues = new LinkedHashMap<>();
                    values.forEach(fld -> {
                        Object parsedValue = parseLiteral(fld.getValue(), variables);
                        parsedValues.put(fld.getName(), parsedValue);
                    });
                    return parsedValues;
                }
                return Assert.assertShouldNeverHappen("We have covered all Value types");
            }
        });
    }

}