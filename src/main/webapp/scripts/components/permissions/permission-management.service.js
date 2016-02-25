'use strict';

angular.module('indigoeln')
    .factory('UserWithAuthority', function ($resource) {
        return $resource('api/users/permission-management', {}, {
            'query': {method: 'GET', isArray: true}
        });
    })
    .factory('PermissionManagement', function ($q, Principal) {
        var _accessList, _author, _entity;

        var VIEWER = ['READ_ENTITY'];
        var CHILD_VIEWER = ['READ_ENTITY', 'READ_SUB_ENTITY'];
        var USER = ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY'];
        var OWNER = ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY', 'UPDATE_ENTITY'];

        return {
            expandPermission: function(list) {
                _.each(list, function(item) {
                    if (item.permissionView === 'OWNER') {
                        item.permissions = OWNER;
                    } else if (item.permissionView === 'CHILD_VIEWER') {
                        item.permissions = CHILD_VIEWER;
                    } else if (item.permissionView === 'USER') {
                        item.permissions = USER;
                    } else {
                        item.permissions = VIEWER;
                    }
                });
                return list;
            },
            hasPermission: function(permission, accessList) {
                if (!accessList && !_accessList) {
                    return $q.when(false);
                }
                var list = accessList ? accessList : _accessList;
                return Principal.identity().then(function (identity) {
                    var hasPermission = false;
                    _.each(list, function(item) {
                        if (item.user.id === identity.id && _.contains(item.permissions, permission)) {
                            hasPermission = true;
                        }
                    });
                    return hasPermission;
                }, function (err) {
                    return false;
                });
            },
            getAuthorAccessList: function(author) {
                return [{
                    user: author,
                    permissions: ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY', 'UPDATE_ENTITY'],
                    permissionView: 'OWNER'
                }];
            },
            getAccessList: function() {
                return _accessList;
            },
            setAccessList: function(list) {
                _accessList = list;
            },
            getAuthor: function() {
                return _author;
            },
            setAuthor: function(user) {
                _author = user;
            },
            getEntity: function() {
                return _entity;
            },
            setEntity: function(entity) {
                _entity = entity;
            },
            hasAuthorityForProjectPermission: function(member, permission) {
                var projectOwnerAuthoritySet = ['PROJECT_READER', 'PROJECT_CREATOR', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'];
                var projectUserAuthoritySet = ['PROJECT_READER', 'NOTEBOOK_READER', 'NOTEBOOK_CREATOR'];
                var projectChildViewerAuthoritySet = ['PROJECT_READER', 'NOTEBOOK_READER'];
                var projectViewerAuthoritySet = ['PROJECT_READER'];

                if (permission === 'OWNER') {
                    return _.every(projectOwnerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'USER') {
                    return _.every(projectUserAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'CHILD_VIEWER') {
                    return _.every(projectChildViewerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'VIEWER') {
                    return _.every(projectViewerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                }
            },
            hasAuthorityForNotebookPermission: function(member, permission) {
                var notebookOwnerAuthoritySet = ['NOTEBOOK_READER', 'NOTEBOOK_CREATOR', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
                var notebookUserAuthoritySet = ['NOTEBOOK_READER', 'EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
                var notebookChildViewerAuthoritySet = ['NOTEBOOK_READER', 'EXPERIMENT_READER'];
                var notebookViewerAuthoritySet = ['NOTEBOOK_READER'];

                if (permission === 'OWNER') {
                    return _.every(notebookOwnerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'USER') {
                    return _.every(notebookUserAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'CHILD_VIEWER') {
                    return _.every(notebookChildViewerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'VIEWER') {
                    return _.every(notebookViewerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                }
            },
            hasAuthorityForExperimentPermission: function(member, permission) {
                var experimentOwnerAuthoritySet = ['EXPERIMENT_READER', 'EXPERIMENT_CREATOR'];
                var experimentViewerAuthoritySet = ['EXPERIMENT_READER'];

                if (permission === 'OWNER') {
                    return _.every(experimentOwnerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                } else if (permission === 'VIEWER') {
                    return _.every(experimentViewerAuthoritySet, function(authority) {
                        return _.contains(member.user.authorities, authority);
                    });
                }
            }
        };
    });














































//
//
//return {
//    get: function(entity) {
//        if (entity.accessList) {
//            return entity.accessList;
//        } else return [];
//    },
//    set: function(list, entity) {
//        if (entity.accessList) {
//            entity.accessList = list;
//        }
//    }
//};