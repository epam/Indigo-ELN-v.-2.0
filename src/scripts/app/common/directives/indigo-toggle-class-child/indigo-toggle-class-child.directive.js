function indigoToggleClassChild() {
    return {
        restrict: 'A',
        link: link
    };

    /* @ngInject */
    function link($scope, $element, $attrs) {
        var toggledClass = $attrs.indigoToggleClassChild;
        $element.bind('click', function(e) {
            e.stopPropagation();
            var child = $element.find('ul').first();
            child.toggleClass(toggledClass);
        });
    }
}

module.export = indigoToggleClassChild;

