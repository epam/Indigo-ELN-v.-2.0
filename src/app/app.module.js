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

// region App styles
require('../assets/less/indigo-bootstrap.less');
require('../assets/less/main.less');
// endregion

var vendors = require('./dependencies/vendors');

// region App modules
var account = require('./account/account.module');
var appLayout = require('./app-layout/app-layout.module');
var common = require('./common/common.module');
var dictionaryManagement = require('./dictionary-management/dictionary-management.module');
var permissions = require('./permissions/permissions.module');
var project = require('./project/project.module');
var userManagement = require('./user-management/user-management.module');
var templateManagement = require('./template-management/template-management.module');
var roleManagement = require('./role-management/role-management.module');
var notebook = require('./notebook/notebook.module');
var experiment = require('./experiment/experiment.module');
var search = require('./search/search.module');
// endregion

var appRun = require('./app.run');
var appConfig = require('./app.config');

var dependencies = [
    vendors,
    account,
    appLayout,
    common,
    dictionaryManagement,
    permissions,
    project,
    userManagement,
    templateManagement,
    roleManagement,
    notebook,
    experiment,
    search
];

module.exports = angular
    .module('indigoeln', dependencies)

    .constant('apiUrl', apiUrl)

    .config(appConfig)
    .run(appRun)

    .name;
