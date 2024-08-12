package com.buddy.api.domains.pet.specifications;

import com.buddy.api.commons.enums.AgeRange;
import com.buddy.api.commons.enums.Gender;
import com.buddy.api.commons.enums.Species;
import com.buddy.api.commons.enums.WeightRange;
import com.buddy.api.domains.pet.entities.PetEntity;
import com.buddy.api.web.pets.requests.PetSearchCriteriaRequest;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.jpa.domain.Specification;

// TODO: Refatorar essa classe pois está muito grande e complexa
public final class PetSpecifications {

    private PetSpecifications() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SPECIE = "specie";
    private static final String FIELD_GENDER = "gender";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_WEIGHT = "weight";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_BIRTH_DATE = "birthDate";

    public static Specification<PetEntity> withParams(PetSearchCriteriaRequest params) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addPredicateIfNotNull(predicates, params.id(),
                value -> criteriaBuilder.equal(root.get(FIELD_ID), value));

            addPredicateIfNotNull(predicates, params.shelterId(),
                value -> criteriaBuilder.equal(root.get("shelter").get(FIELD_ID), value));

            addPredicateIfNotNull(predicates, params.name(),
                value -> criteriaBuilder.like(root.get(FIELD_NAME), "%" + value + "%"));

            addPredicateIfNotNull(predicates, params.species(),
                value -> criteriaBuilder.equal(root.get(FIELD_SPECIE),
                    Species.valueOfDescription(value).getDescription()));

            addPredicateIfNotNull(predicates, params.gender(),
                value -> criteriaBuilder.equal(root.get(FIELD_GENDER),
                    Gender.valueOfDescription(value).getDescription()));

            addPredicateIfNotNull(predicates, params.location(),
                value -> criteriaBuilder.like(root.get(FIELD_LOCATION), "%" + value + "%"));

            addPredicateIfNotNull(predicates, params.weightRange(), value -> {
                WeightRange range = WeightRange.fromDescription(value);
                return criteriaBuilder.between(
                    root.get(FIELD_WEIGHT), range.getMin(), range.getMax()
                );
            });

            addPredicateIfNotNull(predicates, params.description(),
                value -> criteriaBuilder.like(root.get(FIELD_DESCRIPTION), "%" + value + "%"));

            if (params.ageRange() != null) {
                AgeRange ageRange = AgeRange.fromDescription(params.ageRange());

                LocalDate today = LocalDate.now();
                LocalDate minBirthDate;
                LocalDate maxBirthDate;

                switch (ageRange.getDescription()) {
                    case "10+ anos" -> {
                        minBirthDate = today.minusYears(ageRange.getMin());
                        predicates.add(
                            criteriaBuilder.lessThanOrEqualTo(
                                root.get(FIELD_BIRTH_DATE), minBirthDate
                            )
                        );
                    }
                    case "0-1 anos" -> {
                        minBirthDate = today.minusYears(ageRange.getMax());
                        maxBirthDate = today;
                        predicates.add(
                            criteriaBuilder.between(
                                root.get(FIELD_BIRTH_DATE), minBirthDate, maxBirthDate
                            )
                        );
                    }
                    case "1-2 anos" -> {
                        minBirthDate = today.minusYears(2).plusDays(1);
                        maxBirthDate = today.minusYears(1);
                        predicates.add(
                            criteriaBuilder.between(
                                root.get(FIELD_BIRTH_DATE), minBirthDate, maxBirthDate
                            )
                        );
                    }
                    case "2-3 anos" -> {
                        minBirthDate = today.minusYears(3).plusDays(1);
                        maxBirthDate = today.minusYears(2);
                        predicates.add(
                            criteriaBuilder.between(
                                root.get(FIELD_BIRTH_DATE), minBirthDate, maxBirthDate
                            )
                        );
                    }
                    case "3-5 anos" -> {
                        minBirthDate = today.minusYears(5).plusDays(1);
                        maxBirthDate = today.minusYears(3);
                        predicates.add(
                            criteriaBuilder.between(
                                root.get(FIELD_BIRTH_DATE), minBirthDate, maxBirthDate
                            )
                        );
                    }
                    case "5-10 anos" -> {
                        minBirthDate = today.minusYears(10).plusDays(1);
                        maxBirthDate = today.minusYears(5);
                        predicates.add(
                            criteriaBuilder.between(
                                root.get(FIELD_BIRTH_DATE), minBirthDate, maxBirthDate
                            )
                        );
                    }
                    default -> throw new IllegalArgumentException(
                        "Unknown age range " + ageRange.getDescription()
                    );
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static <T> void addPredicateIfNotNull(
        List<Predicate> predicates,
        T value,
        Function<T, Predicate> predicateFunction) {
        if (value != null) {
            predicates.add(predicateFunction.apply(value));
        }
    }
}
