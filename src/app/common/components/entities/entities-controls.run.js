/* eslint angular/on-watch: off*/
run.$inject = ['$rootScope', 'entitiesBrowser'];

function run($rootScope, entitiesBrowser) {
    $rootScope.$on('$stateChangeSuccess', function(event, toState, toStateParams) {
        var tab = angular.copy(toState.data.tab);

        if (tab) {
            tab.params = toStateParams;
            if (tab.type && tab.type === 'entity') {
                entitiesBrowser.addTab(tab);
            }
        }
    });
}

module.exports = run;
