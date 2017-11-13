/* @ngInject */
function permissionManagementService($q, principalService, userRemovableFromProject, userRemovableFromNotebook,
                                     permissionsConstants) {
    var accessList;
    var author;
    var entity;
    var entityId;
    var parentId;

    var VIEWER = ['READ_ENTITY'];
    var USER = ['READ_ENTITY', 'CREATE_SUB_ENTITY'];
    var OWNER = ['READ_ENTITY', 'CREATE_SUB_ENTITY', 'UPDATE_ENTITY'];

    return {
        expandPermission: expandPermission,
        hasPermission: hasPermission,
        getAuthorAccessList: getAuthorAccessList,
        getAccessList: getAccessList,
        setAccessList: setAccessList,
        getAuthor: getAuthor,
        setAuthor: setAuthor,
        getEntity: getEntity,
        setEntity: setEntity,
        getEntityId: getEntityId,
        setEntityId: setEntityId,
        getParentId: getParentId,
        setParentId: setParentId,
        hasAuthorityForProjectPermission: hasAuthorityForProjectPermission,
        hasAuthorityForNotebookPermission: hasAuthorityForNotebookPermission,
        hasAuthorityForExperimentPermission: hasAuthorityForExperimentPermission,
        hasAuthorityForPermission: hasAuthorityForPermission,
        isUserRemovableFromAccessList: isUserRemovableFromAccessList
    };

    function expandPermission(list) {
        _.each(list, function(item) {
            if (item.permissionView === 'OWNER') {
                item.permissions = OWNER;
            } else if (item.permissionView === 'USER') {
                item.permissions = USER;
            } else {
                item.permissions = VIEWER;
            }
        });

        return list;
    }

    function hasPermission(permission) {
        if (!accessList) {
            return false;
        }

        var userId = principalService.getIdentity().id;

        return _.some(accessList, function(item) {
            return item.user.id === userId && _.includes(item.permissions, permission);
        });
    }

    function getAuthorAccessList(entityAuthor, projectAuthor) {
        var experimentCreatorPermissions = {
            user: entityAuthor,
            permissions: ['READ_ENTITY', 'CREATE_SUB_ENTITY', 'UPDATE_ENTITY'],
            permissionView: 'OWNER'
        };
        if (projectAuthor && projectAuthor.id !== entityAuthor.id) {
            var projectCreatorPermissions = {
                user: projectAuthor,
                permissions: ['READ_ENTITY'],
                permissionView: 'VIEWER'
            };

            return [experimentCreatorPermissions, projectCreatorPermissions];
        }

        return [experimentCreatorPermissions];
    }

    function getAccessList() {
        return accessList;
    }

    function setAccessList(newAccessList) {
        accessList = newAccessList;
    }

    function getAuthor() {
        return author;
    }

    function setAuthor(newAuthor) {
        author = newAuthor;
    }

    function getEntity() {
        return entity;
    }

    function setEntity(newEntity) {
        entity = newEntity;
    }

    function getEntityId() {
        return entityId;
    }

    function setEntityId(newEntityId) {
        entityId = newEntityId;
    }

    function getParentId() {
        return parentId;
    }

    function setParentId(newParentId) {
        parentId = newParentId;
    }

    function hasAuthorityForProjectPermission(member, permission) {
        var projectOwnerAuthoritySet = permissionsConstants.PROJECT_OWNER_AUTHORITY_SET;
        var projectUserAuthoritySet = permissionsConstants.PROJECT_USER_AUTHORITY_SET;
        var projectViewerAuthoritySet = permissionsConstants.PROJECT_VIEWER_AUTHORITY_SET;

        if (permission === 'OWNER') {
            return _.every(projectOwnerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === 'USER') {
            return _.every(projectUserAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(projectViewerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForNotebookPermission(member, permission) {
        var notebookOwnerAuthoritySet = permissionsConstants.NOTEBOOK_OWNER_AUTHORITY_SET;
        var notebookUserAuthoritySet = permissionsConstants.NOTEBOOK_USER_AUTHORITY_SET;
        var notebookViewerAuthoritySet = permissionsConstants.NOTEBOOK_VIEWER_AUTHORITY_SET;

        if (permission === 'OWNER') {
            return _.every(notebookOwnerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === 'USER') {
            return _.every(notebookUserAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(notebookViewerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForExperimentPermission(member, permission) {
        var experimentOwnerAuthoritySet = permissionsConstants.EXPERIMENT_OWNER_AUTHORITY_SET;
        var experimentViewerAuthoritySet = permissionsConstants.EXPERIMENT_VIEWER_AUTHORITY_SET;

        if (permission === 'OWNER') {
            return _.every(experimentOwnerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(experimentViewerAuthoritySet, function(authority) {
                return _.includes(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForPermission(member, permission) {
        if (entity === 'Project') {
            return hasAuthorityForProjectPermission(member, permission);
        } else if (entity === 'Notebook') {
            return hasAuthorityForNotebookPermission(member, permission);
        } else if (entity === 'Experiment') {
            return hasAuthorityForExperimentPermission(member, permission);
        }
    }

    function isUserRemovableFromAccessList(member) {
        if (entity === 'Experiment' || !entityId) {
            return $q.resolve(true);
        }

        if (entity === 'Project') {
            return userRemovableFromProject.get({
                projectId: entityId,
                userId: member.user.id
            })
                .$promise
                .then(success);
        } else if (entity === 'Notebook') {
            return userRemovableFromNotebook.get({
                projectId: parentId,
                notebookId: entityId,
                userId: member.user.id
            })
                .$promise
                .then(success);
        }

        return $q.reject();
    }

    function success(result) {
        return result.isUserRemovable;
    }
}

module.exports = permissionManagementService;
