/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var template = require('./editable-cell.html');

function editableCell() {
    return {
        restrict: 'E',
        controller: EditableCellController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            isReadonly: '=',
            isEditing: '=',
            onStartEdit: '&',
            onClose: '&'
        },
        template: template
    };
}

EditableCellController.$inject = ['$scope', 'unitsConverter', 'roundFilter', 'notifyService'];

function EditableCellController($scope, unitsConverter, roundFilter, notifyService) {
    var vm = this;
    var oldVal;
    var isChanged;

    init();

    function saveOldValue() {
        if (!vm.row || !vm.column) {
            return;
        }
        oldVal = _.isObject(vm.row[vm.column.id]) ? vm.row[vm.column.id].value : vm.row[vm.column.id];
    }

    function init() {
        vm.column = $scope.$parent.column;
        vm.row = $scope.$parent.$parent.row;
        saveOldValue();

        vm.isCheckEnabled = true;

        vm.unitParsers = [function(viewValue) {
            return +unitsConverter.convert(viewValue, vm.row[vm.column.id].unit).val();
        }];

        vm.unitFormatters = [function(modelValue) {
            return +roundFilter(unitsConverter.convert(modelValue)
                .as(vm.row[vm.column.id].unit)
                .val(), vm.row[vm.column.id].sigDigits, vm.column, vm.row);
        }];

        vm.isEmpty = isEmpty;
        vm.closeThis = closeThis;

        bindEvents();
    }

    function isEmpty(obj, col) {
        if (obj && col.showDefault) {
            return false;
        }

        return _.isEmpty(obj) || obj.value === '0' || obj.value === 0 || obj.value === null;
    }

    function closeThis() {
        var model = vm.row[vm.column.id];
        if (_.isUndefined(model.value)) {
            model.value = vm.column.min || 0;
        }
        if ((vm.column.type === 'scalar' || vm.column.type === 'unit') && isChanged) {
            var absv = Math.abs(model.value);
            if (absv !== model.value) {
                model.value = absv;
                notifyService.error('Total Amount made must more than zero.');
            }
        }
        if (vm.column.type === 'input' && model === '') {
            vm.row[vm.column.id] = undefined;
        }


        if (isChanged) {
            isChanged = false;
        }
        vm.onClose({
            data: {
                model: vm.row[vm.column.id],
                row: vm.row,
                column: vm.column.id,
                oldVal: oldVal
            }
        });
    }

    function bindEvents() {
        if (vm.column.onClose) {
            $scope.$watch(function() {
                return getCellContent(vm.row[vm.column.id]);
            }, function(newVal, prevVal) {
                var col = vm.column;
                oldVal = prevVal;
                isChanged = !_.isEqual(newVal, prevVal) && vm.isEditing && !vm.isReadonly && vm.isCheckEnabled;
                if (isChanged && col.onChange) {
                    col.onChange({
                        row: vm.row, model: vm.row[col.id], oldVal: oldVal
                    });
                }
            }, true);
        }

        $scope.$watch('::vm.column.checkEnabled', function() {
            if (vm.column.checkEnabled) {
                $scope.$watch('vm.column.checkEnabled(vm.row)', function() {
                    vm.isCheckEnabled = vm.column.checkEnabled(vm.row);
                });
            }
        });
        $scope.$watch('vm.isEditing', function() {
            saveOldValue();
        });
    }

    function getCellContent(cell) {
        return _.isObject(cell) ? cell.value || cell.name : cell;
    }
}

module.exports = editableCell;

