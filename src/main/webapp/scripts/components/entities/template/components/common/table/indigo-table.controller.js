(function() {
    angular
        .module('indigoeln')
        .controller('IndigoTableController', IndigoTableController);

    /* @ngInject */
    function IndigoTableController($scope, dragulaService, localStorageService, $attrs, unitService, selectService,
                                   inputService, scalarService, Principal, $timeout, $filter, EntitiesBrowser) {
        var vm = this;
        var originalColumnIdsAndFlags;
        var editableCell = null;
        var closePrev;
        var stimeout;
        var lastQ;
        var originalRows;
        var searchColumns;
        var user;

        init();

        function init() {
            originalColumnIdsAndFlags = getColumnsProps(vm.indigoColumns);
            originalRows = vm.indigoRows;
            searchColumns = vm.indigoSearchColumns || ['id', 'nbkBatch'];
            vm.pagination = {
                page: 1,
                pageSize: 10
            };
            vm.filteredRows = originalRows;

            vm.setClosePrevious = setClosePrevious;
            vm.toggleEditable = toggleEditable;
            vm.isFormReadonly = isFormReadonly;
            vm.isColumnReadonly = isColumnReadonly;
            vm.isEditable = isEditable;
            vm.search = search;
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
                }
            );
        }

        function updateColumns() {
            var columnIdsAndFlags = JSON.parse(localStorageService.get(user.id + '.' + vm.indigoId + '.columns'));
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
            if (columnId === null || rowIndex === null || vm.isFormReadonly()) {
                return false;
            }

            if (vm.indigoEditable) {
                var row = vm.rowsForDisplay[rowIndex];
                var editable = vm.indigoEditable(row, columnId);
                if (!editable) {
                    return false;
                }
            }

            return editableCell === columnId + '-' + rowIndex;
        }

        function isFormReadonly() {
            var curForm = EntitiesBrowser.getCurrentForm();

            return (curForm && curForm.$$isReadOnly);
        }

        function isColumnReadonly(col, rowId) {
            return col.readonly === true || isEditable(col.id, rowId);
        }

        function search(queryString) {
            var query = queryString.trim().toLowerCase();

            if ((lastQ && lastQ === query) || !originalRows) {
                return;
            }
            if (stimeout) {
                $timeout.cancel(stimeout);
            }
            if (!query) {
                lastQ = query;
                vm.filteredRows = originalRows;

                return;
            }

            stimeout = $timeout(function() {
                var filtered = [];
                originalRows.forEach(function(r) {
                    var rate = 0;
                    searchColumns.forEach(function(sc) {
                        if (!r[sc]) {
                            return;
                        }
                        var s = r[sc].toString().toLowerCase();
                        if (s.indexOf(query) == 0) {
                            rate += 10;
                        } else if (s.indexOf(query) > 0) {
                            rate++;
                        }
                    });
                    r.$$rate = rate;
                    if (rate > 0) {
                        filtered.push(r);
                    }
                });
                vm.filteredRows = $filter('orderBy')(filtered, 'rate', true);
                lastQ = query;
            }, 300);
        }

        function onRowSelect($event, row) {
            var target = $($event.target);
            if ($attrs.indigoTabSupport) {
                initTabSupport($event.currentTarget);
            }
            if (target.is('button,span,ul,a,li,input')) {
                return;
            }
            _.each(vm.indigoRows, function(item) {
                if (item !== row) {
                    item.$$selected = false;
                }
            });
            row.$$selected = !row.$$selected;
            if (vm.indigoOnRowSelected) {
                vm.indigoOnRowSelected(_.find(vm.indigoRows, function(item) {
                    return item.$$selected;
                }));
            }
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

        unitService.processColumns(vm.indigoColumns, vm.indigoRows);
        selectService.processColumns(vm.indigoColumns, vm.indigoRows);
        inputService.processColumns(vm.indigoColumns, vm.indigoRows);
        scalarService.processColumns(vm.indigoColumns, vm.indigoRows);

        function calcPages(rows) {
            return _.groupBy(rows, function(element, index) {
                return Math.floor(index / vm.pagination.pageSize);
            });
        }

        function initTabSupport(tar) {
            var $tar = $(tar);
            var $input = $tar.find('input').focus();
            if (!$input.attr('tab-initiated')) {
                $input.on('keydown', function(e) {
                    if (e.keyCode != 9) {
                        return;
                    } // tab key
                    var $next = $tar.nextAll('[col-read-only="false"]').filter(function() {
                            return $(this).find('[toggleEditable]')[0];
                        }).eq(0),
                        toggle = $next.find('[toggleEditable]')[0];

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
            updateRowsForDisplay();
        }

        function updateRowsForDisplay() {
            var pages = calcPages(vm.filteredRows);
            vm.rowsForDisplay = pages[vm.pagination.page - 1];
        }

        function bindEvents() {
            $scope.$watchCollection('vm.indigoRows', function(newVal, oldVal) {
                if (newVal && oldVal && newVal.length > oldVal.length) {
                    vm.searchText = '';
                    vm.search(vm.searchText);
                }
                originalRows = vm.indigoRows;
                vm.filteredRows = originalRows;
                updateRowsForDisplay();
            });
            $scope.$watchCollection('filteredRows', function(newVal, oldVal) {
                if (newVal && oldVal && newVal.length > oldVal.length) {
                    var pages = calcPages(vm.filteredRows);
                    vm.pagination.page = Object.keys(pages).length;
                }
                updateRowsForDisplay();
            });
        }
    }
})();
