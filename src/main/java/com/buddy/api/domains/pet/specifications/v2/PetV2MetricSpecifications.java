package com.buddy.api.domains.pet.specifications.v2;

import com.buddy.api.domains.pet.entities.PetV2Entity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class PetV2MetricSpecifications {

    private static final String FIELD_SIZE = "size";
    private static final String FIELD_WEIGHT = "weight";
    private static final String FIELD_APPROXIMATE_AGE = "approximateAge";

    private PetV2MetricSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Specification<PetV2Entity> sizeBetween(final BigDecimal min,
                                                         final BigDecimal max) {
        return (root, query, cb) -> rangePredicate(cb, root.get(FIELD_SIZE), min, max);
    }

    public static Specification<PetV2Entity> weightBetween(final BigDecimal min,
                                                           final BigDecimal max) {
        return (root, query, cb) -> rangePredicate(cb, root.get(FIELD_WEIGHT), min, max);
    }

    public static Specification<PetV2Entity> ageBetween(final Integer min,
                                                        final Integer max) {
        return (root, query, cb) -> rangePredicate(cb, root.get(FIELD_APPROXIMATE_AGE), min, max);
    }

    private static <T extends Comparable<T>> Predicate rangePredicate(
        final CriteriaBuilder cb,
        final Path<T> path,
        final T min,
        final T max
    ) {
        if (min == null && max == null) {
            return cb.conjunction();
        }
        if (min != null && max != null) {
            return cb.between(path, min, max);
        }
        return min != null
            ? cb.greaterThanOrEqualTo(path, min)
            : cb.lessThanOrEqualTo(path, max);
    }
}
