function indigoScroller() {
    return {
        restrict: 'A',
        link: link
    };

    function link($scope, $element, $attr) {
        $element.addClass('my-scroller-axis-' + $attr.indigoScroller);
        $element.mCustomScrollbar({
            axis: $attr.indigoScroller,
            theme: $attr.indigoScrollerTheme || 'indigo',
            scrollInertia: 300
        });
    }
}

module.export = indigoScroller;
