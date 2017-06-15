(function () {
    angular
        .module('indigoeln')
        .directive('indigoTable', indigoTable);

    function indigoTable() {
        return {
            restrict: 'E',
            replace: true,
            transclude: true,
            scope: {
                indigoId: '@',
                indigoLabel: '@',
                indigoColumns: '=',
                indigoRows: '=',
                indigoReadonly: '=',
                indigoEditable: '=',
                indigoOnRowSelected: '=',
                indigoDraggableRows: '=',
                indigoDraggableColumns: '=',
                indigoHideColumnSettings: '=',
                indigoSearchColumns: '='
            },
            controller: 'IndigoTableController',
            compile: compile,
            templateUrl: 'scripts/components/entities/template/components/common/table/table.html'
        };
    }

    /* @ngInject */
    function compile(tElement, tAttrs) {
        if (tAttrs.indigoDraggableRows) {
            var $tBody = $(tElement.find('tbody'));
            $tBody.attr('dragula', '\'my-table-rows\'');
            $tBody.attr('dragula-model', 'indigoRows');
        }
        if (tAttrs.indigoDraggableColumns) {
            var $tr = $(tElement.find('thead tr'));
            $tr.attr('dragula', '\'my-table-columns\'');
            $tr.attr('dragula-model', 'indigoColumns');
        }
        return {
            post: function (scope, element, attrs, ctrl, transclude) {
                element.find('.transclude').replaceWith(transclude());
            }
        };
    }
})();