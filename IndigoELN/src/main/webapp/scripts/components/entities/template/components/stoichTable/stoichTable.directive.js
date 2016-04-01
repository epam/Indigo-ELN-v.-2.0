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
            controller: function ($scope, $http, $q, $uibModal) {
                $scope.model = $scope.model || {};
                $scope.model.stoichTable = $scope.model.stoichTable || {};
                $scope.model.stoichTable.reactants = $scope.model.stoichTable.reactants || [];

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
                        name: 'Chemical Name'
                    },
                    {
                        id: 'formula',
                        name: 'Formula'
                    },
                    {
                        id: 'molWt',
                        name: 'Mol.Wt.'
                    },
                    {
                        id: 'exactMass',
                        name: 'Exact Mass'
                    },
                    {
                        id: 'theoWgt',
                        name: 'Theo. Wgt.'
                    },
                    {
                        id: 'theoMoles',
                        name: 'Theo. Moles'
                    },
                    {
                        id: 'saltCode',
                        name: 'Salt Code'
                    },
                    {
                        id: 'saltEq',
                        name: 'Salt EQ'
                    },
                    {
                        id: 'hazardComments',
                        name: 'Hazard Comments'
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
                $scope.$watch('share.reaction', function (newMolFile) {
                    var resetMolInfo = function () {
                        $scope.model.stoichTable.products = null;
                    };
                    if (newMolFile) {

                        $http.put('api/calculations/reaction/extract', newMolFile).then(function (reactionProperties) {
                                if (reactionProperties.data.products && reactionProperties.data.products.length) {
                                    var promises = [];
                                    _.each(reactionProperties.data.products, function (product) {
                                        promises.push($http.put('api/calculations/molecule/info', product));
                                    });
                                    $q.all(promises).then(function (results) {
                                        $scope.model.stoichTable.products = _.map(results, function (result) {
                                            return {
                                                chemicalName: result.data.name,
                                                formula: result.data.molecularFormula,
                                                molWt: result.data.molecularWeight,
                                                exactMass: result.data.exactMolecularWeight,
                                                saltEq:  result.data.saltEq,
                                                saltCode: result.data.saltCode
                                            };
                                        });

                                    });
                                }
                            }
                        );
                    } else {
                        resetMolInfo();
                    }
                });

                $scope.searchReagents = function (activeTab) {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'SearchReagentsController',
                        templateUrl: 'scripts/components/entities/template/components/common/search-reagents/search-reagents.html',
                        resolve: {
                            activeTab: function() {
                                return activeTab;
                            }
                        }
                    }).result.then(function (result) {

                    });
                };
            }
        };
    })
;