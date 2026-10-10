package com.prince.backend.repository;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PgVectorTest {

    @Test
    void formatsEmbeddingAsPgvectorLiteral() {
        assertEquals("[0.5,-1.0,2.25]", PgVector.toLiteral(List.of(0.5, -1.0, 2.25)));
    }

    @Test
    void formatsSingleValue() {
        assertEquals("[1.0]", PgVector.toLiteral(List.of(1.0)));
    }

    @Test
    void rejectsEmptyEmbedding() {
        assertThrows(IllegalArgumentException.class, () -> PgVector.toLiteral(List.of()));
    }

    @Test
    void rejectsNullEmbedding() {
        assertThrows(IllegalArgumentException.class, () -> PgVector.toLiteral(null));
    }

    @Test
    void rejectsNaNAndInfinity() {
        assertThrows(IllegalArgumentException.class,
                () -> PgVector.toLiteral(List.of(1.0, Double.NaN)));
        assertThrows(IllegalArgumentException.class,
                () -> PgVector.toLiteral(List.of(Double.POSITIVE_INFINITY)));
    }

    @Test
    void rejectsNullElement() {
        assertThrows(IllegalArgumentException.class,
                () -> PgVector.toLiteral(Arrays.asList(1.0, null)));
    }
}
