'use strict';

angular.module('indigoeln')
    .factory('PermissionManagement', function () {
        var accessList = [];
        var author = {};

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
                    var permissions = item.permissions;
                    if (permissions === 'OWNER') {
                        item.permissions = OWNER;
                    } else if (permissions === 'CHILD_VIEWER') {
                        item.permissions = CHILD_VIEWER;
                    } else if (permissions === 'USER') {
                        item.permissions = USER;
                    } else {
                        item.permissions = VIEWER;
                    }
                });
                return list;
            },
            collapsePermission: function(list) {
                _.each(list, function(item) {
                    var permissions = item.permissions;
                    if (permissions.length === OWNER.length && _.isEqual(permissions, OWNER)) {
                        item.permissions = 'OWNER';
                    } else if (permissions.length === USER.length && _.isEqual(permissions, USER)) {
                        item.permissions = 'USER';
                    } else if (permissions.length === CHILD_VIEWER.length && _.isEqual(permissions, CHILD_VIEWER)) {
                        item.permissions = 'CHILD_VIEWER';
                    } else {
                        item.permissions = 'VIEWER';
                    }
                });
                return list;
            },
            getAccessList: function() {
                return accessList;
            },
            setAccessList: function(list) {
                accessList = list;
            },
            getAuthor: function() {
                return author;
            },
            setAuthor: function(user) {
                author = user;
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