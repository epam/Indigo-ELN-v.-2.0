(function() {
    angular
        .module('indigoeln')
        .directive('indigoTableVal', indigoTableVal);

    /* @ngInject */
    function indigoTableVal($sce, roundFilter, notifyService, $timeout, EntitiesBrowser, UnitsConverter) {
        return {
            restrict: 'E',
            controller: IndigoTableValController,
            controllerAs: 'vm',
            bindToController: true,
            scope: {
                indigoColumn: '=',
                indigoRow: '=',
                indigoRowIndex: '=',
                isEditableColumn: '&',
                toggleEditableColumn: '&',
                setClosePrevious: '&',
                close: '&',
                isReadonly: '='
            },
            link: link,
            templateUrl: 'scripts/components/entities/template/components/common/table/table-val.html'
        };

        function link($scope, $element, $attrs, controller) {
            var vm = controller;
            var oldVal;
            var isChanged;

            init();

            function init() {
                vm.unitParsers = [function(viewValue) {
                    return +UnitsConverter.convert(viewValue, vm.indigoRow[vm.indigoColumn.id].unit).val();
                }];

                vm.unitFormatters = [function(modelValue) {
                    return +roundFilter(UnitsConverter.convert(modelValue)
                        .as(vm.indigoRow[vm.indigoColumn.id].unit)
                        .val(), vm.indigoRow[vm.indigoColumn.id].sigDigits, vm.indigoColumn, vm.indigoRow);
                }];

                vm.isEditable = isEditable;
                vm.toggleEditable = toggleEditable;
                vm.unitChange = unitChange;
                vm.isEmpty = isEmpty;
                vm.closeThis = closeThis;

                bindEvents();
            }

            function toggleEditable() {
                vm.toggleEditableColumn();
                vm.setClosePrevious({callback: closeThis});
                $timeout(function() {
                    $element.find('input').on('keypress', function(e) {
                        if (e.keyCode === 13) {
                            e.preventDefault();
                            if (vm.isEditable()) {
                                vm.toggleEditable();
                                vm.closeThis();
                            }
                        }
                    });
                }, 0);
            }

            function isEditable() {
                var enabled = true;
                if (vm.indigoColumn.checkEnabled) {
                    enabled = vm.indigoColumn.checkEnabled(vm.indigoRow, vm.indigoColumn.id);
                }

                return enabled && vm.isEditableColumn();
            }

            function isEmpty(obj, col) {
                if (obj && col.showDefault) {
                    return false;
                }

                return obj === 0 || _.isNull(obj) || _.isUndefined(obj) ||
                    (_.isObject(obj) && (_.isEmpty(obj) || obj.value === 0) || obj.value === '0');
            }

            function closeThis() {
                var col = vm.indigoColumn;
                var val = vm.indigoRow[col.id];
                if ((col.type == 'scalar' || col.type == 'unit') && isChanged) {
                    var absv = Math.abs(val.value);
                    if (absv != val.value) {
                        val.value = absv;
                        notifyService.error('Total Amount made must more than zero.');
                    }
                }
                if (col.type == 'input' && val === '') {
                    vm.indigoRow[col.id] = val = undefined;
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
                vm.setClosePrevious({callback: null});
                vm.close();
            }

            function unitChange() {
                EntitiesBrowser.setCurrentFormDirty();
            }

            // unbinds.push($scope.$watch(function () {
            // 	var cell= vm.indigoRow[vm.indigoColumn.id];
            // 	if (_.isObject(cell) && cell.unit) return cell.unit;
            // }, function(newVal, prevVal) {
            // 	if (!angular.equals(newVal, prevVal))
            // }, true))

            function updatePopover() {
                vm.popoverTitle = vm.indigoRow[vm.indigoColumn.id];
                var image = vm.indigoRow.structure ? vm.indigoRow.structure.image : '';
                vm.popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" ' +
                    'src="data:image/svg+xml;base64,' + image + '" alt="Image is unavailable."></div>');
            }

            function bindEvents() {
                if (vm.indigoColumn.hasStructurePopover) {
                    $scope.$watch(function() {
                        return vm.indigoRow[vm.indigoColumn.id];
                    }, updatePopover);
                    $scope.$watch(function() {
                        return vm.indigoRow.structure ? vm.indigoRow.structure.image : null;
                    }, updatePopover);
                }

                if (vm.indigoColumn.onClose) {
                    $scope.$watch(function() {
                        return _.isObject(vm.indigoRow[vm.indigoColumn.id]) ? vm.indigoRow[vm.indigoColumn.id].value || vm.indigoRow[vm.indigoColumn.id].name : vm.indigoRow[vm.indigoColumn.id];
                    }, function(newVal, prevVal) {
                        oldVal = prevVal;
                        isChanged = !_.isEqual(newVal, prevVal) && vm.isEditable();
                        var col = vm.indigoColumn;
                        if (isChanged && col.onChange) {
                            col.onChange({
                                row: vm.indigoRow, model: vm.indigoRow[col.id], oldVal: oldVal
                            });
                        }
                    }, true);
                }
            }
        }

        function IndigoTableValController() {
        }
    }
})();
