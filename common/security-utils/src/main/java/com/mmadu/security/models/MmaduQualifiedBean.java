package com.mmadu.security.models;

import org.springframework.context.annotation.Bean;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Bean
@MmaduQualified
public @interface MmaduQualifiedBean {
}
