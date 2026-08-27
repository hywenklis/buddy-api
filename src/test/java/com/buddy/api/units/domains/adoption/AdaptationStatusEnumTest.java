package com.buddy.api.units.domains.adoption;

import static org.assertj.core.api.Assertions.assertThat;

import com.buddy.api.domains.adoption.enums.AdaptationStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdaptationStatusEnumTest {

    @Test
    @DisplayName("Should have exactly 4 adaptation status values defined")
    void should_have_four_values() {
        assertThat(AdaptationStatusEnum.values()).hasSize(4);
    }

    @Test
    @DisplayName("Should contain IN_PROGRESS status")
    void should_contain_in_progress() {
        assertThat(AdaptationStatusEnum.IN_PROGRESS).isNotNull();
    }

    @Test
    @DisplayName("Should contain ADAPTED status")
    void should_contain_adapted() {
        assertThat(AdaptationStatusEnum.ADAPTED).isNotNull();
    }

    @Test
    @DisplayName("Should contain STRUGGLING status")
    void should_contain_struggling() {
        assertThat(AdaptationStatusEnum.STRUGGLING).isNotNull();
    }

    @Test
    @DisplayName("Should contain RETURNED status")
    void should_contain_returned() {
        assertThat(AdaptationStatusEnum.RETURNED).isNotNull();
    }
}
