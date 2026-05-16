package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.common.repository.BaseRepository;
import com.epam.indigoeln.eln.entity.RoleEntity;
import com.epam.indigoeln.eln.mapper.RoleMapper;
import com.epam.indigoeln.eln.model.EntityType;
import com.epam.indigoeln.eln.model.RoleDTO;
import com.epam.indigoeln.eln.model.RoleRef;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RoleRepository extends BaseRepository<RoleEntity> {

    @Inject
    RoleMapper roleMapper;

    public RoleRepository() {
        super(EntityType.ROLE, RoleEntity.class);
    }

    public List<RoleDTO> list() {
        return list("from Role order by name").stream()
                .map(roleMapper::entityToDTO)
                .toList();
    }

    public List<RoleRef> suggest() {
        return list("from Role order by name").stream()
                .map(roleMapper::entityToRef)
                .toList();
    }
}
