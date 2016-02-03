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
                    authorities: ['CONTENT_EDITOR', 'PROJECT_READER'],
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