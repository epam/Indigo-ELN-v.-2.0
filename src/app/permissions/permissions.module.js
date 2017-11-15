var PermissionsController = require('./component/permissions.controller');
var PermissionViewController = require('./permissions-view/permission-view.controller');
var permissionsConstant = require('./permissions.constant');
var permissionsConfig = require('./component/permissions.constant');
var permissionViewConfig = require('./permissions-view/permission-view.constant');
var userRemovableFromNotebookService = require('./resources/user-removable-from-notebook.service');
var userRemovableFromProjectService = require('./resources/user-removable-from-project.service');
var userWithAuthorityService = require('./resources/user-with-authority.service');
var permissionService = require('./services/permissions.service');

module.exports = angular
    .module('indigoeln.permissions', [])

    .controller('PermissionsController', PermissionsController)
    .controller('PermissionViewController', PermissionViewController)

    .factory('userRemovableFromNotebookService', userRemovableFromNotebookService)
    .factory('userRemovableFromProjectService', userRemovableFromProjectService)
    .factory('userWithAuthorityService', userWithAuthorityService)
    .factory('permissionService', permissionService)

    .constant('permissionsConstant', permissionsConstant)
    .constant('permissionsConfig', permissionsConfig)
    .constant('permissionViewConfig', permissionViewConfig)

    .name;
