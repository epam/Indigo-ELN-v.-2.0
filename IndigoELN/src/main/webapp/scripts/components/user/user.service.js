angular.module('indigoeln')
    .factory('User', function ($resource) {
        function transformRequest(data) {
            if (data.user.group) {
                data.user.group = data.user.group.name;
            }
        }
        return $resource('api/users/:login', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    transformRequest(data);
                    return angular.toJson(data);
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    transformRequest(data);
                    return angular.toJson(data);
                }
            },
            'delete': {method: 'DELETE'}
        });
    });
