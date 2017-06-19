angular.module('indigoeln')
    .factory('UserWithAuthority', function ($resource) {
        return $resource('api/users/permission-management', {}, {
            'query': {method: 'GET', isArray: true}
        });
    })
    .factory('UserRemovableFromProject', function ($resource) {
        return $resource('api/projects/permissions/user-removable', {}, {
            'get': {method: 'GET'}
        });
    })
    .factory('UserRemovableFromNotebook', function ($resource) {
        return $resource('api/notebooks/permissions/user-removable', {}, {
            'get': {method: 'GET'}
        });
    })
    .factory('PermissionManagement', permissionManagement);

/* @ngInject */
function permissionManagement($q, Principal, UserRemovableFromProject, UserRemovableFromNotebook) {
    var _accessList, _author, _entity, _entityId, _parentId;

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
        _.each(list, function (item) {
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

    function hasPermission(permission, accessList) {
        if (!accessList && !_accessList) {
            return $q.when(false);
        }
        var list = accessList ? accessList : _accessList;
        return Principal.identity().then(function (identity) {
            var hasPermission = false;
            _.each(list, function (item) {
                if (item.user.id === identity.id && _.contains(item.permissions, permission)) {
                    hasPermission = true;
                }
            });
            return hasPermission;
        }, function () {
            return false;
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
        } else {
            return [experimentCreatorPermissions];
        }
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
        var projectOwnerAuthoritySet = ['PROJECT_READER', 'PROJECT_CREATOR', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'];
        var projectUserAuthoritySet = ['PROJECT_READER', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'];
        var projectViewerAuthoritySet = ['PROJECT_READER'];

        if (permission === 'OWNER') {
            return _.every(projectOwnerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        } else if (permission === 'USER') {
            return _.every(projectUserAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(projectViewerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        }
    }

    function hasAuthorityForNotebookPermission(member, permission) {
        var notebookOwnerAuthoritySet = ['NOTEBOOK_READER', 'NOTEBOOK_CREATOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
        var notebookUserAuthoritySet = ['NOTEBOOK_READER', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
        var notebookViewerAuthoritySet = ['NOTEBOOK_READER'];

        if (permission === 'OWNER') {
            return _.every(notebookOwnerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        } else if (permission === 'USER') {
            return _.every(notebookUserAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(notebookViewerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        }
    }


    function hasAuthorityForExperimentPermission(member, permission) {
        var experimentOwnerAuthoritySet = ['EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
        var experimentViewerAuthoritySet = ['EXPERIMENT_READER'];

        if (permission === 'OWNER') {
            return _.every(experimentOwnerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
            });
        } else if (permission === 'VIEWER') {
            return _.every(experimentViewerAuthoritySet, function (authority) {
                return _.contains(member.user.authorities, authority);
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
        var agent, params;
        var deferred = $q.defer();
        if (_entity === 'Experiment' || !_entityId) {
            deferred.resolve(true);
            return deferred.promise;
        }
        if (_entity === 'Project') {
            agent = UserRemovableFromProject;
            params = {projectId: _entityId, userId: member.user.id};
        } else if (_entity === 'Notebook') {
            agent = UserRemovableFromNotebook;
            params = {projectId: _parentId, notebookId: _entityId, userId: member.user.id};
        }
        if (agent) {
            agent.get(params).$promise.then(function (result) {
                deferred.resolve(result.isUserRemovable);
            });
        }
        return deferred.promise;
    }
}
