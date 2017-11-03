angular.module('indigoeln')
    .factory('userService', userService);

userService.$inject = ['$resource', 'apiUrl'];

function userService($resource, apiUrl) {
    function transformRequest(user) {
        if (_.isObject(user.group)) {
            user.group = user.group.name;
        }

        return angular.toJson(user);
    }

    return $resource(apiUrl + 'users/:login', {}, {
        query: {
            method: 'GET', isArray: true
        },
        get: {
            method: 'GET',
            transformResponse: angular.fromJson
        },
        save: {
            method: 'POST',
            transformRequest: transformRequest
        },
        update: {
            method: 'PUT',
            transformRequest: transformRequest
        },
        delete: {
            method: 'DELETE'
        }
    });
}
