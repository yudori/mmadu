package com.mmadu.registration.typeconverters;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FieldTypeConverterResolverTest {
    private FieldTypeConverterResolver fieldTypeConverterResolver = FieldTypeConverterResolver.getInstance();

    public FieldTypeConverterResolverTest() throws Exception {
    }

    @Test
    void getConverterForType() {
        assertThat(fieldTypeConverterResolver.getConverterForType("datetime").get(), equalTo(DateTimeType.class));
    }
}