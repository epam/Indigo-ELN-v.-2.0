(function() {
    angular
        .module('indigoeln')
        .controller('IndigoTableController', IndigoTableController);

    /* @ngInject */
    function IndigoTableController($scope, dragulaService, localStorageService, $attrs, unitService, selectService,
                                   inputService, scalarService, Principal, $timeout, $filter) {
        var vm = this;
        var originalColumnIdsAndFlags;
        var editableCell = null;
        var closePrev;
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

            vm.setClosePrevious = setClosePrevious;
            vm.toggleEditable = toggleEditable;
            vm.isColumnReadonly = isColumnReadonly;
            vm.isEditable = isEditable;
            vm.searchDebounce = _.debounce(search, 300);
            vm.onRowSelect = onRowSelect;
            vm.onPageChanged = onPageChanged;
            vm.saveInLocalStorage = saveInLocalStorage;
            vm.resetColumns = resetColumns;

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
            var columnIdsAndFlags = angular.fromJson(localStorageService.get(user.id + '.' + vm.indigoId + '.columns'));
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
            localStorageService.set(user.id + '.' + vm.indigoId + '.columns', angular.toJson(getColumnsProps(vm.indigoColumns)));
        }

        function resetColumns() {
            localStorageService.remove(user.id + '.' + vm.indigoId + '.columns');
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

        function setClosePrevious(_closePrev) {
            closePrev = _closePrev;
        }

        function toggleEditable(columnId, rowIndex) {
            if (closePrev) {
                closePrev();
            }
            editableCell = columnId + '-' + rowIndex;
        }

        function isEditable(columnId, rowIndex) {
            if (columnId === null || rowIndex === null || vm.isReadonly) {
                return false;
            }

            if (vm.indigoEditable) {
                var row = vm.indigoRows[rowIndex];
                var editable = vm.indigoEditable(row, columnId);
                if (!editable) {
                    return false;
                }
            }

            return editableCell === (columnId + '-' + rowIndex);
        }

        function isColumnReadonly(col, rowId) {
            return col.readonly === true || isEditable(col.id, rowId);
        }

        function getRate(str, query, rate) {
            if (str.indexOf(query) == 0) {
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
            var target = $($event.target);
            if ($attrs.indigoTabSupport) {
                initTabSupport($event.currentTarget);
            }
            if (target.is('button,span,ul,a,li,input')) {
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

        function initTabSupport(tar) {
            var $tar = $(tar);
            var $input = $tar.find('input').focus();
            if (!$input.attr('tab-initiated')) {
                $input.on('keydown', function(e) {
                    if (e.keyCode != 9) {
                        return;
                    } // tab key
                    var $next = $tar.nextAll('[col-read-only="false"]').filter(function() {
                        return angular.element(this).find('[toggleEditable]')[0];
                    }).eq(0);

                    var toggle = $next.find('[toggleEditable]')[0];

                    if (toggle) {
                        $timeout(function() {
                            angular.element(toggle).triggerHandler('click');
                            initTabSupport($next);
                        });
                    }
                }).attr('tab-initiated', true);
            }
        }

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
