'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('home', {
                parent: 'sidebar',
                url: '/',
                data: {
                    authorities: []
                },
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/main/main.html',
                        controller: 'MainController'
                    }
                },
                resolve: {
                    
                }
            });
    });