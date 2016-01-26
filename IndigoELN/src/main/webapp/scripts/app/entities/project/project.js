'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/project/{id}',
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/entities/project/project-detail.html',
                        controller: 'ProjectDetailController'
                    }
                },
                data: {
                    authorities: ['ROLE_ADMIN', 'ROLE_USER'],
                    pageTitle: 'indigoeln'
                },
                bindToController: true,
                resolve: {
                    data: ['$stateParams', 'Project', function($stateParams, Project) {
                        return Project.get({id : $stateParams.id});
                    }]
                }
            });
    });