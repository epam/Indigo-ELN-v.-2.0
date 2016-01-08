(function () {
    'use strict';

    angular
        .module('indigoeln')
        .directive('experimentTabs', experimentTabs);

    experimentTabs.$inject = ['$timeout', '$filter'];

    function experimentTabs($timeout, $filter) {
        return {
            scope: {},
            restrict: 'E',
            templateUrl: 'scripts/app/entities/experiment/tabs/experiment-tabs.html',
            link: function (scope, elem, attrs) {
                scope.tabs = [];
                scope.newExperiment = newExperiment;

                scope.removeTab = removeTab;

                scope.$on('experiment-open', function (event, data) {
                    var openedTab = $filter('filter')(scope.tabs, {experiment: data.experiment});//-1;//scope.tabs.indexOf(data.experiment);

                    if (openedTab.length > 0) {
                        openedTab[0].active = true;
                    } else {
                        scope.tabs.push({
                            experiment: data.experiment,
                            active: true,
                            content: 'scripts/app/entities/experiment/tabs/experiment-tab.html',
                            name: data.experiment.title
                        });
                    }

                });

                scope.$on('new-experiment', newExperiment);

                function newExperiment() {
                    scope.tabs.push({
                        active: true,
                        content: 'scripts/app/entities/experiment/tabs/new-experiment-tab.html',
                        name: 'New experiment'
                    });
                }

                function removeTab(tab) {
                    var indx = scope.tabs.indexOf(tab);

                    scope.tabs.splice(indx, 1);
                }
            }
        };
    }

})();