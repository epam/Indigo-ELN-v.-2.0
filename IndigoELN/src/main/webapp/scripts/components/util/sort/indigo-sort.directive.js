(function() {
    angular
        .module('indigoeln')
        .directive('indigoSort', indigoSort);

    function indigoSort() {
        return {
            restrict: 'A',
            scope: {
                predicate: '=indigoSort',
                ascending: '=',
                callback: '&'
            },
            controller: controller
        };

        /* @ngInject */
        function controller($scope) {
            this.sort = function(field) {
                if (field !== $scope.predicate) {
                    $scope.ascending = true;
                } else {
                    $scope.ascending = !$scope.ascending;
                }
                $scope.predicate = field;
                $scope.$apply();
                $scope.callback();
            };

            this.applyClass = function(element) {
                var allThIcons = element.parent().find('span.glyphicon'),
                    sortIcon = 'glyphicon-sort',
                    sortAsc = 'glyphicon-sort-by-attributes',
                    sortDesc = 'glyphicon-sort-by-attributes-alt',
                    remove = sortIcon + ' ' + sortDesc,
                    add = sortAsc,
                    thisIcon = element.find('span.glyphicon');
                if (!$scope.ascending) {
                    remove = sortIcon + ' ' + sortAsc;
                    add = sortDesc;
                }
                allThIcons.removeClass(sortAsc + ' ' + sortDesc);
                allThIcons.addClass(sortIcon);
                thisIcon.removeClass(remove);
                thisIcon.addClass(add);
            };
        }
    }
})();
