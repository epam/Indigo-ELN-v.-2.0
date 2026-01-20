package com.epam.indigoeln.eln.repository;

import com.epam.indigoeln.eln.entity.SaltCodeEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SaltCodeRepository implements PanacheRepositoryBase<SaltCodeEntity, UUID> {
}
