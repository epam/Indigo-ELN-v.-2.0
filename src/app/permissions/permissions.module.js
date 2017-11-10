var PermissionManagementController = require('./component/permission-management.controller');
var PermissionViewManagementController = require('./permissions-view/permission-view.controller');
var permissionsConstants = require('./permissions.constant');
var permissionManagementConfig = require('./component/permission-management.constant');
var permissionViewManagementConfig = require('./permissions-view/permission-view.constant');
var userRemovableFromNotebook = require('./resources/user-removable-from-notebook.service');
var userRemovableFromProject = require('./resources/user-removable-from-project.service');
var userWithAuthority = require('./resources/user-with-authority.service');
var permissionManagementService = require('./services/permission-management.service');

module.exports = angular
    .module('indigoeln.permissions', [])

    .controller('PermissionManagementController', PermissionManagementController)
    .controller('PermissionViewManagementController', PermissionViewManagementController)

    .factory('userRemovableFromNotebook', userRemovableFromNotebook)
    .factory('userRemovableFromProject', userRemovableFromProject)
    .factory('userWithAuthority', userWithAuthority)
    .factory('permissionManagementService', permissionManagementService)

    .constant('permissionsConstants', permissionsConstants)
    .constant('permissionManagementConfig', permissionManagementConfig)
    .constant('permissionViewManagementConfig', permissionViewManagementConfig)

    .name;
