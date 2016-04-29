angular
    .module('indigoeln')
    .factory('Notebook', function ($resource) {
        return $resource('api/projects/:projectId/notebooks/:notebookId', {
            projectId: '@projectId'
        }, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': {method: 'POST'},
            'update': {method: 'PUT'},
            'delete': {method: 'DELETE'}
        });
    });
