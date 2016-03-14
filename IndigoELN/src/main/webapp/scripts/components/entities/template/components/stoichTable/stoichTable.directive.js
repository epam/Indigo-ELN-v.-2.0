/**
 * Created by Stepan_Litvinov on 3/10/2016.
 */
'use strict';
angular.module('indigoeln')
    .directive('stoichTable', function () {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/stoichTable/stoichTable.html',
            controller: function ($scope) {
                $scope.reactantsColumns = [
                    {
                        id: 'compoundId',
                        name: 'Compound ID',
                        type: 'input'
                    }, {
                        id: 'casNumber',
                        name: 'CAS Number',
                        type: 'input'
                    }, {
                        id: 'nbkBatch',
                        name: 'Nbk Batch #',
                        type: 'input'
                    }, {
                        id: 'chemicalName',
                        name: 'Chemical Name',
                        type: 'input'
                    }, {
                        id: 'molWeight',
                        name: 'Mol Weight',
                        type: 'input'
                    }, {
                        id: 'weight',
                        name: 'Weight',
                        type: 'input'
                    }, {
                        id: 'volume',
                        name: 'Volume',
                        type: 'input'
                    }, {
                        id: 'mol',
                        name: 'Mol',
                        type: 'input'
                    }, {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'input'
                    }, {
                        id: 'rxnRole',
                        name: 'Rxn Role',
                        type: 'input'
                    }, {
                        id: 'molarity',
                        name: 'Molarity',
                        type: 'input'
                    }, {
                        id: 'purity',
                        name: 'Purity',
                        type: 'input'
                    }, {
                        id: 'molFormula',
                        name: 'Mol Formula',
                        type: 'input'
                    }, {
                        id: 'saltCode',
                        name: 'Salt Code',
                        type: 'input'
                    }, {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'input'
                    }, {
                        id: 'loadFactor',
                        name: 'Load Factor',
                        type: 'input'
                    }, {
                        id: 'hazardComments',
                        name: 'Hazard Comments',
                        type: 'input'
                    }, {
                        id: 'comments',
                        name: 'Comments',
                        type: 'input'
                    }];
                $scope.intendedColumns = [
                    {
                        id: 'chemicalName',
                        name: 'Chemical Name',
                        type: 'input'
                    },
                    {
                        id: 'formula',
                        name: 'Foumula',
                        type: 'input'
                    },
                    {
                        id: 'molWt',
                        name: 'Mol.Wt.',
                        type: 'input'
                    },
                    {
                        id: 'exactMass',
                        name: 'Exact Mass',
                        type: 'input'
                    },
                    {
                        id: 'theoWgt',
                        name: 'Theo. Wgt.',
                        type: 'input'
                    },
                    {
                        id: 'theoMoles',
                        name: 'Theo. Moles',
                        type: 'input'
                    },
                    {
                        id: 'saltCode',
                        name: 'Salt Code',
                        type: 'input'
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ',
                        type: 'input'
                    },
                    {
                        id: 'hazardComments',
                        name: 'Hazard Comments',
                        type: 'input'
                    },
                    {
                        id: 'eq',
                        name: 'EQ',
                        type: 'input'
                    }
                ];
                $scope.onRowSelected = function (row) {
                    $scope.selectedRow = row;
                };
                $scope.rows = [{}, {}];
                $scope.clear = function () {
                    for (var key in $scope.selectedRow) {
                        if ($scope.selectedRow.hasOwnProperty(key)) {
                            if (!_.contains(['$$hashKey', 'selected'], key)) {
                                delete $scope.selectedRow[key];
                            }
                        }
                    }
                };
                $scope.removeRow = function () {
                    $scope.rows = _.without($scope.rows, $scope.selectedRow);
                };
            }
        };
    });