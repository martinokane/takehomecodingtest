package com.barclays.takehomecodingtest.utils;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;

import java.util.EnumSet;
import java.util.UUID;

public class UserIdGenerator implements BeforeExecutionGenerator {

    private static final String PREFIX = "usr-";
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 8;

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
            EventType eventType) {
        return generateUserId();
    }

    public static String generateUserId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        StringBuilder id = new StringBuilder(PREFIX);

        for (int i = 0; i < ID_LENGTH; i++) {
            int charIndex = (uuid.charAt(i % uuid.length()) - '0' + i) % CHARS.length();
            id.append(CHARS.charAt(Math.abs(charIndex)));
        }

        return id.toString();
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }
}
