function scrollSpy() {
    return {
        restrict: 'A',
        link: function($scope, $element, $attrs) {
            var raw = $element[0];

            $element.bind('scroll', function() {
                // Calls function when element is scrolled to the bottom
                if (raw.scrollTop + raw.offsetHeight >= raw.scrollHeight) {
                    if ($attrs.scrollSpy) {
                        $scope.$apply($attrs.scrollSpy);
                    }
                }
            });
        }
    };
}

module.exports = scrollSpy;
