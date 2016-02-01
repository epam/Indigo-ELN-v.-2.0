'use strict';

angular.module('indigoeln')
    .config(function ($stateProvider) {
        $stateProvider
            .state('project', {
                parent: 'entity',
                url: '/projects/{id}',
                views: {
                    'content@app_page': {
                        templateUrl: 'scripts/app/entities/project/project-detail.html',
                        controller: 'ProjectDetailController'
                    }
                },
                data: {
                    authorities: ['ROLE_ADMIN', 'ROLE_USER'],
                    pageTitle: 'indigoeln'
                },
                resolve: {
                    project: function($stateParams, Project) {
                        return Project.get({id : $stateParams.id}).$promise;
                    },
                    users: function(User) {
                        return User.query().$promise;
                    }
                }
            });
    });