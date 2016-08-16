/**
 * Created by Stepan_Litvinov on 3/15/2016.
 */
angular.module('indigoeln')
    .factory('unitService', function ($uibModal, CalculationService) {

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
                    _.each(that.rows, function (row) {
                        if (row.registrationStatus !== 'PASSED' || row.registrationStatus !== 'IN_PROGRESS') {
                            row[id] = row[id] || {};
                            row[id].value = result.value;
                            row[id].unit = result.unit;
                            row[id].entered = true;
                            CalculationService.calculateProductBatch({row: row, column: id});
                        }
                    });
                }, function () {

                });
            }

        };

        return {
            processColumns: function (columns, rows) {
                _.each(columns, function (column) {
                    if (column.type === 'unit') {
                        // uncomment this when needed si on the first place by default
                        // var $uInst = $u();
                        // column.unitItems = _.sortBy(column.unitItems, function (i, index) {
                        //     return $uInst.toBase(i) === i ? -1 : index;
                        // });
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