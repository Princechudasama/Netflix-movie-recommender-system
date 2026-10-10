package com.prince.backend.repository;

import java.util.List;

/**
 * Helpers for passing embeddings to pgvector.
 */
public final class PgVector {

    private PgVector() {
    }

    /**
     * Converts an embedding into pgvector's text format, e.g. {@code [0.1,0.2,0.3]}.
     * Use it with a cast in SQL: {@code ?::vector}.
     */
    public static String toLiteral(List<Double> embedding) {

        if (embedding == null || embedding.isEmpty()) {
            throw new IllegalArgumentException(
                    "Embedding must not be null or empty."
            );
        }

        StringBuilder literal = new StringBuilder(embedding.size() * 10);
        literal.append('[');

        for (int i = 0; i < embedding.size(); i++) {

            Double value = embedding.get(i);

            // pgvector rejects NaN and infinity.
            if (value == null || value.isNaN() || value.isInfinite()) {
                throw new IllegalArgumentException(
                        "Embedding contains an invalid value at index " + i + "."
                );
            }

            if (i > 0) {
                literal.append(',');
            }

            literal.append(value.doubleValue());
        }

        return literal.append(']').toString();
    }
}
