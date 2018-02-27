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

var template = require('./set-unit-value.html');

unitService.$inject = ['$uibModal', 'calculationHelper', 'registrationUtil', 'batchesCalculation'];

function unitService($uibModal, calculationHelper, registrationUtil, batchesCalculation) {
    return {
        getActions: function(name, unitItems) {
            var actions = [{
                name: 'Set value for ' + name,
                title: name,
                units: unitItems,
                action: function(rows, column) {
                    unitAction(rows, name, column, unitItems);
                }
            }];

            _.map(unitItems, function(unit) {
                actions.push(toUnitAction(unit));
            });

            return actions;
        }
    };

    function unitAction(rows, title, column, units) {
        var columnId = column.id;

        return $uibModal.open({
            template: template,
            controller: 'SetUnitValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                },
                unitNames: function() {
                    return units;
                }
            }
        }).result.then(function(result) {
            _.each(rows, function(row) {
                if (!registrationUtil.isRegistered(row)) {
                    row[columnId] = row[columnId] || {};
                    row[columnId].value = result.value;
                    row[columnId].unit = result.unit;
                    row[columnId].entered = true;
                    var calculatedRow = batchesCalculation.calculateRow({changedRow: row, changedField: columnId});
                    calculationHelper.updateViewRow(calculatedRow, row);
                }
            });
        });
    }

    function setUnit(name, item) {
        item.unit = name;
    }

    function toUnitAction(unit) {
        return {
            name: 'Set Unit ' + unit,
            action: function(rows, column) {
                _.each(rows, function(row) {
                    setUnit(unit, row[column.id]);
                });
            }
        };
    }
}

module.exports = unitService;
