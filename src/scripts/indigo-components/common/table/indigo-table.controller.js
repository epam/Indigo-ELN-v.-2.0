(function() {
    angular
        .module('indigoeln.Components')
        .controller('IndigoTableController', IndigoTableController);

    /* @ngInject */
    function IndigoTableController($scope, dragulaService, simpleLocalCache, Principal, $timeout, $filter) {
        var vm = this;
        var searchColumns;
        var userId;

        init();

        function init() {
            userId = Principal.getUserId();
            searchColumns = vm.indigoSearchColumns || ['id', 'nbkBatch'];
            vm.searchText = '';
            vm.filteredRows = vm.indigoRows;
            vm.pagination = {
                page: 1,
                pageSize: 10
            };
            vm.visibleColumns = getVisibleColumns();

            vm.startEdit = startEdit;
            vm.searchDebounce = _.debounce(search, 300);
            vm.onRowSelect = onRowSelect;
            vm.onPageChanged = onPageChanged;
            vm.onChangedColumnSetting = onChangedColumnSetting;
            vm.resetColumns = resetColumnsSettings;
            vm.onClose = onClose;
            vm.onClickRadio = onClickRadio;

            bindEvents();
        }

        function updateVisibleColumnsExpression() {
            _.forEach(vm.indigoColumns, function(column) {
                var visibleColumn = vm.visibleColumns[column.id];
                if (visibleColumn && _.isBoolean(column.isVisible) && visibleColumn !== column.isVisible) {
                    vm.onChangedVisibleColumn({column: column, isVisible: visibleColumn});
                }
            });
        }

        function onClickRadio(column, row, value) {
            _.forEach(vm.indigoRows, function(iRow) {
                iRow[column.id] = iRow === row;
            });

            column.onClick({model: value, row: row, column: column.id});
        }

        function getVisibleColumns() {
            return simpleLocalCache.getByKey(userId + '.' + vm.indigoId + '.visible.columns') || buildVisibleColumns(vm.indigoColumns);
        }

        function saveColumnSettings() {
            simpleLocalCache.putByKey(userId + '.' + vm.indigoId + '.visible.columns', vm.visibleColumns);
        }

        function getSortedColumns(columns) {
            if (!columns) {
                return;
            }

            var sortedColumns = simpleLocalCache.getByKey(userId + '.' + vm.indigoId + '.columnsOrder');

            if (_.isArray(sortedColumns)) {
                var resultColumns = [];
                _.forEach(sortedColumns, function(sortedColumn, i) {
                    resultColumns[i] = _.find(columns, function(column) {
                        return column.id === sortedColumn;
                    });
                });

                return resultColumns;
            }

            return columns;
        }

        function saveColumnsOrder() {
            simpleLocalCache.putByKey(userId + '.' + vm.indigoId + '.columnsOrder', _.map(vm.columns, 'id'));
        }

        function onChangedColumnSetting(changedColumn, isVisible) {
            vm.visibleColumns[changedColumn.id] = isVisible;
            vm.onChangedVisibleColumn({column: changedColumn, isVisible: isVisible});
            saveColumnSettings();
        }

        function buildVisibleColumns(columns) {
            var visibleColumns = {};
            _.forEach(columns, function(column) {
                visibleColumns[column.id] = _.isBoolean(column.isVisible) ? column.isVisible : true;
            });

            return visibleColumns;
        }

        function resetColumnsSettings() {
            vm.visibleColumns = buildVisibleColumns(vm.indigoColumns);
            vm.columns = vm.indigoColumns.slice();

            saveColumnSettings();
        }

        function onClose(column, data) {
            vm.editingCellId = null;
            vm.onCloseCell({column: column, data: data});
        }

        function startEdit(id) {
            vm.editingCellId = id;
        }

        function getRate(str, query, rate) {
            if (str.indexOf(query) === 0) {
                return 10;
            } else if (str.indexOf(query) > 0) {
                return 1;
            }

            return rate;
        }

        function filterRowsByQuery(query) {
            var filtered = [];
            _.forEach(vm.indigoRows, function(row) {
                var rate = 0;
                _.forEach(searchColumns, function(column) {
                    if (!row[column]) {
                        return;
                    }
                    rate += getRate(row[column].toString().toLowerCase(), query, rate);
                });
                row.$$rate = rate;
                if (rate > 0) {
                    filtered.push(row);
                }
            });

            return $filter('orderBy')(filtered, '$$rate', true);
        }

        function search() {
            var query = vm.searchText.trim().toLowerCase();

            if (!vm.indigoRows) {
                return;
            }

            if (!query) {
                vm.filteredRows = vm.indigoRows;
                updateRowsForDisplay();

                return;
            }
            vm.filteredRows = filterRowsByQuery(query);

            updateRowsForDisplay();
        }

        function onRowSelect($event, row) {
            if (angular.element($event.target).is('button,span,ul,a,li,input')) {
                return;
            }
            vm.onSelectRow({row: !_.isEqual(vm.selectedBatch, row) ? row : null});
        }

        dragulaService.options($scope, 'my-table-columns', {
            moves: function(el, container, handle) {
                return !handle.classList.contains('no-draggable');
            }
        });
        dragulaService.options($scope, 'my-table-rows', {
            moves: function(el, container, handle) {
                return angular.element(handle).is('div') || angular.element(handle).is('td');
            }
        });

        function onPageChanged() {
            updateRowsForDisplay();
        }

        function getSkipItems() {
            return (vm.pagination.page - 1) * vm.pagination.pageSize;
        }

        function updateRowsForDisplay() {
            if (!vm.filteredRows || vm.filteredRows.length === 0) {
                vm.rowsForDisplay = null;

                return;
            }

            var skip = getSkipItems(vm.filteredRows);

            if (skip >= vm.filteredRows.length) {
                updateCurrentPage(vm.filteredRows);
                skip = getSkipItems();
            }

            $timeout(function() {
                vm.totalFilteredRowsLength = vm.filteredRows.length;
                vm.rowsForDisplay = $filter('limitTo')(vm.filteredRows, vm.pagination.pageSize, skip);
            });
        }

        function updateCurrentPage(rows) {
            vm.pagination.page = _.ceil(rows.length / vm.pagination.pageSize);
        }

        function initColumns() {
            vm.columns = getSortedColumns(vm.indigoColumns);
            updateVisibleColumnsExpression();
            updateRowsForDisplay();
        }

        function bindEvents() {
            $scope.$watch('vm.indigoColumns', initColumns);
            $scope.$watch('vm.indigoRows.length', vm.searchDebounce);
            $scope.$watch('vm.indigoRows', vm.searchDebounce);

            $scope.$on('my-table-columns.dragend', saveColumnsOrder);
        }
    }
})();
