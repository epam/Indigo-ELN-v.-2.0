angular.module('indigoeln')
    .factory('AccountRole', function ($resource) {
        return $resource('api/accounts/account/roles', {}, {
            'query': {method: 'GET', isArray: true}
        });
    });
