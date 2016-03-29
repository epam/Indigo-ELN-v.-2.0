/**
 * Created by Stepan_Litvinov on 3/1/2016.
 */
'use strict';
angular.module('indigoeln')
    .directive('myTableVal', function () {
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
                $scope.toggleEditable = function () {
                    return myTableCtrl.toggleEditable($scope.myColumn.id, $scope.myRowIndex);
                };
                $scope.isEditable = function () {
                    return myTableCtrl.isEditable($scope.myColumn.id, $scope.myRowIndex);
                };
                $scope.closeThis = function () {
                    return myTableCtrl.toggleEditable(null, null, null);
                };
                $scope.isEmpty = function (obj) {
                    return typeof obj !== 'boolean' && _.isEmpty(obj);
                };
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
            controller: function ($scope, dragulaService, localStorageService, $attrs, unitService, selectService) {
                var columnsIds = JSON.parse(localStorageService.get($scope.myId + '.columns'));

                $scope.myColumns = _.sortBy($scope.myColumns, function (column) {
                    return _.indexOf(columnsIds, column.id);
                });
                if ($attrs.myDraggableColumns) {
                    $scope.$watch(function () {
                        return _.map($scope.myColumns, _.iteratee('id')).join('-');
                    }, function () {
                        localStorageService.set($scope.myId + '.columns', JSON.stringify(_.pluck($scope.myColumns, 'id')));
                    });
                }

                var editableCell = null;
                this.toggleEditable = function (columnId, rowIndex) {
                    editableCell = columnId + '-' + rowIndex;
                };
                this.isEditable = function (columnId, rowIndex) {
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
                    if (row.selected) {
                        _.each($scope.myRows, function (item) {
                            item.selected = false;
                        });
                    } else {
                        _.each($scope.myRows, function (item) {
                            item.selected = false;
                        });
                        row.selected = true;
                    }
                    if ($scope.myOnRowSelected) {
                        $scope.myOnRowSelected(_.find($scope.myRows, function (item) {
                            return item.selected;
                        }));
                    }
                };
                dragulaService.options($scope, 'my-table-columns', {
                    moves: function (el, container, handle) {
                        return !handle.classList.contains('no-draggable');
                    }
                });

                unitService.processColumns($scope.myColumns, $scope.myRows);
                selectService.processColumns($scope.myColumns, $scope.myRows);

            },
            compile: function (tElement, tAttrs, transclude) {
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