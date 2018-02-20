require('./entity-tree.less');
var entityTree = require('./entity-tree.directive');
var simpleNode = require('./simple-node/simple-node.directive');

var entityTreeService = require('./entity-tree.service');
var entityTreeFactory = require('./entity-tree.factory');

var run = require('./entity-tree.run');
var appSidebar = require('../../../app-layout/app-sidebar/app-sidebar.module');

var dependencies = [
    appSidebar
];

module.exports = angular
    .module('indigoeln.entityTree', dependencies)

    .directive('entityTree', entityTree)
    .directive('simpleNode', simpleNode)

    .factory('entityTreeFactory', entityTreeFactory)
    .factory('entityTreeService', entityTreeService)

    .run(run)

    .name;

