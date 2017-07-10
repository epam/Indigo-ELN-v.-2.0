(function() {
    angular
        .module('indigoeln')
        .directive('indigoComponent', indigoComponent);

    /* @ngInject */
    function indigoComponent($compile) {
        return {
            restrict: 'E',
            // replace: true,
            scope: {
                componentId: '@',
                indigoModel: '=',
                indigoExperiment: '=',
                indigoReadonly: '=',
                indigoExperimentForm: '=',
                indigoShare: '=',
                indigoSaveExperimentFn: '&'
            },
            link: link,
            controller: indigoComponentController,
            controllerAs: 'vm'
        };

        function link($scope, $element) {
            // for capability
            $scope.model = $scope.indigoModel;
            // for readonly
            $scope.experiment = _.extend({}, $scope.indigoExperiment);
            // for communication between components
            $scope.share = $scope.indigoShare;

            compileTemplate();

            function compileTemplate() {
                var id = $scope.componentId;
                var compileElement = getTemplate(id);
                $element.append(compileElement);
                $compile(compileElement)($scope);
            }
        }

        /* @ngInject */
        function indigoComponentController($rootScope) {
            var vm = this;

            init();

            function init() {
                vm.compoundSummarySelectedRow = compoundSummarySelectedRow;
            }

            function compoundSummarySelectedRow(row) {
                vm.share.selectedRow = row || null;
                if (row) {
                    var data = {
                        row: row
                    };
                    $rootScope.$broadcast('batch-summary-row-selected', data);
                } else {
                    $rootScope.$broadcast('batch-summary-row-deselected');
                }
            }
        }

        function getTemplate(id) {
            var directiveName = 'indigo-' + id;

            return angular.element('<' + directiveName +
                ' model="indigoModel"' +
                ' experiment="experiment"' +
                ' share="indigoShare"' +
                ' readonly="indigoReadonly"' +
                ' experiment-form="indigoExperimentForm"' +
                ' indigo-share="indigoShare"' +
                ' indigo-save-experiment-fn="indigoSaveExperimentFn">' +
                '</' + directiveName + '>');
        }
    }
})();
