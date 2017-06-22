(function () {
    angular
        .module('indigoeln')
        .directive('indigoAutorecovery', indigoAutorecovery);

    function indigoAutorecovery() {
        return {
            restrict: 'E',
            scope: {
                indigoRestored: '='
            },
            templateUrl: 'scripts/app/entities/autorecovery/autorecovery.html'
        };
    }
})();