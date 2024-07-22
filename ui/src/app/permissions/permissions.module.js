/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var PermissionsController = require('./component/permissions.controller');
var PermissionViewController = require('./permissions-view/permission-view.controller');
var permissionsConstant = require('./permissions.constant');
var permissionRolesConstant = require('./permission-roles.json');
var permissionsConfig = require('./component/permissions.constant');
var permissionViewConfig = require('./permissions-view/permission-view.constant');
var userRemovableFromNotebookService = require('./resources/user-removable-from-notebook.service');
var userRemovableFromProjectService = require('./resources/user-removable-from-project.service');
var userWithAuthorityService = require('./resources/user-with-authority.service');
var permissionService = require('./services/permission.service');
var permissionModal = require('./services/permission-modal.service');

module.exports = angular
    .module('indigoeln.permissions', [])

    .controller('PermissionsController', PermissionsController)
    .controller('PermissionViewController', PermissionViewController)

    .factory('userRemovableFromNotebookService', userRemovableFromNotebookService)
    .factory('userRemovableFromProjectService', userRemovableFromProjectService)
    .factory('userWithAuthorityService', userWithAuthorityService)
    .factory('permissionService', permissionService)
    .factory('permissionModal', permissionModal)

    .constant('permissionsConstant', permissionsConstant)
    .constant('permissionsConfig', permissionsConfig)
    .constant('permissionViewConfig', permissionViewConfig)
    .constant('permissionRoles', permissionRolesConstant)

    .name;
