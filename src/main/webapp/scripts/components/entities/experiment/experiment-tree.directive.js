(function () {
    'use strict';

    angular.module('indigoeln').directive('experimentTree', experimentTree);

    experimentTree.$inject = ['experimentService', '$rootScope'];

    function experimentTree(experimentService, $rootScope) {
        return {
            scope: {},
            restrict: 'E',
            templateUrl: 'scripts/app/entities/experiment/tree/experiment-tree.html',
            link: function (scope, elem, attrs) {
                scope.data = [{
                    name: 'Notebook 1',
                    type: 'root',
                    nodes: experimentService.query()
                }];

                scope.open = function (data) {
                    $rootScope.$broadcast('experiment-open', {experiment: data})
                };

                scope.$on('created-experiment', function (event, data) {
                    scope.data[0].nodes.push(data.experiment);
                });

                scope.$on('created-notebook', function (event, data) {
                    var notebook = data.notebook;
                    notebook.type = 'root';

                    scope.data.push(notebook);
                });
            }
        }
    }

})();