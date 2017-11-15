var entitiesConfig = require('./entities.config');
var run = require('./entities.run');

var indigoEntitiesControls = require('./indigo-entities-controls/indigo-entities-controls.directive');
var entities = require('./entities/entities.directive');

var dependencies = [];

module.exports = angular
    .module('indigoeln.entities', dependencies)

    .directive('indigoEntitiesControls', indigoEntitiesControls)
    .directive('entities', entities)

    .config(entitiesConfig)
    .run(run)

    .name;
