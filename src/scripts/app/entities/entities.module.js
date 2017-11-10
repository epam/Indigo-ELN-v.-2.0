var entitiesConfig = require('./entities.config');

var entitiesControls = require('./entities-controls/entities-controls.module');
var helpers = require('./helpers/helpers.module');

var dependencies = [
    entitiesControls,
    helpers
];

module.exports = angular
    .module('indigoeln.entities', dependencies)

    .config(entitiesConfig)

    .name;
