package com.epam.indigoeln.eln.service;

import com.epam.indigoeln.common.exception.EntityNotFoundException;
import com.epam.indigoeln.eln.entity.DictionaryEntity;
import com.epam.indigoeln.eln.entity.DictionaryItemEntity;
import com.epam.indigoeln.eln.mapper.DictionaryMapper;
import com.epam.indigoeln.eln.model.*;
import com.epam.indigoeln.eln.repository.DictionaryItemRepository;
import com.epam.indigoeln.eln.repository.DictionaryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.*;

@Slf4j
@ApplicationScoped
@Transactional(Transactional.TxType.SUPPORTS) // cached methods don't require transaction
public class DictionaryService {

    @Inject
    DictionaryRepository dictionaryRepository;

    @Inject
    DictionaryItemRepository dictionaryItemRepository;

    @Inject
    DictionaryMapper dictionaryMapper;

    @Inject
    @CacheName("dictionaryItems")
    Cache dictionaryItemsCache;

    ObjectReader saltCodeDetailsReader;

    DictionaryService(ObjectMapper objectMapper) {
        saltCodeDetailsReader = objectMapper.readerFor(SaltCodeDetails.class);
    }

    void invalidate() {
        dictionaryItemsCache.invalidate("all").await().indefinitely();
    }

    private <T extends DictionaryItemRef> CachedItems<T> cached(String dictionaryRef) {
        //noinspection unchecked
        return (CachedItems<T>) cached().byDictionary.get(refToID(dictionaryRef));
    }

