angular.module('indigoeln')
    .factory('User', function($resource) {
        function transformRequest(user) {
            if (_.isObject(user.group)) {
                user.group = user.group.name;
            }
        }

        return $resource('api/users/:login', {}, {
            query: {
                method: 'GET', isArray: true
            },
            get: {
                method: 'GET',
                transformResponse: function(data) {
                    data = JSON.parse(data);

                    return data;
                }
            },
            save: {
                method: 'POST',
                transformRequest: function(data) {
                    transformRequest(data);

                    return JSON.stringify(data);
                }
            },
            update: {
                method: 'PUT',
                transformRequest: function(data) {
                    transformRequest(data);

                    return JSON.stringify(data);
                }
            },
            delete: {
                method: 'DELETE'
            }
        });
    });
