(function () {
    'use strict';

    angular.module('indigoeln').

        factory('experimentService', experimentService);

    experimentService.$inject = ['$resource'];

    function experimentService($resource) {
        return $resource('service/experiments');
    }

})();