    private CachedAllItems cached() {
        return dictionaryItemsCache.get("all", key -> getCachedAllItems())
                .await().indefinitely();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    CachedAllItems getCachedAllItems() {
        CachedAllItems result = new CachedAllItems();
        for (DictionaryEntity dictionary : dictionaryRepository.listAll()) {
            result.byDictionary.put(dictionary.getId(), new CachedItems<>());
        }
        for (DictionaryItemEntity item : dictionaryItemRepository.listAll(Sort.by("name"))) {
            CachedItems<DictionaryItemRef> cachedItems = result.byDictionary.get(item.getDictionary().getId());
            DictionaryItemRef ref = convertToRef(item);
            result.add(ref);
        }
        return result;
    }

    public <T extends DictionaryItemRef> List<T> getDictionary(String dictionaryRef, boolean allowInactive) {
        CachedItems<T> cached = cached(dictionaryRef);
        return cached.list(allowInactive);
    }

    @Transactional
    public List<DictionaryItemDTO> getDictionaryFull(String dictionaryRef) {
        return dictionaryMapper.itemToDTOList(dictionaryItemRepository.list(refToID(dictionaryRef), true));
    }

    public <T extends DictionaryItemRef> T get(UUID id) {
        DictionaryItemRef ref = cached().all.get(id);
        if (ref == null) {
            throw new EntityNotFoundException(ELNEntityType.DICTIONARY_ITEM, id);
        }
        //noinspection unchecked
        return (T) ref;
    }

    @Nullable
    public <T extends DictionaryItemRef> T get(@Nullable DictionaryItemEntity entity) {
        if (entity == null) {
            return null;
        }
        DictionaryItemRef ref = cached().all.get(entity.getId());
        if (ref == null) {
            throw new EntityNotFoundException(ELNEntityType.DICTIONARY_ITEM, entity.getId());
        }
        //noinspection unchecked
        return (T) ref;
    }

    public <T extends DictionaryItemRef> List<T> get(Collection<DictionaryItemEntity> entities) {
        //noinspection unchecked,DataFlowIssue
        return entities.stream()
                .map(e -> (T) get(e))
                .toList();
    }

    @Nullable
    @Transactional(Transactional.TxType.REQUIRED)
    public DictionaryItemEntity lookup(@Nullable DictionaryItemRef ref) {
        return lookup(ref, true);
    }

    @Nullable
    @Transactional(Transactional.TxType.REQUIRED)
    public DictionaryItemEntity lookup(@Nullable DictionaryItemRef ref, boolean allowInactive) {
        if (ref == null) {
            return null;
        }
        if (!allowInactive && ref.isInactive()) {
            throw new EntityNotFoundException(ELNEntityType.DICTIONARY_ITEM, ref.getId() + " is not active or deleted");
        }
        return dictionaryItemRepository.getReference(ref.getId());
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<DictionaryItemEntity> lookup(Collection<? extends DictionaryItemRef> refs) {
        return lookup(refs, true);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public List<DictionaryItemEntity> lookup(Collection<? extends DictionaryItemRef> refs, boolean allowInactive) {
        List<DictionaryItemEntity> found = new ArrayList<>(refs.size());
        for (DictionaryItemRef ref : refs) {
            if (!allowInactive && ref.isInactive()) {
                throw new EntityNotFoundException(ELNEntityType.DICTIONARY_ITEM, ref.getId());
            } else {
                found.add(dictionaryItemRepository.getReference(ref.getId()));
            }
        }
        return found;
    }

    @Transactional
    public List<DictionaryItemRef> suggestDictionaryItems(String dictionaryRef, String search) {
        return dictionaryItemRepository.suggest(refToID(dictionaryRef), search);
    }

    public static UUID refToID(String dictionaryRef) {
        BuiltInDictionary builtInDictionary = BuiltInDictionary.lookup(dictionaryRef);
        if (builtInDictionary != null) {
            return builtInDictionary.getId();
        }
        try {
            return UUID.fromString(dictionaryRef);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException(ELNEntityType.DICTIONARY, dictionaryRef);
        }
    }

    @SneakyThrows
    private DictionaryItemRef convertToRef(DictionaryItemEntity entity) {
        BuiltInDictionary builtInDictionary = BuiltInDictionary.lookup(entity.getDictionary().getId());
        DictionaryItemRef.Creator creator = switch (builtInDictionary) {
            case null -> DictionaryItemRef::new;
            case THERAPEUTIC_AREA -> TherapeuticAreaRef::new;
            case PROJECT_CODE -> ProjectCodeRef::new;
            case PROJECT_KEYWORD -> ProjectKeywordRef::new;
            case STEREOISOMER_CODE -> StereoisomerCodeRef::new;
            case HEALTH_HAZARD -> HealthHazardRef::new;
            case HANDLING_PRECAUTIONS -> HandlingPrecautionsRef::new;
            case STORAGE_INSTRUCTIONS -> StorageInstructionsRef::new;
            case COMPOUND_PROTECTION -> CompoundProtectionRef::new;
            case SOLVENT -> SolventRef::new;
            case EXTERNAL_SUPPLIER -> ExternalSupplierRef::new;
            case SAMPLE_SOURCE -> SampleSourceRef::new;
            case SAMPLE_SOURCE_DETAILS -> SampleSourceDetailsRef::new;
            case COMPONENT_STATE -> ComponentStateRef::new;
            default -> null;
        };
        if (creator != null) {
            return creator.create(entity.getId(), entity.getName(), entity.getActive(), entity.getDeleted(), entity.getDictionary().getId());
        }
        //noinspection SwitchStatementWithTooFewBranches
        switch (builtInDictionary) {
            case SALT_CODE -> {
                SaltCodeDetails details = saltCodeDetailsReader.readValue(entity.getDetails());
                return new SaltCodeRef(entity.getId(), entity.getName(), entity.getActive(), entity.getDeleted(), entity.getDictionary().getId(), details.code(), details.formula(), details.charge(), details.molWeight());
            }
            default -> throw new UnsupportedOperationException(builtInDictionary.name());
        }
    }

    private static class CachedAllItems {

        private final Map<UUID, DictionaryItemRef> all = new HashMap<>();
        private final Map<UUID, CachedItems<DictionaryItemRef>> byDictionary = new HashMap<>();

        public void add(DictionaryItemRef ref) {
            all.put(ref.getId(), ref);
            CachedItems<DictionaryItemRef> items = byDictionary.get(ref.getDictionaryID());
            items.map.put(ref.getId(), ref);
            items.list.add(ref);
        }
    }

    private static class CachedItems<T extends DictionaryItemRef> {

        private final Map<UUID, T> map = new HashMap<>();
        private final List<T> list = new ArrayList<>();

        @Nullable
        T get(UUID id, boolean allowInactive) {
            T ref = map.get(id);
            if (!allowInactive && ref.isInactive()) {
                ref = null;
            }
            return ref;
        }

        boolean contains(UUID id, boolean allowInactive) {
            return get(id, allowInactive) != null;
        }

        List<T> list(boolean allowInactive) {
            if (!allowInactive) {
                return list.stream()
                        .filter(x -> !x.isInactive())
                        .toList();
            }
            return list;
        }
    }

    @RegisterForReflection
    private record SaltCodeDetails (
            String code,
            String formula,
            int charge,
            double molWeight
    ) {}
}
