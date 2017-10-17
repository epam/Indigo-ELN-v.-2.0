(function() {
    angular
        .module('indigoeln')
        .directive('editableCell', editableCell);

    editableCell.$inject = [];

    function editableCell() {
        return {
            restrict: 'E',
            controller: editableCellController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                isReadonly: '=',
                isEditing: '=',
                onStartEdit: '&',
                onClose: '&'
            },
            templateUrl: 'scripts/components/entities/template/components/common/table/table-val.html'
        };
    }

    editableCellController.$inject = ['$scope', 'UnitsConverter', 'roundFilter', 'notifyService'];

    function editableCellController($scope, UnitsConverter, roundFilter, notifyService) {
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
            var col = vm.column;
            var val = vm.row[col.id];
            if ((col.type === 'scalar' || col.type === 'unit') && isChanged) {
                var absv = Math.abs(val.value);
                if (absv !== val.value) {
                    val.value = absv;
                    notifyService.error('Total Amount made must more than zero.');
                }
            }
            if (col.type === 'input' && val === '') {
                vm.row[col.id] = undefined;
            }

            if (col.onClose && isChanged) {
                col.onClose({
                    model: vm.row[col.id],
                    row: vm.row,
                    column: col.id,
                    oldVal: oldVal
                });
                isChanged = false;
            }
            vm.onClose({
                data: {
                    model: vm.row[col.id],
                    row: vm.row,
                    column: col.id,
                    oldVal: oldVal
                }
            });
        }

        function bindEvents() {
            if (vm.column.onClose) {
                $scope.$watch(function() {
                    return _.isObject(vm.row[vm.column.id]) ? vm.row[vm.column.id].value || vm.row[vm.column.id].name : vm.row[vm.column.id];
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
    }
})();
