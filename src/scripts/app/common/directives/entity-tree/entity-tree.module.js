var entityTree = require('./entity-tree.directive');
var simpleNode = require('./simple-node/simple-node.directive');

module.exports = angular
    .module('indigoeln.entityTree', [])

    .directive('entityTree', entityTree)
    .directive('simpleNode', simpleNode)

    .run(function($rootScope, entityTreeService, principalService) {
        principalService.addUserChangeListener(entityTreeService.clearAll);
    })

    .name;

