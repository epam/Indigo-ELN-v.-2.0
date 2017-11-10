// region App styles
require('../../assets/less/indigo-bootstrap.less');
require('../../assets/less/main.less');
// endregion

var vendors = require('./dependencies/vendors');

// region App modules
var account = require('./account/account.module');
var appLayout = require('./app-layout/app-layout.module');
var common = require('./common/common.module');
var dictionaryManagement = require('./dictionary-management/dictionary-management.module');
var entities = require('./entities/entities.module');
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
    entities,
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
