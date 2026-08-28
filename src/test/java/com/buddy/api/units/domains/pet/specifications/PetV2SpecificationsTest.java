package com.buddy.api.units.domains.pet.specifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.buddy.api.domains.pet.dtos.v2.PetV2SearchCriteriaDto;
import com.buddy.api.domains.pet.entities.PetV2Entity;
import com.buddy.api.domains.pet.enums.PetGender;
import com.buddy.api.domains.pet.enums.PetSpecies;
import com.buddy.api.domains.pet.specifications.v2.PetV2BasicSpecifications;
import com.buddy.api.domains.pet.specifications.v2.PetV2MetricSpecifications;
import com.buddy.api.domains.pet.specifications.v2.PetV2RelationshipSpecifications;
import com.buddy.api.domains.pet.specifications.v2.PetV2Specifications;
import com.buddy.api.domains.pet.specifications.v2.PetV2TextSpecifications;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PetV2Specifications — Modular Unit Tests")
class PetV2SpecificationsTest {

    @Mock
    private Root<PetV2Entity> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        when(cb.conjunction()).thenReturn(predicate);
    }

    @Nested
    @DisplayName("PetV2BasicSpecifications Tests")
    class BasicSpecificationsTests {

        @Test
        @DisplayName("Should create species equality predicate when species is provided")
        void should_filter_by_species() {
            final Path<Object> path = mock(Path.class);
            when(root.get("species")).thenReturn(path);
            when(cb.equal(path, PetSpecies.DOG)).thenReturn(predicate);

            final var spec = PetV2BasicSpecifications.hasSpecies(PetSpecies.DOG);
            final var result = spec.toPredicate(root, query, cb);

            assertThat(result).isNotNull().isEqualTo(predicate);
            verify(cb).equal(path, PetSpecies.DOG);
        }

        @Test
        @DisplayName("Should return conjunction when species is null")
        void should_return_conjunction_when_null() {
            final var spec = PetV2BasicSpecifications.hasSpecies(null);
            final var result = spec.toPredicate(root, query, cb);

            assertThat(result).isNotNull().isEqualTo(predicate);
            verify(cb).conjunction();
        }

        @Test
        @DisplayName("Should create gender equality predicate when gender is provided")
        void should_filter_by_gender() {
            final Path<Object> path = mock(Path.class);
            when(root.get("gender")).thenReturn(path);
            when(cb.equal(path, PetGender.MALE)).thenReturn(predicate);

            final var spec = PetV2BasicSpecifications.hasGender(PetGender.MALE);
            final var result = spec.toPredicate(root, query, cb);

            assertThat(result).isEqualTo(predicate);
            verify(cb).equal(path, PetGender.MALE);
        }

        @Test
        @DisplayName("Should return conjunction when gender is null")
        void should_return_conjunction_when_gender_null() {
            final var spec = PetV2BasicSpecifications.hasGender(null);
            final var result = spec.toPredicate(root, query, cb);

            assertThat(result).isEqualTo(predicate);
            verify(cb).conjunction();
        }

        @Test
        @DisplayName("Should filter by isNeutered")
        void should_filter_by_is_neutered() {
            final Path<Object> path = mock(Path.class);
            when(root.get("isNeutered")).thenReturn(path);
            when(cb.equal(path, true)).thenReturn(predicate);

            final var spec = PetV2BasicSpecifications.isNeutered(true);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should return conjunction when isNeutered is null")
        void should_return_conjunction_when_is_neutered_null() {
            final var spec = PetV2BasicSpecifications.isNeutered(null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should filter by isForAdoption")
        void should_filter_by_is_for_adoption() {
            final Path<Object> path = mock(Path.class);
            when(root.get("isForAdoption")).thenReturn(path);
            when(cb.equal(path, true)).thenReturn(predicate);

            final var spec = PetV2BasicSpecifications.isForAdoption(true);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should return conjunction when isForAdoption is null")
        void should_return_conjunction_when_is_for_adoption_null() {
            final var spec = PetV2BasicSpecifications.isForAdoption(null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }
    }

    @Nested
    @DisplayName("PetV2TextSpecifications Tests")
    class TextSpecificationsTests {

        @Test
        @DisplayName("Should create lower-case like predicate when name is provided")
        void should_filter_by_name_like() {
            final Path<String> path = mock(Path.class);
            final Expression<String> lowerExpr = mock(Expression.class);
            when(root.<String>get("name")).thenReturn(path);
            when(cb.lower(path)).thenReturn(lowerExpr);
            when(cb.like(lowerExpr, "%thor%")).thenReturn(predicate);

            final var spec = PetV2TextSpecifications.nameContains("Thor");
            final var result = spec.toPredicate(root, query, cb);

            assertThat(result).isEqualTo(predicate);
            verify(cb).like(lowerExpr, "%thor%");
        }

        @Test
        @DisplayName("Should return conjunction when name is blank or null")
        void should_return_conjunction_when_blank() {
            assertThat(PetV2TextSpecifications.nameContains(null).toPredicate(root, query, cb))
                .isEqualTo(predicate);
            assertThat(PetV2TextSpecifications.nameContains("   ").toPredicate(root, query, cb))
                .isEqualTo(predicate);
        }
    }

    @Nested
    @DisplayName("PetV2RelationshipSpecifications Tests")
    class RelationshipSpecificationsTests {

        @Test
        @DisplayName("Should filter by guardian profile ID")
        void should_filter_by_guardian_profile_id() {
            final var profileId = UUID.randomUUID();
            final Path<Object> guardianPath = mock(Path.class);
            final Path<Object> profileIdPath = mock(Path.class);

            when(root.get("guardianProfile")).thenReturn(guardianPath);
            when(guardianPath.get("profileId")).thenReturn(profileIdPath);
            when(cb.equal(profileIdPath, profileId)).thenReturn(predicate);

            final var spec = PetV2RelationshipSpecifications.hasGuardianProfileId(profileId);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should return conjunction when guardian profile ID is null")
        void should_return_conjunction_when_guardian_id_null() {
            final var spec = PetV2RelationshipSpecifications.hasGuardianProfileId(null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }
    }

    @Nested
    @DisplayName("PetV2MetricSpecifications Tests")
    class MetricSpecificationsTests {

        @Test
        @DisplayName("Should filter size between min and max")
        void should_filter_size_between() {
            final Path<BigDecimal> path = mock(Path.class);
            when(root.<BigDecimal>get("size")).thenReturn(path);
            when(cb.between(path, BigDecimal.valueOf(10), BigDecimal.valueOf(30)))
                .thenReturn(predicate);

            final var spec = PetV2MetricSpecifications.sizeBetween(
                BigDecimal.valueOf(10), BigDecimal.valueOf(30)
            );
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should filter weight greater than or equal to min")
        void should_filter_weight_min() {
            final Path<BigDecimal> path = mock(Path.class);
            when(root.<BigDecimal>get("weight")).thenReturn(path);
            when(cb.greaterThanOrEqualTo(path, BigDecimal.valueOf(5))).thenReturn(predicate);

            final var spec = PetV2MetricSpecifications.weightBetween(BigDecimal.valueOf(5), null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should filter age less than or equal to max")
        void should_filter_age_max() {
            final Path<Integer> path = mock(Path.class);
            when(root.<Integer>get("approximateAge")).thenReturn(path);
            when(cb.lessThanOrEqualTo(path, 5)).thenReturn(predicate);

            final var spec = PetV2MetricSpecifications.ageBetween(null, 5);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should return conjunction when range bounds are both null")
        void should_return_conjunction_when_range_null() {
            final var spec = PetV2MetricSpecifications.sizeBetween(null, null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }
    }

    @Nested
    @DisplayName("PetV2Specifications Facade Tests")
    class FacadeSpecificationsTests {

        @Test
        @DisplayName("Should return conjunction when criteria is null")
        void should_return_conjunction_when_criteria_null() {
            final var spec = PetV2Specifications.withCriteria(null);
            assertThat(spec.toPredicate(root, query, cb)).isEqualTo(predicate);
        }

        @Test
        @DisplayName("Should compose specifications from non-null criteria fields")
        void should_compose_specifications() {
            final var criteria = PetV2SearchCriteriaDto.builder()
                .species(PetSpecies.CAT)
                .gender(PetGender.FEMALE)
                .isForAdoption(true)
                .build();

            final var spec = PetV2Specifications.withCriteria(criteria);
            assertThat(spec).isNotNull();
        }
    }

    @Nested
    @DisplayName("Utility Constructors Tests")
    class UtilityConstructorsTests {

        @Test
        @DisplayName("Should throw UnsupportedOperationException on instantiation")
        void should_throw_on_utility_class_instantiation() {
            assertUtilityClass(PetV2BasicSpecifications.class);
            assertUtilityClass(PetV2TextSpecifications.class);
            assertUtilityClass(PetV2MetricSpecifications.class);
            assertUtilityClass(PetV2RelationshipSpecifications.class);
            assertUtilityClass(PetV2Specifications.class);
        }

        private void assertUtilityClass(final Class<?> clazz) {
            assertThatThrownBy(() -> {
                final var constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }
}
