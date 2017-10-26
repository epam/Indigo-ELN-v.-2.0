angular.module('indigoeln')
    .factory('User', function($resource, apiUrl) {
        function transformRequest(user) {
            if (_.isObject(user.group)) {
                user.group = user.group.name;
            }
        }

        return $resource(apiUrl + 'users/:login', {}, {
            query: {
                method: 'GET', isArray: true
            },
            get: {
                method: 'GET',
                transformResponse: function(data) {
                    data = angular.fromJson(data);

                    return data;
                }
            },
            save: {
                method: 'POST',
                transformRequest: function(data) {
                    transformRequest(data);

                    return angular.toJson(data);
                }
            },
            update: {
                method: 'PUT',
                transformRequest: function(data) {
                    transformRequest(data);

                    return angular.toJson(data);
                }
            },
            delete: {
                method: 'DELETE'
            }
        });
    });
