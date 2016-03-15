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
                $scope.model = $scope.model || {};
                $scope.model.stoichTable = $scope.model.stoichTable || {};
                $scope.model.stoichTable.reactants = $scope.model.stoichTable.reactants || [];
                $scope.products = {};
                $scope.model.stoichTable.products = $scope.model.stoichTable.products || [$scope.products];

                var grams = ['mg', 'g', 'kg'];
                var liters = ['ul', 'ml', 'l'];
                var moles = ['umol', 'mmol', 'mol'];
                var molarity = ['mM', 'M'];
                var rxnValues = [{name: 'REACTANT'}, {name: 'REAGENT'}, {name: 'SOLVENT'}];
                var saltCodeValues = [{name: '00 - Parent'}, {name: '01 - Salt1(MW-10)'}, {name: '02 - Salt2(MW-20)'}];
                var loadFactorUnits = ['mmol/g'];
                $scope.reactantsColumns = [
                    {
                        id: 'compoundId',
                        name: 'Compound ID',
                        type: 'input'
                    }, {
                        id: 'casNumber',
                        name: 'CAS Number'
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
                        name: 'Mol Weight'
                    }, {
                        id: 'weight',
                        name: 'Weight',
                        type: 'unit',
                        unitItems: grams
                    }, {
                        id: 'volume',
                        name: 'Volume',
                        type: 'unit',
                        unitItems: liters
                    }, {
                        id: 'mol',
                        name: 'Mol',
                        unitItems: moles
                    }, {
                        id: 'limiting',
                        name: 'Limiting',
                        type: 'boolean'
                    }, {
                        id: 'rxnRole',
                        name: 'Rxn Role',
                        type: 'select',
                        values: function () {
                            return rxnValues;
                        }
                    }, {
                        id: 'molarity',
                        name: 'Molarity',
                        type: 'unit',
                        unitItems: molarity
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
                        type: 'select',
                        values: function () {
                            return saltCodeValues;
                        }
                    }, {
                        id: 'saltEq',
                        name: 'Salt EQ'
                    }, {
                        id: 'loadFactor',
                        name: 'Load Factor',
                        type: 'unit',
                        unitItems: loadFactorUnits
                    }, {
                        id: 'hazardComments',
                        name: 'Hazard Comments',
                        type: 'input'
                    }, {
                        id: 'comments',
                        name: 'Comments',
                        type: 'input'
                    }];
                $scope.productsColumns = [
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