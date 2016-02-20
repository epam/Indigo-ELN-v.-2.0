'use strict';

angular.module('indigoeln')
    .factory('PermissionManagement', function ($q, Principal) {
        var _accessList;
        var _author;

        var VIEWER = ['READ_ENTITY'];
        var CHILD_VIEWER = ['READ_ENTITY', 'READ_SUB_ENTITY'];
        var USER = ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY'];
        var OWNER = ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY', 'UPDATE_ENTITY'];

        var projectPermissions = [
            { id: 'VIEWER', name: 'VIEWER (read project)'},
            { id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read project and notebooks)'},
            { id: 'USER', name: 'USER (read project and notebooks, create notebooks)'},
            { id: 'OWNER', name: 'OWNER (read and update project, read and create notebooks)'}
        ];
        var notebookPermissions = [
            { id: 'VIEWER', name: 'VIEWER (read notebook)'},
            { id: 'CHILD_VIEWER', name: 'CHILD_VIEWER (read notebook and experiments)'},
            { id: 'USER', name: 'USER (read notebook and experiments, create experiments)'},
            { id: 'OWNER', name: 'OWNER (read and update notebook, read and create experiments)'}
        ];
        var experimentPermissions = [
            { id: 'VIEWER', name: 'VIEWER (read experiment)'},
            { id: 'OWNER', name: 'OWNER (read and update experiment)'}
        ];

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
                    return false;
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
            getProjectPermissions: function() {
                return projectPermissions;
            },
            getNotebookPermissions: function() {
                return notebookPermissions;
            },
            getExperimentPermissions: function() {
                return experimentPermissions;
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