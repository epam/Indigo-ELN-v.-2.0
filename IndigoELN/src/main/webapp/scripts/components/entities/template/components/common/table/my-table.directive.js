/**
 * Created by Stepan_Litvinov on 3/1/2016.
 */
angular.module('indigoeln')
    .directive('myTableVal', function ($sce, roundFilter) {
        return {
            restrict: 'E',
            replace: true,
            require: '^myTable',
            scope: {
                myColumn: '=',
                myRow: '=',
                myRowIndex: '='
            },
            link: function ($scope, iElement, iAttrs, myTableCtrl) {
                var oldVal, isChanged;
                $scope.toggleEditable = function () {
                    return myTableCtrl.toggleEditable($scope.myColumn.id, $scope.myRowIndex);
                };
                $scope.isEditable = function () {
                    return myTableCtrl.isEditable($scope.myColumn.id, $scope.myRowIndex);
                };
                $scope.isEmpty = function (obj) {
                    return _.isNull(obj) || _.isUndefined(obj) || obj === 0 ||
                        (_.isObject(obj) && (_.isEmpty(obj) || obj.value === 0));
                };
                $scope.closeThis = function () {
                    if ($scope.myColumn.onClose && isChanged) {
                        $scope.myColumn.onClose({
                            model: $scope.myRow[$scope.myColumn.id],
                            row: $scope.myRow,
                            column: $scope.myColumn.id,
                            oldVal: oldVal
                        });
                        isChanged = false;
                    }
                    return myTableCtrl.toggleEditable(null, null, null);
                };
                if ($scope.myColumn.onClose) {
                    $scope.$watch(function () {
                        return _.isObject($scope.myRow[$scope.myColumn.id]) ? $scope.myRow[$scope.myColumn.id].value || $scope.myRow[$scope.myColumn.id].name : $scope.myRow[$scope.myColumn.id];
                    }, function (newVal, prevVal) {
                        oldVal = prevVal;
                        isChanged = !angular.equals(newVal, prevVal) && $scope.isEditable();
                    }, true);
                }
                if ($scope.myColumn.hasPopover) {
                    $scope.$watch(function () {
                        return $scope.myRow[$scope.myColumn.id];
                    }, function () {
                        $scope.popoverTitle = $scope.myRow[$scope.myColumn.id];
                        var image = $scope.myRow.structure ? $scope.myRow.structure.image : '';
                        $scope.popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" style="padding:10px;" ' +
                            'src="data:image/svg+xml;base64,' + image + '" alt="Image is unavailable."></div>');
                    });
                }
                $scope.unitParsers = [function (viewValue) {
                    return +$u(viewValue, $scope.myRow[$scope.myColumn.id].unit).val();
                }];
                $scope.unitFormatters = [function (modelValue) {
                    return +roundFilter($u(modelValue).as($scope.myRow[$scope.myColumn.id].unit).val(), $scope.myRow[$scope.myColumn.id].entered, $scope.myRow[$scope.myColumn.id].sigDigits);
                }];
            },
            templateUrl: 'scripts/components/entities/template/components/common/table/my-table-val.html'
        };

    })
    .directive('myTable', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
                myId: '@',
                myColumns: '=',
                myRows: '=',
                myReadonly: '=',
                myOnRowSelected: '=',
                myDraggableRows: '=',
                myDraggableColumns: '='

            },
            controller: function ($scope, dragulaService, localStorageService, $attrs, unitService, selectService, Principal) {
                var that = this;
                Principal.identity()
                    .then(function (user) {
                        $scope.saveInLocalStorage = function () {
                            localStorageService.set(user.id + '.' + $scope.myId + '.columns', JSON.stringify(
                                _.map($scope.myColumns, function (column) {
                                    column.isVisible = _.isUndefined(column.isVisible) ? true : column.isVisible;
                                    return {id: column.id, isVisible: column.isVisible};
                                })
                            ));
                        };

                        var columnIdsAndFlags = JSON.parse(localStorageService.get(user.id + '.' + $scope.myId + '.columns'));
                        if (!columnIdsAndFlags) {
                            $scope.saveInLocalStorage();
                        }
                        $scope.myColumns = _.sortBy($scope.myColumns, function (column) {
                                return _.findIndex(columnIdsAndFlags, function (item) {
                                    return item.id === column.id;
                                });
                            }
                        );

                        $scope.myColumns = _.map($scope.myColumns, function (column) {
                            var index = _.findIndex(columnIdsAndFlags, function (item) {
                                return item.id === column.id;
                            });
                            if (index > -1) {
                                column.isVisible = columnIdsAndFlags[index].isVisible;
                            }
                            return column;
                        });

                        if ($attrs.myDraggableColumns) {
                            $scope.$watch(function () {
                                return _.map($scope.myColumns, _.iteratee('id')).join('-');
                            }, $scope.saveInLocalStorage);
                        }
                    });

                var editableCell = null;
                that.toggleEditable = function (columnId, rowIndex) {
                    editableCell = columnId + '-' + rowIndex;
                };
                that.isEditable = function (columnId, rowIndex) {
                    if (columnId === null || rowIndex === null) {
                        return false;
                    }
                    return editableCell === columnId + '-' + rowIndex;
                };
                $scope.onRowSelect = function ($event, row) {
                    var target = $($event.target);
                    if (target.is('button,span,ul,a,li,input')) {
                        return;
                    }
                    _.each($scope.myRows, function (item) {
                        if (item !== row) {
                            item.$$selected = false;
                        }
                    });

                    row.$$selected = !row.$$selected;

                    if ($scope.myOnRowSelected) {
                        $scope.myOnRowSelected(_.find($scope.myRows, function (item) {
                            return item.$$selected;
                        }));
                    }
                };
                dragulaService.options($scope, 'my-table-columns', {
                    moves: function (el, container, handle) {
                        return !handle.classList.contains('no-draggable');
                    }
                });
                dragulaService.options($scope, 'my-table-rows', {
                    moves: function (el, container, handle) {
                        return $(handle).is('div') || $(handle).is('td');
                    }
                });

                unitService.processColumns($scope.myColumns, $scope.myRows);
                selectService.processColumns($scope.myColumns, $scope.myRows);

            },
            compile: function (tElement, tAttrs) {
                if (tAttrs.myDraggableRows) {
                    var $tBody = $(tElement.find('tbody'));
                    $tBody.attr('dragula', '\'my-table-rows\'');
                    $tBody.attr('dragula-model', 'myRows');
                }
                if (tAttrs.myDraggableColumns) {
                    var $tr = $(tElement.find('thead tr'));
                    $tr.attr('dragula', '\'my-table-columns\'');
                    $tr.attr('dragula-model', 'myColumns');
                }
            },
            templateUrl: 'scripts/components/entities/template/components/common/table/my-table.html'
        };

    });