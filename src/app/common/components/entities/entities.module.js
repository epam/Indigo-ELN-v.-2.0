var entitiesConfig = require('./entities.config');
var run = require('./entities-controls.run');

var indigoEntitiesControls = require('./indigo-entities-controls/indigo-entities-controls.directive');
var entities = require('./entities/entities.directive');

var dependencies = [];

module.exports = angular
    .module('indigoeln.entities', dependencies)

    .config(entitiesConfig)
    .run(run)

    .directive('indigoEntitiesControls', indigoEntitiesControls)
    .directive('entities', entities)

    .name;
