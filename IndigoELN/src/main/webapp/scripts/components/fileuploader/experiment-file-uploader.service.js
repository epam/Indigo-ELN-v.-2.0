angular
    .module('indigoeln')
    .factory('ExperimentFileUploaderService', experimentFileUploaderService);

/* @ngInject */
function experimentFileUploaderService($resource){
    return $resource('api/experiment_files/:id', {}, {
        'query': {method: 'GET', isArray: true},
        'get': {
            method: 'GET',
            transformResponse: function (data) {
                data = angular.fromJson(data);
                return data;
            }
        },
        'save': {method: 'POST'},
        'update': {method: 'PUT'},
        'delete': {method: 'DELETE'}
    });
}