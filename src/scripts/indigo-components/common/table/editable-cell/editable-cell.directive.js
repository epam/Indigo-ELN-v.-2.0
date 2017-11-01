(function() {
    angular
        .module('indigoeln.Components')
        .directive('editableCell', editableCell);

    editableCell.$inject = [];

    function editableCell() {
        return {
            restrict: 'E',
            controller: EditableCellController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                isReadonly: '=',
                isEditing: '=',
                onStartEdit: '&',
                onClose: '&'
            },
            templateUrl: 'scripts/indigo-components/common/table/editable-cell/editable-cell.html'
        };
    }

    EditableCellController.$inject = ['$scope', 'UnitsConverter', 'roundFilter', 'notifyService'];

    function EditableCellController($scope, UnitsConverter, roundFilter, notifyService) {
        var vm = this;
        var oldVal;
        var isChanged;

        init();

        function init() {
            vm.column = $scope.$parent.column;
            vm.row = $scope.$parent.$parent.row;

            vm.isCheckEnabled = true;

            vm.unitParsers = [function(viewValue) {
                return +UnitsConverter.convert(viewValue, vm.row[vm.column.id].unit).val();
            }];

            vm.unitFormatters = [function(modelValue) {
                return +roundFilter(UnitsConverter.convert(modelValue)
                    .as(vm.row[vm.column.id].unit)
                    .val(), vm.row[vm.column.id].sigDigits, vm.column, vm.row);
            }];

            vm.isEmpty = isEmpty;
            vm.closeThis = closeThis;

            bindEvents();
        }

        function isEmpty(obj, col) {
            if (obj && col.showDefault) {
                return false;
            }

            return _.isEmpty(obj) || obj.value === '0' || obj.value === 0;
        }

        function closeThis() {
            var model = vm.row[vm.column.id];
            if ((vm.column.type === 'scalar' || vm.column.type === 'unit') && isChanged) {
                var absv = Math.abs(model.value);
                if (absv !== model.value) {
                    model.value = absv;
                    notifyService.error('Total Amount made must more than zero.');
                }
            }
            if (vm.column.type === 'input' && model === '') {
                vm.row[vm.column.id] = undefined;
            }

            if (isChanged) {
                isChanged = false;
            }
            vm.onClose({
                data: {
                    model: vm.row[vm.column.id],
                    row: vm.row,
                    column: vm.column.id,
                    oldVal: oldVal
                }
            });
        }

        function bindEvents() {
            if (vm.column.onClose) {
                $scope.$watch(function() {
                    return getCellContent(vm.row[vm.column.id]);
                }, function(newVal, prevVal) {
                    var col = vm.column;
                    oldVal = prevVal;
                    isChanged = !_.isEqual(newVal, prevVal) && vm.isEditing && !vm.isReadonly && vm.isCheckEnabled;
                    if (isChanged && col.onChange) {
                        col.onChange({
                            row: vm.row, model: vm.row[col.id], oldVal: oldVal
                        });
                    }
                }, true);
            }

            $scope.$watch('::vm.column.checkEnabled', function() {
                if (vm.column.checkEnabled) {
                    $scope.$watch('vm.column.checkEnabled(vm.row)', function() {
                        vm.isCheckEnabled = vm.column.checkEnabled(vm.row);
                    });
                }
            });
        }

        function getCellContent(cell) {
            return _.isObject(cell) ? cell.value || cell.name : cell;
        }
    }
})();
