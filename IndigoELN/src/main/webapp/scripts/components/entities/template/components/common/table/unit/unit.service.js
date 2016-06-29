/**
 * Created by Stepan_Litvinov on 3/15/2016.
 */
angular.module('indigoeln')
    .factory('unitService', function ($uibModal) {

        var setUnit = function (name, item) {
            item.unit = name;
        };

        var toUnitNameAction = function (unit) {
            return {
                name: unit,
                onClick: setUnit.bind(null, unit)
            };
        };

        var toUnitAction = function (rows, unit) {
            return {
                name: 'Set Unit ' + unit,
                action: function (id) {
                    _.each(rows, function (row) {
                        setUnit(unit, row[id]);
                    });
                }
            };
        };

        var setUnitValueAction = {
            action: function (id) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/components/entities/template/components/common/table/unit/set-unit-value.html',
                    controller: 'SetUnitValueController',
                    size: 'sm',
                    resolve: {
                        name: function () {
                            return that.title;
                        },
                        unitNames: function () {
                            return that.units;
                        }
                    }
                }).result.then(function (result) {
                    _.each(that.rows, function (item) {
                        item[id] = item[id] || {};
                        item[id].value = result.value;
                        item[id].unit = result.unit;
                    });
                }, function () {

                });
            }

        };

        return {
            processColumns: function (columns, rows) {
                _.each(columns, function (column) {
                    if (column.type === 'unit') {
                        column.units = _.map(column.unitItems, toUnitNameAction);
                        var setValueAction = [];
                        if (!column.hideSetValue) {
                            setValueAction.push(_.extend({}, setUnitValueAction, {
                                    name: 'Set value for ' + column.name,
                                    title: column.name,
                                    units: column.unitItems,
                                    rows: rows
                                })
                            );
                        }
                        column.actions = (column.actions || [])
                            .concat(setValueAction)
                            .concat(_.map(column.unitItems, toUnitAction.bind(null, rows)));
                    }
                });
            }
        };
    });