(function () {
    'use strict';

    angular
        .module('indigoeln')
        .factory('experimentTablesService', experimentTablesService);

    experimentTablesService.$inject = ['$resource'];

    function experimentTablesService($resource) {
        return $resource('service/experiments/tables');
    }

})();