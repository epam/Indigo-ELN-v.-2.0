(function() {
    angular
        .module('indigoeln')
        .directive('indigoParsersFormatters', indigoParsersFormatters);

    function indigoParsersFormatters() {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {
                indigoParsersFormatters: '='
            },
            link: link
        };

        /* @ngInject */
        function link(scope, element, attrs, ngModel) {
            // model -> view
            _.each(scope.indigoParsersFormatters.indigoFormatters, function(i) {
                ngModel.$formatters.push(i);
            });
            // view -> model
            _.each(scope.indigoParsersFormatters.indigoParsers, function(i) {
                ngModel.$parsers.push(i);
            });
        }
    }
})();
