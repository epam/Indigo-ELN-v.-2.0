(function() {
    angular
        .module('indigoeln')
        .directive('nestedBlur', nestedBlur);

    function nestedBlur($parse) {
        return {
            compile: function($element, attr) {
                var expression = $parse(attr.nestedBlur);

                return function(scope, element) {
                    element.on('focusout', function(event) {
                        scope.$apply(function() {
                            expression(scope, {$event: event});
                        });
                    });
                };
            }
        };
    }
})();
