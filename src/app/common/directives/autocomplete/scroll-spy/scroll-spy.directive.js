function scrollSpy() {
    return {
        restrict: 'A',
        link: function($scope, $element, $attrs) {
            var raw = $element[0];

            $element.bind('scroll', function() {
                // Calls function when element is scrolled to the bottom
                var isScrolledToBottom = raw.scrollTop + raw.offsetHeight >= raw.scrollHeight;

                if (isScrolledToBottom && $attrs.scrollSpy) {
                    $scope.$apply($attrs.scrollSpy);
                }
            });

            $scope.$on('$destroy', function() {
                $element.unbind('scroll');
            });
        }
    };
}

module.exports = scrollSpy;
