/**
 * Created by Stepan_Litvinov on 3/1/2016.
 */
angular.module('indigoeln')
.directive('myTableVal', function ($sce, roundFilter, Alert, $timeout) {
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

			$scope.toggleEditable = function ($event) {
				var val = myTableCtrl.toggleEditable($scope.myColumn.id, $scope.myRowIndex);
				myTableCtrl.setClosePrevious($scope.closeThis);
				$timeout(function() {
					iElement.find('input').on('keypress', function(e) {
						if (e.keyCode == 13) {
							e.preventDefault();
							if ($scope.isEditable()) {
								$scope.toggleEditable();
								$scope.closeThis()
							}
						}
					})
				}, 0)
				return val;
			};
			$scope.columnClick = function () {
				if (!myTableCtrl.isFormReadonly())
					$scope.myColumn.onClick({model: $scope.myRow[$scope.myColumn.id], row: $scope.myRow, column: $scope.myColumn.id})
			}
			$scope.isEditable = function () {
				var enabled = true;
				if ($scope.myColumn.checkEnabled) {
					enabled = $scope.myColumn.checkEnabled($scope.myRow, $scope.myColumn.id )
				}
				return enabled && myTableCtrl.isEditable($scope.myColumn.id, $scope.myRowIndex);
			};
			$scope.isEmpty = function (obj, col) {
				if (obj && col.showDefault) return false;
				return obj === 0 || _.isNull(obj) || _.isUndefined(obj) ||
					(_.isObject(obj) && (_.isEmpty(obj) || obj.value === 0) || obj.value === '0');
			};
			$scope.closeThis = function () {
				var col = $scope.myColumn;
				var val = $scope.myRow[col.id];
				if ((col.type == 'scalar' || col.type == 'unit') && isChanged) {
					var absv = Math.abs(val.value);
					if (absv!=val.value) {
						val.value = absv;
						Alert.error('Total Amount made must more than zero.')
					}
				}
				if (col.type == 'input' && val === '') {
					$scope.myRow[col.id] = val = undefined;
				} 
				if (col.onClose && isChanged) {
					col.onClose({
						model: $scope.myRow[col.id],
						row: $scope.myRow,
						column: col.id,
						oldVal: oldVal
					});
					isChanged = false;
				}
				myTableCtrl.setClosePrevious(null);
				return myTableCtrl.toggleEditable(null, null, null);;
			};
			var unbinds = [];
			if ($scope.myColumn.onClose) {
				unbinds.push($scope.$watch(function () {
					return _.isObject($scope.myRow[$scope.myColumn.id]) ? $scope.myRow[$scope.myColumn.id].value || $scope.myRow[$scope.myColumn.id].name : $scope.myRow[$scope.myColumn.id];
				}, function (newVal, prevVal) {
					oldVal = prevVal;
					isChanged = !angular.equals(newVal, prevVal) && $scope.isEditable();
					var col = $scope.myColumn;
					if (isChanged) { 
						if (col.onChange)
							col.onChange({ row : $scope.myRow, model: $scope.myRow[col.id], oldVal: oldVal })
					};
				}, true));
			}
			$scope.unitChange = function() {
				myTableCtrl.setDirty()
			}

			// unbinds.push($scope.$watch(function () {
			// 	var cell= $scope.myRow[$scope.myColumn.id];
			// 	if (_.isObject(cell) && cell.unit) return cell.unit;
			// }, function(newVal, prevVal) {
			// 	if (!angular.equals(newVal, prevVal))
			// }, true))

            if ($scope.myColumn.hasStructurePopover) {
				var updatePopover = function () {
					$scope.popoverTitle = $scope.myRow[$scope.myColumn.id];
					var image = $scope.myRow.structure ? $scope.myRow.structure.image : '';
					$scope.popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" style="padding:10px;" ' +
						'src="data:image/svg+xml;base64,' + image + '" alt="Image is unavailable."></div>');
				};
				unbinds.push($scope.$watch(function () {
					return $scope.myRow[$scope.myColumn.id];
				}, updatePopover));
				unbinds.push($scope.$watch(function () {
					return $scope.myRow.structure ? $scope.myRow.structure.image : null;
				}, updatePopover));
			}
			$scope.$on('$destroy', function () {
				_.each(unbinds, function (unbind) {
					unbind();
				});
			});
			$scope.unitParsers = [function (viewValue) {
				return +$u(viewValue, $scope.myRow[$scope.myColumn.id].unit).val();
			}];
			$scope.unitFormatters = [function (modelValue) {
                return +roundFilter($u(modelValue).as($scope.myRow[$scope.myColumn.id].unit).val(), $scope.myRow[$scope.myColumn.id].sigDigits, $scope.myColumn, $scope.myRow);
			}];
			
		},
		templateUrl: 'scripts/components/entities/template/components/common/table/my-table-val.html'
	};

})
.directive('myTable', function () {
	return {
		restrict: 'E',
		replace: true,
		transclude: true,
		scope: {
			myId: '@',
			myLabel: '@',
			myColumns: '=',
			myRows: '=',
			myReadonly: '=',
			myEditable: '=',
			myOnRowSelected: '=',
			myDraggableRows: '=',
			myDraggableColumns: '=',
			myHideColumnSettings:'=',
			mySearchColumns: '=',
		},
        controller: function ($scope, dragulaService, localStorageService, $attrs, unitService, selectService, inputService, scalarService, Principal, $timeout, $filter, EntitiesBrowser) {
			var that = this;
			function getColumnsProps(myColumns) {
				return _.map(myColumns, function (column) {
					column.isVisible = _.isUndefined(column.isVisible) ? true : column.isVisible;
					return {id: column.id, isVisible: column.isVisible};
				});
			}

			var originalColumnIdsAndFlags = getColumnsProps($scope.myColumns);

			function updateColumns(user) {
				var columnIdsAndFlags = JSON.parse(localStorageService.get(user.id + '.' + $scope.myId + '.columns'));
				if (!columnIdsAndFlags) {
					$scope.saveInLocalStorage();
				}
				columnIdsAndFlags = columnIdsAndFlags || originalColumnIdsAndFlags;

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
			}

			Principal.identity()
			.then(function (user) {
				$scope.saveInLocalStorage = function () {
					localStorageService.set(user.id + '.' + $scope.myId + '.columns', JSON.stringify(getColumnsProps($scope.myColumns)));
				};
				updateColumns(user);

				if ($attrs.myDraggableColumns) {
					var unsubscribe = $scope.$watch(function () {
						return _.map($scope.myColumns, _.iteratee('id')).join('-');
					}, $scope.saveInLocalStorage);
					$scope.$on('$destroy', function () {
						unsubscribe();
					});
				}
				$scope.resetColumns = function () {
					localStorageService.remove(user.id + '.' + $scope.myId + '.columns');
					updateColumns(user);
				};
			});

			var editableCell = null, closePrev;
			that.setClosePrevious = function(_closePrev) {
				closePrev = _closePrev; 
			}
			that.toggleEditable = function (columnId, rowIndex) {
				if (closePrev) closePrev();
				editableCell = columnId + '-' + rowIndex;
			};
			that.isEditable = function (columnId, rowIndex) {
				if (that.isFormReadonly()) return;
				if ($scope.myEditable) {
					var row = $scope.rowsForDisplay[rowIndex];
					var editable = $scope.myEditable(row, columnId);
					if (!editable) {
						return false;
					}
				}
				if (columnId === null || rowIndex === null) {
					return false;
				}
				return editableCell === columnId + '-' + rowIndex;
			};

			that.isFormReadonly = function() {
				var curForm = EntitiesBrowser.getCurrentForm()
				return  (curForm && curForm.$$isReadOnly);
			}
				
			that.setDirty = function() {
				EntitiesBrowser.setCurrentFormDirty();
			}
			
			$scope.isColumnReadonly = function(col, rowId) {
				var iseditable = !that.isEditable(col.id, rowId);
				return col.readonly === true || !iseditable;
			} 

			var stimeout;
			var originalRows = $scope.myRows, lastQ;
			var searchColumns = $scope.mySearchColumns || ['id', 'nbkBatch']
			$scope.filteredRows =  originalRows;
			
			$scope.search =	function(q) {
				if (q === undefined) {
					q = $scope.searchText.trim().toLowerCase();
				}
				$scope.searchText = q;
				if (lastQ && lastQ  == q || !originalRows) return;
				if (stimeout) $timeout.cancel(stimeout)
				if (!q) {
					lastQ  = q;
					$scope.filteredRows = originalRows;
					return;
				};
				stimeout = $timeout(function() {
					var filtered = [];
					originalRows.forEach(function(r) {
						var rate = 0;
						searchColumns.forEach(function(sc) {
							if (!r[sc]) return;
							var s = r[sc].toString().toLowerCase();
							if (s.indexOf(q) == 0) {
								rate+=10;
							} else if  (s.indexOf(q) > 0) {
								rate++;
							}
						})
						r.$$rate = rate;
						if (rate > 0) filtered.push(r)
					})
					$scope.filteredRows = $filter('orderBy')(filtered, 'rate', true);
					lastQ = q;
				}, 300)
			}

			$scope.onRowSelect = function($event, row) {
			    var target = $($event.target);
			    if ($attrs.myTabSupport)
			        initTabSupport($event.currentTarget);
			    if (target.is('button,span,ul,a,li,input')) {
			        return;
			    }
			    _.each($scope.myRows, function(item) {
			        if (item !== row) {
			            item.$$selected = false;
			        }
			    });
			    row.$$selected = !row.$$selected;
			    if ($scope.myOnRowSelected) {
			        $scope.myOnRowSelected(_.find($scope.myRows, function(item) {
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
            inputService.processColumns($scope.myColumns, $scope.myRows);
            scalarService.processColumns($scope.myColumns, $scope.myRows);
			$scope.pagination = {
				page: 1,
				pageSize: 10
			};
			function calcPages(rows) {
				return _.groupBy(rows, function (element, index) {
					return Math.floor(index / $scope.pagination.pageSize);
				});
			}

			function initTabSupport(tar)  {
				var $tar = $(tar);
				var $input = $tar.find('input').focus();
				if (!$input.attr('tab-initiated')) {
				    $input.on('keydown', function(e) {
				        if (e.keyCode != 9) return; //tab key
				        var $next = $tar.nextAll('[col-read-only="false"]').filter(function() {
				        	return $(this).find('[toggleEditable]')[0];
				        }).eq(0), toggle = $next.find('[toggleEditable]')[0];

				        //console.warn($next[0])
				        if (toggle) {
				            $timeout(function() {
				                angular.element(toggle).triggerHandler('click');
				        		initTabSupport($next)
				            })
				        }
				    }).attr('tab-initiated', true)
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
			$scope.$watchCollection('myRows', function (newVal, oldVal) {
				if (newVal && oldVal && newVal.length > oldVal.length) {
					$scope.search('')
				} 
				$scope.filteredRows =  originalRows =  $scope.myRows; 
			})
			$scope.$watchCollection('filteredRows', function (newVal, oldVal) {
				if (newVal && oldVal && newVal.length > oldVal.length) {
					var pages = calcPages($scope.filteredRows);
					$scope.pagination.page = Object.keys(pages).length;
				}
				updateRowsForDisplay();
			});

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
			return {
				post: function (scope, element, attrs, ctrl, transclude) {
					element.find('.transclude').replaceWith(transclude());
				}
			};
		},
		templateUrl: 'scripts/components/entities/template/components/common/table/my-table.html'
	};

});