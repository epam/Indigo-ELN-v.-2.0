run.$inject = ['$rootScope', 'entitiesBrowser'];

function run($rootScope, entitiesBrowser) {
    var unsubscribe = $rootScope.$on('$stateChangeSuccess', function(event, toState, toStateParams) {
        var tab = angular.copy(toState.data.tab);

        if (tab) {
            tab.params = toStateParams;
            if (tab.type && tab.type === 'entity') {
                entitiesBrowser.addTab(tab);
            }
        }
    });

    $rootScope.$on('$destroy', unsubscribe);
}

module.exports = run;
