'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('tab', {
                abstract: true,
                parent: 'entity',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/tabs/tab-manager.html',
                        controller: 'TabManagerController'
                    }
                },
                resolve: {
                    data: function () {
                    }
                }
            });
    });
