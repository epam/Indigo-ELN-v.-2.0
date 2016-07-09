angular.module('indigoeln')
    .factory('Project', function ($resource) {
        return $resource('api/projects/:projectId', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': {method: 'POST'},
            'update': {
                method: 'PUT',
                url: 'api/projects'
            },
            'delete': {method: 'DELETE'}
        });
    });