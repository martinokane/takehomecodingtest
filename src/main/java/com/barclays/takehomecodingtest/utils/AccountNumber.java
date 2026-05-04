package com.barclays.takehomecodingtest.utils;

import org.hibernate.annotations.IdGeneratorType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for automatic Account Number generation.
 * Generates numbers in the format: 01XXXXXX
 */
@IdGeneratorType(value = AccountNumberGenerator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AccountNumber {
}
