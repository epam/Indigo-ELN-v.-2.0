var roleManagementConfig = require('./role-management.config');
var RoleManagementController = require('./component/role-management.controller');
var RoleManagementDeleteController = require('./delete-dialog/role-management-delete-dialog.controller');
var RoleManagementSaveController = require('./save-dialog/role-management-save-dialog.controller');


module.exports = angular
    .module('indigoeln.roleManagement', [])

    .controller('RoleManagementController', RoleManagementController)
    .controller('RoleManagementDeleteController', RoleManagementDeleteController)
    .controller('RoleManagementSaveController', RoleManagementSaveController)

    .config(roleManagementConfig)

    .name;
