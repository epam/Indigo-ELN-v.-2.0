package com.epam.indigoeln.core.repository.role;

import com.epam.indigoeln.core.model.RolePermission;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.stream.Collectors;

@Repository
public class RolePermissionRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void deleteRolePermission(String id) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        mongoTemplate.remove(query, RolePermission.class);
    }

    public void saveRolePermission(RolePermission rolePermission) {
        mongoTemplate.save(rolePermission);
    }

    public RolePermission getRolePermission(String roleId, String permission) {
        Query searchQuery = new Query(
                Criteria.where("role_id").is(new ObjectId(roleId))
                        .andOperator(Criteria.where("value").is(permission)));
        return mongoTemplate.findOne(searchQuery, RolePermission.class);
    }

    public Collection<RolePermission> getRolesPermissions(Collection<String> rolesIds) {
        Query searchQuery = new Query(Criteria.where("role_id").in(
                rolesIds.stream().map(ObjectId::new).collect(Collectors.toList())
        ));
        return mongoTemplate.find(searchQuery, RolePermission.class);
    }
}
