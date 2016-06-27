angular.module('indigoeln')
    .factory('RegistrationService', function ($resource) {
        return $resource('api/registration', {
            repositoryId: '@repositoryId'
        }, {
            'info': {url: 'api/registration/info', method: 'GET', isArray: true},
            'register': {url: 'api/registration/:repositoryId/register', method: 'POST'}
        });
    });


