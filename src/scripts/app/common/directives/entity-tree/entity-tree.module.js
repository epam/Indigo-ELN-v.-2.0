var entityTree = require('./entity-tree.directive');
var simpleNode = require('./simple-node/simple-node.directive');

var entityTreeService = require('./entity-tree.service');
var run = require('./entity-tree.run');

var sidebar = require('../../../sidebar/sidebar.module');

var dependencies = [
    sidebar
];

module.exports = angular
    .module('indigoeln.entityTree', dependencies)

    .directive('entityTree', entityTree)
    .directive('simpleNode', simpleNode)

    .factory('entityTreeService', entityTreeService)

    .run(run)

    .name;

