(function() {
    angular
        .module('indigoeln')
        .directive('indigoTableVal', indigoTableVal);

    function indigoTableVal() {
        return {
            restrict: 'E',
            controller: IndigoTableValController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                model: '=',
                indigoColumn: '=',
                indigoRow: '=',
                isReadonly: '=',
                isEditing: '=',
                onStartEdit: '&',
                onClose: '&'
            },
            templateUrl: 'scripts/components/entities/template/components/common/table/table-val.html'
        };
    }

    IndigoTableValController.$inject = ['$scope', 'UnitsConverter', 'roundFilter', 'notifyService'];

    function IndigoTableValController($scope, UnitsConverter, roundFilter, notifyService) {
        var vm = this;
        var oldVal;
        var isChanged;

        init();

        function init() {
            vm.unitParsers = [function(viewValue) {
                return +UnitsConverter.convert(viewValue, vm.model.unit).val();
            }];

            vm.unitFormatters = [function(modelValue) {
                return +roundFilter(UnitsConverter.convert(modelValue)
                    .as(vm.model.unit)
                    .val(), vm.model.sigDigits, vm.indigoColumn, vm.indigoRow);
            }];

            vm.isEdit = isEdit;
            vm.isEmpty = isEmpty;
            vm.closeThis = closeThis;

            bindEvents();
        }

        function isEdit() {
            if (vm.indigoColumn.checkEnabled) {
                return vm.indigoColumn.checkEnabled(vm.indigoRow, vm.indigoColumn.id);
            }

            return true;
        }

        function isEmpty(obj, col) {
            if (obj && col.showDefault) {
                return false;
            }

            return _.isEmpty(obj) || obj.value === '0' || obj.value === 0;
        }

        function closeThis() {
            var col = vm.indigoColumn;
            var val = vm.indigoRow[col.id];
            if ((col.type === 'scalar' || col.type === 'unit') && isChanged) {
                var absv = Math.abs(val.value);
                if (absv !== val.value) {
                    val.value = absv;
                    notifyService.error('Total Amount made must more than zero.');
                }
            }
            if (col.type === 'input' && val === '') {
                vm.indigoRow[col.id] = undefined;
            }

            if (col.onClose && isChanged) {
                col.onClose({
                    model: vm.indigoRow[col.id],
                    row: vm.indigoRow,
                    column: col.id,
                    oldVal: oldVal
                });
                isChanged = false;
            }
            vm.onClose({
                data: {
                    model: vm.indigoRow[col.id],
                    row: vm.indigoRow,
                    column: col.id,
                    oldVal: oldVal
                }
            });
        }

        function bindEvents() {
            if (vm.indigoColumn.onClose) {
                $scope.$watch(function() {
                    return _.isObject(vm.model) ? vm.model.value || vm.model.name : vm.model;
                }, function(newVal, prevVal) {
                    var col = vm.indigoColumn;
                    oldVal = prevVal;
                    isChanged = !_.isEqual(newVal, prevVal) && vm.isEdit();
                    if (isChanged && col.onChange) {
                        col.onChange({
                            row: vm.indigoRow, model: vm.indigoRow[col.id], oldVal: oldVal
                        });
                    }
                }, true);
            }
        }
    }
})();
