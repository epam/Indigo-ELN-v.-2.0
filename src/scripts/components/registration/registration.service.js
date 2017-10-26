angular.module('indigoeln')
    .factory('RegistrationService', function($resource, apiUrl) {
        return $resource(apiUrl + 'registration', {
            repositoryId: '@repositoryId',
            compoundNo: '@compoundNo'
        }, {
            info: {
                url: apiUrl + 'registration/info', method: 'GET', isArray: true
            },
            register: {
                url: apiUrl + 'registration/:repositoryId/register', method: 'POST'
            },
            compounds: {
                url: apiUrl + 'registration/compounds/:compoundNo', method: 'GET', isArray: true
            }
        });
    });

