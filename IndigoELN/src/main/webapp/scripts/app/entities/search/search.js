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
                    authorities: ['CONTENT_EDITOR'],
                    pageTitle: 'indigoeln',
                    tab: {
                        name: 'Search',
                        kind: 'search-panel',
                        type:'entity',
                        state: 'entities.search-panel'
                    }
                }/*,
                resolve: {

                }*/
            });
    });