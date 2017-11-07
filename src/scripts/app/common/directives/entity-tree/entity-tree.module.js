var entityTree = require('./entity-tree.directive');
var entityTreeService = require('./entity-tree.service');
var simpleNode = require('./simple-node/simple-node.directive');

var sidebar = require('../../../sidebar/sidebar.module');

var dependencies = [
    sidebar
];

module.exports = angular
    .module('indigoeln.entityTree', dependencies)

    .directive('entityTree', entityTree)
    .directive('simpleNode', simpleNode)

    .factory('entityTreeService', entityTreeService)

    .run(function($rootScope, entityTreeService, principalService) {
        principalService.addUserChangeListener(entityTreeService.clearAll);
    })

    .name;

