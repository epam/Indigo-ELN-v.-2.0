// region App styles
require('../../assets/less/indigo-bootstrap.less');
require('../../assets/less/main.less');
// endregion

var vendors = require('./dependencies/vendors');

// region App modules
var account = require('./account/account.module');
var appLayout = require('./app-layout/app-layout.module');
var appNavbar = require('./app-navbar/app-navbar.module');
var common = require('./common/common.module');
var dictionaryManagement = require('./dictionary-management/dictionary-management.module');
var entities = require('./entities/entities.module');
var permissions = require('./permissions/permissions.module');
var project = require('./project/project.module');
var sidebar = require('./sidebar/sidebar.module');
var userManagement = require('./user-management/user-management.module');
// endregion

var appRun = require('./app.run');
var appConfig = require('./app.config');

var dependencies = [
    account,
    appLayout,
    appNavbar,
    common,
    dictionaryManagement,
    entities,
    permissions,
    project,
    sidebar,
    userManagement
];

module.exports = angular
    .module('indigoeln', _.concat(vendors, dependencies))

    .run(appRun)
    .config(appConfig)

    .constant('apiUrl', apiUrl)

    .name;
