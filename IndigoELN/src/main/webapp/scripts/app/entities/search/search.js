angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('entities.search-panel', {
                url: '/search',
                views: {
                    'tabContent': {
                        templateUrl: 'scripts/app/entities/search/search-panel.html',
                        controller: 'SearchPanelController'
                    }
                },
                data: {
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Search',
                        kind: 'search-panel',
                        type:'entity',
                        state: 'entities.search-panel'
                    }
                },
                resolve: {
                    pageInfo: function($q, Principal, UserWithAuthority) {
                        var deferred = $q.defer();
                        $q.all([
                            UserWithAuthority.query().$promise,
                            Principal.identity()
                        ]).then(function(results){
                            deferred.resolve({
                                users: results[0],
                                identity: results[1]
                            });
                        });
                        return deferred.promise;
                    }
                }
            });
    });