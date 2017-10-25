angular.module('indigoeln')
    .factory('UserWithAuthority', function($resource) {
        return $resource('api/users/permission-management', {}, {
            query: {
                method: 'GET', isArray: true
            }
        });
    })
    .factory('UserRemovableFromProject', function($resource) {
        return $resource('api/projects/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    })
    .factory('UserRemovableFromNotebook', function($resource) {
        return $resource('api/notebooks/permissions/user-removable', {}, {
            get: {
                method: 'GET'
            }
        });
    })
    .factory('PermissionManagement', permissionManagement);

/* @ngInject */
function permissionManagement($q, Principal, UserRemovableFromProject, UserRemovableFromNotebook, permissionConstants) {
    var _accessList;
    var _author;
    var _entity;
    var _entityId;
    var _parentId;

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
        if (!_accessList) {
            return false;
        }

        var userId = Principal.getIdentity().id;

        return _.some(_accessList, function(item) {
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
        return _accessList;
    }

    function setAccessList(list) {
        _accessList = list;
    }

    function getAuthor() {
        return _author;
    }

    function setAuthor(user) {
        _author = user;
    }

    function getEntity() {
        return _entity;
    }

    function setEntity(entity) {
        _entity = entity;
    }

    function getEntityId() {
        return _entityId;
    }

    function setEntityId(entityId) {
        _entityId = entityId;
    }

    function getParentId() {
        return _parentId;
    }

    function setParentId(parentId) {
        _parentId = parentId;
    }

    function hasAuthorityForProjectPermission(member, permission) {
        var projectOwnerAuthoritySet = permissionConstants.PROJECT_OWNER_AUTHORITY_SET;
        var projectUserAuthoritySet = permissionConstants.PROJECT_USER_AUTHORITY_SET;
        var projectViewerAuthoritySet = permissionConstants.PROJECT_VIEWER_AUTHORITY_SET;

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
        var notebookOwnerAuthoritySet = permissionConstants.NOTEBOOK_OWNER_AUTHORITY_SET;
        var notebookUserAuthoritySet = permissionConstants.NOTEBOOK_USER_AUTHORITY_SET;
        var notebookViewerAuthoritySet = permissionConstants.NOTEBOOK_VIEWER_AUTHORITY_SET;

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
        var experimentOwnerAuthoritySet = permissionConstants.EXPERIMENT_OWNER_AUTHORITY_SET;
        var experimentViewerAuthoritySet = permissionConstants.EXPERIMENT_VIEWER_AUTHORITY_SET;

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
        if (_entity === 'Project') {
            return hasAuthorityForProjectPermission(member, permission);
        } else if (_entity === 'Notebook') {
            return hasAuthorityForNotebookPermission(member, permission);
        } else if (_entity === 'Experiment') {
            return hasAuthorityForExperimentPermission(member, permission);
        }
    }

    function isUserRemovableFromAccessList(member) {
        if (_entity === 'Experiment' || !_entityId) {
            return $q.resolve(true);
        }

        if (_entity === 'Project') {
            return UserRemovableFromProject.get({
                projectId: _entityId,
                userId: member.user.id
            })
                .$promise
                .then(success);
        } else if (_entity === 'Notebook') {
            return UserRemovableFromNotebook.get({
                projectId: _parentId,
                notebookId: _entityId,
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
