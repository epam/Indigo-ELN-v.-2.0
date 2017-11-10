detectRendered.$inject = ['$parse'];

function detectRendered($parse) {
    return {
        link: function($scope, $element, $attr) {
            var expression = $parse($attr.detectRendered);
            angular.getTestability($element).whenStable(function() {
                expression($scope, {});
            });
        }
    };
}

module.exports = detectRendered;
