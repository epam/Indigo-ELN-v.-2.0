/* eslint angular/on-watch: off*/
run.$inject = ['$rootScope', 'entitiesBrowserService'];

function run($rootScope, entitiesBrowserService) {
    $rootScope.$on('$stateChangeSuccess', function(event, toState, toStateParams) {
        var tab = angular.copy(toState.data.tab);

        if (tab) {
            tab.params = toStateParams;
            if (tab.type && tab.type === 'entity') {
                entitiesBrowserService.addTab(tab);
            }
        }
    });
}

module.exports = run;
