require('./component/role-management.less');

var roleManagementConfig = require('./role-management.config');
var RoleManagementController = require('./component/role-management.controller');
var RoleManagementSaveController = require('./save-dialog/role-management-save-dialog.controller');
var roleManagementUtils = require('./role-management-utils.service');

var editRole = require('./edit-role/edit-role.directive');

module.exports = angular
    .module('indigoeln.roleManagement', [])

    .controller('RoleManagementController', RoleManagementController)
    .controller('RoleManagementSaveController', RoleManagementSaveController)

    .factory('roleManagementUtils', roleManagementUtils)

    .directive('editRole', editRole)

    .config(roleManagementConfig)

    .name;
