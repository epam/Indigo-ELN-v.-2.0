angular.module('indigoeln')
    .factory('RegistrationService', function ($resource) {
        return $resource('api/registration', {
            repositoryId: '@repositoryId'
        }, {
            'register': {url: 'api/registration/:repositoryId/register', method: 'POST'}
        });
    });


