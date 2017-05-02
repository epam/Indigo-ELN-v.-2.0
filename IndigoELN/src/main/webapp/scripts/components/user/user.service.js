angular.module('indigoeln')
    .factory('User', function ($resource) {
        function transformRequest(user) {
            if (_.isObject(user.group)) {
                user.group = user.group.name;
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
    }).factory('Users', function($q, Dictionary) {
        var deferred;
        return {
            get: function(force) {
                if (!deferred || force) {
                    deferred = Dictionary.get({ id: 'users' });
                } 
                return deferred.$promise;
            }
        }
    });
