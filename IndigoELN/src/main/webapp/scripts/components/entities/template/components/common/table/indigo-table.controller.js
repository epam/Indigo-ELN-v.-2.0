(function () {
    angular
        .module('indigoeln')
        .controller('IndigoTableController', IndigoTableController);

    /* @ngInject */
    function IndigoTableController($scope, dragulaService, localStorageService, $attrs, unitService, selectService, inputService, scalarService, Principal, $timeout, $filter, EntitiesBrowser) {
        var that = this;

        function getColumnsProps(indigoColumns) {
            return _.map(indigoColumns, function (column) {
                column.isVisible = _.isUndefined(column.isVisible) ? true : column.isVisible;
                return {id: column.id, isVisible: column.isVisible};
            });
        }

        var originalColumnIdsAndFlags = getColumnsProps($scope.indigoColumns);

        function updateColumns(user) {
            var columnIdsAndFlags = JSON.parse(localStorageService.get(user.id + '.' + $scope.indigoId + '.columns'));
            if (!columnIdsAndFlags) {
                $scope.saveInLocalStorage();
            }
            columnIdsAndFlags = columnIdsAndFlags || originalColumnIdsAndFlags;

            $scope.indigoColumns = _.sortBy($scope.indigoColumns, function (column) {
                    return _.findIndex(columnIdsAndFlags, function (item) {
                        return item.id === column.id;
                    });
                }
            );

            $scope.indigoColumns = _.map($scope.indigoColumns, function (column) {
                var index = _.findIndex(columnIdsAndFlags, function (item) {
                    return item.id === column.id;
                });
                if (index > -1) {
                    column.isVisible = columnIdsAndFlags[index].isVisible;
                }
                return column;
            });
        }

        Principal.identity()
            .then(function (user) {
                $scope.saveInLocalStorage = function () {
                    localStorageService.set(user.id + '.' + $scope.indigoId + '.columns', JSON.stringify(getColumnsProps($scope.indigoColumns)));
                };
                updateColumns(user);

                if ($attrs.indigoDraggableColumns) {
                    var unsubscribe = $scope.$watch(function () {
                        return _.map($scope.indigoColumns, _.iteratee('id')).join('-');
                    }, $scope.saveInLocalStorage);
                    $scope.$on('$destroy', function () {
                        unsubscribe();
                    });
                }
                $scope.resetColumns = function () {
                    localStorageService.remove(user.id + '.' + $scope.indigoId + '.columns');
                    updateColumns(user);
                };
            });

        var editableCell = null, closePrev;
        that.setClosePrevious = function (_closePrev) {
            closePrev = _closePrev;
        };
        that.toggleEditable = function (columnId, rowIndex) {
            if (closePrev) {
                closePrev();
            }
            editableCell = columnId + '-' + rowIndex;
        };
        that.isEditable = function (columnId, rowIndex) {
            if (that.isFormReadonly()) {
                return;
            }

            if ($scope.indigoEditable) {
                var row = $scope.rowsForDisplay[rowIndex];
                var editable = $scope.indigoEditable(row, columnId);
                if (!editable) {
                    return false;
                }
            }
            if (columnId === null || rowIndex === null) {
                return false;
            }
            return editableCell === columnId + '-' + rowIndex;
        };

        that.isFormReadonly = function () {
            var curForm = EntitiesBrowser.getCurrentForm();
            return (curForm && curForm.$$isReadOnly);
        };

        that.setDirty = function () {
            EntitiesBrowser.setCurrentFormDirty();
        };

        $scope.isColumnReadonly = function (col, rowId) {
            var iseditable = !that.isEditable(col.id, rowId);
            return col.readonly === true || !iseditable;
        };

        var stimeout;
        var originalRows = $scope.indigoRows, lastQ;
        var searchColumns = $scope.indigoSearchColumns || ['id', 'nbkBatch'];
        $scope.filteredRows = originalRows;

        $scope.search = function (q) {
            if (q === undefined) {
                q = $scope.searchText.trim().toLowerCase();
            }
            $scope.searchText = q;
            if (lastQ && lastQ == q || !originalRows) return;
            if (stimeout) $timeout.cancel(stimeout);
            if (!q) {
                lastQ = q;
                $scope.filteredRows = originalRows;
                return;
            }

            stimeout = $timeout(function () {
                var filtered = [];
                originalRows.forEach(function (r) {
                    var rate = 0;
                    searchColumns.forEach(function (sc) {
                        if (!r[sc]) return;
                        var s = r[sc].toString().toLowerCase();
                        if (s.indexOf(q) == 0) {
                            rate += 10;
                        } else if (s.indexOf(q) > 0) {
                            rate++;
                        }
                    });
                    r.$$rate = rate;
                    if (rate > 0) filtered.push(r);
                });
                $scope.filteredRows = $filter('orderBy')(filtered, 'rate', true);
                lastQ = q;
            }, 300);
        };

        $scope.onRowSelect = function ($event, row) {
            var target = $($event.target);
            if ($attrs.indigoTabSupport)
                initTabSupport($event.currentTarget);
            if (target.is('button,span,ul,a,li,input')) {
                return;
            }
            _.each($scope.indigoRows, function (item) {
                if (item !== row) {
                    item.$$selected = false;
                }
            });
            row.$$selected = !row.$$selected;
            if ($scope.indigoOnRowSelected) {
                $scope.indigoOnRowSelected(_.find($scope.indigoRows, function (item) {
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

        unitService.processColumns($scope.indigoColumns, $scope.indigoRows);
        selectService.processColumns($scope.indigoColumns, $scope.indigoRows);
        inputService.processColumns($scope.indigoColumns, $scope.indigoRows);
        scalarService.processColumns($scope.indigoColumns, $scope.indigoRows);
        $scope.pagination = {
            page: 1,
            pageSize: 10
        };
        function calcPages(rows) {
            return _.groupBy(rows, function (element, index) {
                return Math.floor(index / $scope.pagination.pageSize);
            });
        }

        function initTabSupport(tar) {
            var $tar = $(tar);
            var $input = $tar.find('input').focus();
            if (!$input.attr('tab-initiated')) {
                $input.on('keydown', function (e) {
                    if (e.keyCode != 9) return; //tab key
                    var $next = $tar.nextAll('[col-read-only="false"]').filter(function () {
                        return $(this).find('[toggleEditable]')[0];
                    }).eq(0), toggle = $next.find('[toggleEditable]')[0];

                    //console.warn($next[0])
                    if (toggle) {
                        $timeout(function () {
                            angular.element(toggle).triggerHandler('click');
                            initTabSupport($next);
                        });
                    }
                }).attr('tab-initiated', true);
            }
        }

        $scope.onPageChanged = function (page) {
            $scope.pagination.page = page;
        };

        var updateRowsForDisplay = function () {
            var pages = calcPages($scope.filteredRows);
            $scope.rowsForDisplay = pages[$scope.pagination.page - 1];
        };
        $scope.$watch('pagination.page', updateRowsForDisplay);
        $scope.$watchCollection('indigoRows', function (newVal, oldVal) {
            if (newVal && oldVal && newVal.length > oldVal.length) {
                $scope.search('');
            }
            $scope.filteredRows = originalRows = $scope.indigoRows;
        });
        $scope.$watchCollection('filteredRows', function (newVal, oldVal) {
            if (newVal && oldVal && newVal.length > oldVal.length) {
                var pages = calcPages($scope.filteredRows);
                $scope.pagination.page = Object.keys(pages).length;
            }
            updateRowsForDisplay();
        });
    }
})();