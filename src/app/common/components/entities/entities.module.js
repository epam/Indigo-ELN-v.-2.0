var entitiesConfig = require('./entities.config');
var run = require('./entities-controls.run');

var entitiesControls = require('./entities-controls/entities-controls.module');

var dependencies = [
    entitiesControls
];

module.exports = angular
    .module('indigoeln.entities', dependencies)

    .config(entitiesConfig)
    .run(run)

    .name;
