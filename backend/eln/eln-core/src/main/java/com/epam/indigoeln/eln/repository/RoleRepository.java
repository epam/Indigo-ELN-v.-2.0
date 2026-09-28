package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.mapper.RoleMapper;
import com.epam.indigoeln.eln.model.ELNEntityType;
import com.epam.indigoeln.eln.model.RoleDTO;
import com.epam.indigoeln.eln.model.RoleRef;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

import static com.epam.indigoeln.common.util.ModelUtil.map;

@ApplicationScoped
public class RoleRepository extends BaseRepository<RoleEntity> {

    @Inject
    RoleMapper roleMapper;

    public RoleRepository() {
        super(ELNEntityType.ROLE, RoleEntity.class);
    }

    public List<RoleDTO> list() {
        return map(doFind(Sort.by("name")), roleMapper::entityToDTO);
    }

    public List<RoleRef> suggest() {
        return map(doFind(Sort.by("name")), roleMapper::entityToRef);
    }
}
