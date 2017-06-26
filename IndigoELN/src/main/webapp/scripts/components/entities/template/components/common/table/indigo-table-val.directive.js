(function () {
    angular
        .module('indigoeln')
        .directive('indigoTableVal', indigoTableVal);

    /* @ngInject */
    function indigoTableVal($sce, roundFilter, Alert, $timeout) {
        return {
            restrict: 'E',
            replace: true,
            require: '^indigoTable',
            scope: {
                indigoColumn: '=',
                indigoRow: '=',
                indigoRowIndex: '='
            },
            link: link,
            templateUrl: 'scripts/components/entities/template/components/common/table/table-val.html'
        };

        /* @ngInject */
        function link($scope, iElement, iAttrs, indigoTableCtrl) {
            var oldVal, isChanged;

            $scope.toggleEditable = function ($event) {
                var val = indigoTableCtrl.toggleEditable($scope.indigoColumn.id, $scope.indigoRowIndex);
                indigoTableCtrl.setClosePrevious($scope.closeThis);
                $timeout(function () {
                    iElement.find('input').on('keypress', function (e) {
                        if (e.keyCode == 13) {
                            e.preventDefault();
                            if ($scope.isEditable()) {
                                $scope.toggleEditable();
                                $scope.closeThis();
                            }
                        }
                    });
                }, 0);
                return val;
            };
            $scope.isEditable = function () {
                var enabled = true;
                if ($scope.indigoColumn.checkEnabled) {
                    enabled = $scope.indigoColumn.checkEnabled($scope.indigoRow, $scope.indigoColumn.id);
                }
                return enabled && indigoTableCtrl.isEditable($scope.indigoColumn.id, $scope.indigoRowIndex);
            };
            $scope.isEmpty = function (obj, col) {
                if (obj && col.showDefault) return false;
                return obj === 0 || _.isNull(obj) || _.isUndefined(obj) ||
                    (_.isObject(obj) && (_.isEmpty(obj) || obj.value === 0) || obj.value === '0');
            };
            $scope.closeThis = function () {
                var col = $scope.indigoColumn;
                var val = $scope.indigoRow[col.id];
                if ((col.type == 'scalar' || col.type == 'unit') && isChanged) {
                    var absv = Math.abs(val.value);
                    if (absv != val.value) {
                        val.value = absv;
                        Alert.error('Total Amount made must more than zero.');
                    }
                }
                if (col.type == 'input' && val === '') {
                    $scope.indigoRow[col.id] = val = undefined;
                }
                if (col.onClose && isChanged) {
                    col.onClose({
                        model: $scope.indigoRow[col.id],
                        row: $scope.indigoRow,
                        column: col.id,
                        oldVal: oldVal
                    });
                    isChanged = false;
                }
                indigoTableCtrl.setClosePrevious(null);
                return indigoTableCtrl.toggleEditable(null, null, null);
            };
            var unbinds = [];
            if ($scope.indigoColumn.onClose) {
                unbinds.push($scope.$watch(function () {
                    return _.isObject($scope.indigoRow[$scope.indigoColumn.id]) ? $scope.indigoRow[$scope.indigoColumn.id].value || $scope.indigoRow[$scope.indigoColumn.id].name : $scope.indigoRow[$scope.indigoColumn.id];
                }, function (newVal, prevVal) {
                    oldVal = prevVal;
                    isChanged = !angular.equals(newVal, prevVal) && $scope.isEditable();
                    var col = $scope.indigoColumn;
                    if (isChanged && col.onChange) {
                        col.onChange({row: $scope.indigoRow, model: $scope.indigoRow[col.id], oldVal: oldVal});
                    }
                }, true));
            }
            $scope.unitChange = function () {
                indigoTableCtrl.setDirty();
            };

            // unbinds.push($scope.$watch(function () {
            // 	var cell= $scope.indigoRow[$scope.indigoColumn.id];
            // 	if (_.isObject(cell) && cell.unit) return cell.unit;
            // }, function(newVal, prevVal) {
            // 	if (!angular.equals(newVal, prevVal))
            // }, true))

            if ($scope.indigoColumn.hasStructurePopover) {
                var updatePopover = function () {
                    $scope.popoverTitle = $scope.indigoRow[$scope.indigoColumn.id];
                    var image = $scope.indigoRow.structure ? $scope.indigoRow.structure.image : '';
                    $scope.popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" style="padding:10px;" ' +
                        'src="data:image/svg+xml;base64,' + image + '" alt="Image is unavailable."></div>');
                };
                unbinds.push($scope.$watch(function () {
                    return $scope.indigoRow[$scope.indigoColumn.id];
                }, updatePopover));
                unbinds.push($scope.$watch(function () {
                    return $scope.indigoRow.structure ? $scope.indigoRow.structure.image : null;
                }, updatePopover));
            }
            $scope.$on('$destroy', function () {
                _.each(unbinds, function (unbind) {
                    unbind();
                });
            });
            $scope.unitParsers = [function (viewValue) {
                return +$u(viewValue, $scope.indigoRow[$scope.indigoColumn.id].unit).val();
            }];
            $scope.unitFormatters = [function (modelValue) {
                return +roundFilter($u(modelValue).as($scope.indigoRow[$scope.indigoColumn.id].unit).val(), $scope.indigoRow[$scope.indigoColumn.id].sigDigits, $scope.indigoColumn, $scope.indigoRow);
            }];
        }
    }
})();