(function() {
    angular
        .module('indigoeln')
        .controller('IndigoTableController', IndigoTableController);

    /* @ngInject */
    function IndigoTableController($scope, dragulaService, EntitiesCache, $attrs, unitService, selectService,
                                   inputService, scalarService, Principal, $timeout, $filter) {
        var vm = this;
        var originalColumnIdsAndFlags;
        var searchColumns;
        var user;

        init();

        function init() {
            originalColumnIdsAndFlags = getColumnsProps(vm.indigoColumns);
            searchColumns = vm.indigoSearchColumns || ['id', 'nbkBatch'];
            vm.searchText = '';
            vm.pagination = {
                page: 1,
                pageSize: 10
            };
            updateRowsForDisplay(vm.indigoRows);

            unitService.processColumns(vm.indigoColumns, vm.indigoRows);
            selectService.processColumns(vm.indigoColumns, vm.indigoRows);
            inputService.processColumns(vm.indigoColumns, vm.indigoRows);
            scalarService.processColumns(vm.indigoColumns, vm.indigoRows);

            vm.startEdit = startEdit;
            vm.searchDebounce = _.debounce(search, 300);
            vm.onRowSelect = onRowSelect;
            vm.onPageChanged = onPageChanged;
            vm.saveInLocalStorage = saveInLocalStorage;
            vm.resetColumns = resetColumns;
            vm.onClose = onClose;

            getUser();
            bindEvents();
        }

        function getColumnsProps(indigoColumns) {
            return _.map(indigoColumns, function(column) {
                column.isVisible = _.isUndefined(column.isVisible) ? true : column.isVisible;

                return {
                    id: column.id, isVisible: column.isVisible
                };
            });
        }

        function getSortedColumns(columnIdsAndFlags) {
            return _.sortBy(vm.indigoColumns, function(column) {
                return _.findIndex(columnIdsAndFlags, function(item) {
                    return item.id === column.id;
                });
            });
        }

        function updateColumns() {
            var columnIdsAndFlags = JSON.parse(EntitiesCache.getByName(user.id + '.' + vm.indigoId + '.columns'));
            if (!columnIdsAndFlags) {
                vm.saveInLocalStorage();
            }
            columnIdsAndFlags = columnIdsAndFlags || originalColumnIdsAndFlags;

            vm.indigoColumns = _.map(getSortedColumns(columnIdsAndFlags), function(column) {
                var index = _.findIndex(columnIdsAndFlags, function(item) {
                    return item.id === column.id;
                });
                if (index > -1) {
                    column.isVisible = columnIdsAndFlags[index].isVisible;
                }

                return column;
            });
        }

        function saveInLocalStorage() {
            EntitiesCache.putByName(user.id + '.' + vm.indigoId + '.columns', JSON.stringify(getColumnsProps(vm.indigoColumns)));
        }

        function resetColumns() {
            EntitiesCache.removeByKey(user.id + '.' + vm.indigoId + '.columns');
            updateColumns();
        }

        function getUser() {
            Principal
                .identity()
                .then(function(userIdentity) {
                    user = userIdentity;
                    updateColumns(user);

                    if ($attrs.indigoDraggableColumns) {
                        $scope.$watch(function() {
                            return _.map(vm.indigoColumns, _.iteratee('id')).join('-');
                        }, vm.saveInLocalStorage);
                    }
                });
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

        function search(queryString) {
            var query = queryString.trim().toLowerCase();

            if (!vm.indigoRows) {
                return;
            }

            if (!query) {
                updateRowsForDisplay(vm.indigoRows);

                return;
            }

            updateRowsForDisplay(filterRowsByQuery(query));
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
                return $(handle).is('div') || $(handle).is('td');
            }
        });

        function onPageChanged() {
            updateRowsForDisplay(vm.rowsForDisplay);
        }

        function getSkipItems() {
            return (vm.pagination.page - 1) * vm.pagination.pageSize;
        }

        function updateRowsForDisplay(rows) {
            if (!rows || rows.length === 0) {
                vm.limit = 0;
                vm.rowsForDisplay = null;

                return;
            }

            var skip = getSkipItems(rows);

            if (skip >= rows.length) {
                updateCurrentPage(rows);
                skip = getSkipItems();
            }

            $timeout(function() {
                vm.limit = skip + vm.pagination.pageSize;
                vm.rowsForDisplay = rows;
            });
        }

        function updateCurrentPage(rows) {
            vm.pagination.page = _.ceil(rows.length / vm.pagination.pageSize);
        }

        function bindEvents() {
            $scope.$watchCollection('vm.indigoRows', function() {
                search(vm.searchText);
            });
        }
    }
})();
