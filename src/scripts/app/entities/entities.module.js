var entitiesConfig = require('./entities.config');

var entitiesControls = require('./entities-controls/entities-controls.module');
var experiment = require('./experiment/experiment.module');
var helpers = require('./helpers/helpers.module');
var notebook = require('./notebook/notebook.module');
var roleManagement = require('./role-management/role-management.module');
var search = require('./search/search.module');
var templateManagement = require('./template-management/template-management.module');

var dependencies = [
    entitiesControls,
    experiment,
    helpers,
    notebook,
    roleManagement,
    search,
    templateManagement
];

module.exports = angular
    .module('indigoeln.entities', dependencies)

    .config(entitiesConfig)

    .name;
