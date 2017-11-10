var EntitiesController = require('./entities.controller');
var indigoEntitiesControls = require('./indigo-entities-controls.directive');

module.exports = angular
    .module('indigoeln.entities.entitiesControls', [])

    .controller('EntitiesController', EntitiesController)

    .directive('indigoEntitiesControls', indigoEntitiesControls)

    .name;
