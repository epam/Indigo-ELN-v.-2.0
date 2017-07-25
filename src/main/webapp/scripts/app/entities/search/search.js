angular.module('indigoeln')
    .config(function($stateProvider) {
        $stateProvider
            .state('entities.search-panel', {
                url: '/search',
                params: {query: null},
                views: {
                    tabContent: {
                        templateUrl: 'scripts/app/entities/search/search-panel.html',
                        controller: 'SearchPanelController',
                        controllerAs: 'vm'
                    }
                },
                data: {
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Search',
                        kind: 'search-panel',
                        type: 'entity',
                        state: 'entities.search-panel'
                    }
                },
                resolve: {
                    pageInfo: function($q, Principal, Users) {
                        var deferred = $q.defer();
                        $q.all([
                            Users.get(),
                            Principal.identity()
                        ]).then(function(results) {
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
