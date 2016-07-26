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
				return obj === 0 || _.isNull(obj) || _.isUndefined(obj) ||
					(_.isObject(obj) && (_.isEmpty(obj) || obj.value === 0) || obj.value === '0');
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
			var unbinds = [];
			if ($scope.myColumn.onClose) {
				unbinds.push($scope.$watch(function () {
					return _.isObject($scope.myRow[$scope.myColumn.id]) ? $scope.myRow[$scope.myColumn.id].value || $scope.myRow[$scope.myColumn.id].name : $scope.myRow[$scope.myColumn.id];
				}, function (newVal, prevVal) {
					if (_.isObject($scope.myRow[$scope.myColumn.id])) {
						$scope.myRow[$scope.myColumn.id].displayValue = +roundFilter(newVal, $scope.myRow[$scope.myColumn.id].sigDigits, $scope.myRow[$scope.myColumn.id].entered);
					}
					oldVal = prevVal;
					isChanged = !angular.equals(newVal, prevVal) && $scope.isEditable();
				}, true));
			}
			if ($scope.myColumn.hasPopover) {
				unbinds.push($scope.$watch(function () {
					return $scope.myRow[$scope.myColumn.id];
				}, function () {
					$scope.popoverTitle = $scope.myRow[$scope.myColumn.id];
					var image = $scope.myRow.structure ? $scope.myRow.structure.image : '';
					$scope.popoverTemplate = $sce.trustAsHtml('<div><img class="img-fill" style="padding:10px;" ' +
						'src="data:image/svg+xml;base64,' + image + '" alt="Image is unavailable."></div>');
				}));
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
				return +roundFilter($u(modelValue).as($scope.myRow[$scope.myColumn.id].unit).val(), $scope.myRow[$scope.myColumn.id].sigDigits, $scope.myRow[$scope.myColumn.id].entered);
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
			myDraggableColumns: '='

		},
		controller: function ($scope, dragulaService, localStorageService, $attrs, unitService, selectService, Principal) {
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

			var editableCell = null;
			that.toggleEditable = function (columnId, rowIndex) {
				editableCell = columnId + '-' + rowIndex;
			};
			that.isEditable = function (columnId, rowIndex) {
				if ($scope.myEditable) {
					var row = $scope.myRows[rowIndex];
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
			$scope.pagination = {
				page: 1,
				pageSize: 10
			};
			function calcPages(rows) {
				return _.groupBy(rows, function (element, index) {
					return Math.floor(index / $scope.pagination.pageSize);
				});
			}

			$scope.onPageChanged = function (page) {
				$scope.pagination.page = page;
			};
			var updateRowsForDisplay = function () {
				var pages = calcPages($scope.myRows);
				$scope.rowsForDisplay = pages[$scope.pagination.page - 1];
			};
			$scope.$watch('pagination.page', updateRowsForDisplay);
			$scope.$watchCollection('myRows', function (newVal, oldVal) {
				if (newVal && oldVal && newVal.length > oldVal.length) {
					var pages = calcPages($scope.myRows);
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