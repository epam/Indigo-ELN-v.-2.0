(function () {
    angular
        .module('indigoeln')
        .directive('indigoReactionScheme', indigoReactionScheme);

    function indigoReactionScheme() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/components/entities/template/components/reactionScheme/reactionScheme.html'
        };
    }
})();