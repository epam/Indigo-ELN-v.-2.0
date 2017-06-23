(function () {
    angular
        .module('indigoeln')
        .directive('noDirty', noDirty);

    function noDirty() {
        return {
            require: 'ngModel',
            link: link
        };

        /* @ngInject */
        function link(scope, element, attrs, ngModelCtrl) {
            // override the $setDirty method on ngModelController
            if (scope.noDirty) {
                ngModelCtrl.$setDirty = angular.noop;
            }
        }
    }
})();
