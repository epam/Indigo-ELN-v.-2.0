angular
    .module('indigoeln.entityTree')
    .directive('simpleNode', function() {
        return {
            replace: true,
            transclude: true,
            templateUrl: 'scripts/app/common/directives/entity-tree/simple-node/simple-node.html'
        };
    });
