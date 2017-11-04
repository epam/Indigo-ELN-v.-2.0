var roleManagementConfig = require('./role-management.config');
var RoleManagementController = require('./role-management.controller');
var RoleManagementDeleteController = require('./role-management-delete-dialog.controller');
var RoleManagementSaveController = require('./role-management-save-dialog.controller');


module.exports = angular
    .module('indigoeln.entities.roleManagement', [])

    .controller('RoleManagementController', RoleManagementController)
    .controller('RoleManagementDeleteController', RoleManagementDeleteController)
    .controller('RoleManagementSaveController', RoleManagementSaveController)

    .config(roleManagementConfig)

    .name;
