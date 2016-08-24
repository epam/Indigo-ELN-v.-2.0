/**
 * Created by Stepan_Litvinov on 3/15/2016.
 */
angular.module('indigoeln')
    .factory('inputService', function ($uibModal, RegistrationUtil) {

        var setInputValueAction = {
            action: function (id) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/components/entities/template/components/common/table/input/set-input-value.html',
                    controller: 'SetInputValueController',
                    size: 'sm',
                    resolve: {
                        name: function () {
                            return that.title;
                        }
                    }
                }).result.then(function (result) {
                    _.each(that.rows, function (row) {
                        if (!RegistrationUtil.isRegistered(row)) {
                            row[id] = result;
                        }
                    });
                }, function () {

                });
            }

        };
        return {
            processColumns: function (columns, rows) {
                _.each(columns, function (column) {
                    if (column.type === 'input' && column.bulkAssignment) {
                        column.actions = (column.actions || [])
                            .concat([_.extend({}, setInputValueAction, {
                                name: 'Set value for ' + column.name,
                                title: column.name,
                                rows: rows
                            })]);
                    }
                });
            }
        };
    });