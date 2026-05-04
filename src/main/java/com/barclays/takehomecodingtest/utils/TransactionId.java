package com.barclays.takehomecodingtest.utils;

import org.hibernate.annotations.IdGeneratorType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for automatic Transaction ID generation.
 * Generates IDs in the format: tan-XXXXXXXX
 */
@IdGeneratorType(value = TransactionIdGenerator.class)
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionId {
}
