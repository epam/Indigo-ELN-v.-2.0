angular.module('indigoeln')
    .config(function($stateProvider) {
        $stateProvider
            .state('error', {
                parent: 'app_page',
                url: '/error',
                data: {
                    authorities: [],
                    pageTitle: 'Error page!'
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/error/error.html'
                    }
                },
                resolve: {}
            })
            .state('accessdenied', {
                parent: 'app_page',
                url: '/accessdenied',
                data: {
                    authorities: []
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/error/accessdenied.html'
                    }
                },
                resolve: {}
            });
    });
