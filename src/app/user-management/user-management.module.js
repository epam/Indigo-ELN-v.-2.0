var UserManagementController = require('./component/user-management.controller');
var UserManagementDeleteController = require('./delete-dialog/user-management-delete-dialog.controller');
var userManagementConfig = require('./user-management.config');

module.exports = angular
    .module('indigoeln.userManagement', [])

    .controller('UserManagementController', UserManagementController)
    .controller('UserManagementDeleteController', UserManagementDeleteController)

    .config(userManagementConfig)

    .name;
