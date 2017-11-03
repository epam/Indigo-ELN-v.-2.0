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
                    pageInfo: function($q, principalService, usersService) {
                        return $q.all([
                            usersService.get(),
                            principalService.identity()
                        ]).then(function(results) {
                            return {
                                users: results[0],
                                identity: results[1]
                            };
                        });

                    }
                }
            });
    });
