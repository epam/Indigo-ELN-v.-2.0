package com.epam.indigoeln.eln.test;

import com.epam.indigoeln.common.util.Pair;
import com.epam.indigoeln.eln.model.ACLEntryDTO;
import com.epam.indigoeln.eln.model.AccessLevel;
import one.util.streamex.StreamEx;
import org.assertj.core.api.AbstractAssert;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ACLListAssert<T extends ACLEntryDTO> extends AbstractAssert<ACLListAssert<T>, List<T>> {

    public static <T extends ACLEntryDTO> ACLListAssert<T> assertThatACL(List<T> actual) {
        return new ACLListAssert<>(actual);
    }

    protected ACLListAssert(List<T> actual) {
        super(actual, ACLListAssert.class);
    }

    public ACLListAssert<T> containsOnly(String name, AccessLevel level, boolean inherited) {
        return containsOnly(Map.of(name, Pair.of(level, inherited)));
    }

    public ACLListAssert<T> containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2) {
        return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2)));
    }

    public ACLListAssert<T> containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2, String name3, AccessLevel level3, boolean inherited3) {
        return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2), name3, Pair.of(level3, inherited3)));
    }

    public ACLListAssert<T> containsOnly(String name1, AccessLevel level1, boolean inherited1, String name2, AccessLevel level2, boolean inherited2, String name3, AccessLevel level3, boolean inherited3, String name4, AccessLevel level4, boolean inherited4) {
        return containsOnly(Map.of(name1, Pair.of(level1, inherited1), name2, Pair.of(level2, inherited2), name3, Pair.of(level3, inherited3), name4, Pair.of(level4, inherited4)));
    }

    public ACLListAssert<T> containsOnly(Map<String, Pair<AccessLevel, Boolean>> expected) {
        Map<String, Pair<AccessLevel, Boolean>> actualMap = StreamEx.of(actual).toMap(ACLEntryDTO::getDisplayName, e -> Pair.of(e.getLevel(), e.getInherited()));
        assertThat(actualMap).containsExactlyInAnyOrderEntriesOf(expected);
        return this;
    }
}
