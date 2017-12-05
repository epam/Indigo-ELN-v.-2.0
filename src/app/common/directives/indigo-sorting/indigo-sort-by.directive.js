function indigoSortBy() {
    return {
        restrict: 'A',
        scope: false,
        require: '^indigoSort',
        link: link
    };

    function link($scope, $element, $attr, parentCtrl) {
        var iconElement = $element.find('.glyphicon');

        $element.bind('click', function() {
            parentCtrl.sort($attr.indigoSortBy);
        });

        $scope.$watch(function() {
            return parentCtrl.isAscending + parentCtrl.indigoSort;
        }, updateClasses);

        function updateClasses() {
            var isCurrent = parentCtrl.indigoSort === $attr.indigoSortBy;
            iconElement.toggleClass('glyphicon-sort-by-attributes', isCurrent && parentCtrl.isAscending);
            iconElement.toggleClass('glyphicon-sort-by-attributes-alt', isCurrent && !parentCtrl.isAscending);
        }
    }
}

module.exports = indigoSortBy;

