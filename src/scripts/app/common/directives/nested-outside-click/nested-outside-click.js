nestedOutsideClick.$inject = ['$parse', '$document', '$timeout'];

function nestedOutsideClick($parse, $document, $timeout) {
    return {
        link: function($scope, $element, attr) {
            var expression = $parse(attr.nestedOutsideClick);
            $timeout(function() {
                $document.on('click', handler);
            });

            $scope.$on('$destroy', function() {
                $document.off('click', handler);
            });

            function handler(event) {
                if ($element[0] !== event.target && !includes($element[0].children, event.target)) {
                    $scope.$apply(function() {
                        expression($scope, {$event: event});
                    });
                }
            }
        }
    };

    function includes(children, target) {
        return _.some(children, function(child) {
            return child === target || (child.children && child.children.length && includes(child.children, target));
        });
    }
}

module.exports = nestedOutsideClick;
