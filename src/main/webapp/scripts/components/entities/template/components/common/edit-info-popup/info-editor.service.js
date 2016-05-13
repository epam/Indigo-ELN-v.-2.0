angular.module('indigoeln')
    .factory('InfoEditor', function ($uibModal) {
        var selectFromDictionary = function (dictionary, model, title, callback) {
            $uibModal.open({
                animation: true,
                size: 'sm',
                controller: 'SelectFromDictionaryController',
                templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/select-from-dictionary/select-from-dictionary.html',
                resolve: {
                    data: function () {
                        return model;
                    },
                    dictionary: function (Dictionary) {
                        return Dictionary.get({id: dictionary}).$promise;
                    },
                    title: function () {
                        return title;
                    }
                }
            }).result.then(function (result) {
                callback(result);
            });
        };

        return {
            editSolubility: function (data, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'lg',
                    controller: 'EditSolubilityController',
                    templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-solubility/edit-solubility.html',
                    resolve: {
                        data: function () {
                            return data;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            },
            editResidualSolvents: function (data, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'lg',
                    controller: 'EditResidualSolventsController',
                    templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-residual-solvents/edit-residual-solvents.html',
                    resolve: {
                        data: function () {
                            return data;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            },
            editExternalSupplier: function (data, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'md',
                    controller: 'EditExternalSupplierController',
                    templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-external-supplier/edit-external-supplier.html',
                    resolve: {
                        data: function () {
                            return data;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            },
            editMeltingPoint: function (data, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'md',
                    controller: 'EditMeltingPointController',
                    templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-melting-point/edit-melting-point.html',
                    resolve: {
                        data: function () {
                            return data;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            },
            editPurity: function (data, callback) {
                $uibModal.open({
                    animation: true,
                    size: 'lg',
                    controller: 'EditPurityController',
                    templateUrl: 'scripts/components/entities/template/components/common/edit-info-popup/edit-purity/edit-purity.html',
                    resolve: {
                        data: function () {
                            return data;
                        },
                        dictionary: function (Dictionary) {
                            return Dictionary.get({id: 'purity'}).$promise;
                        }
                    }
                }).result.then(function (result) {
                    callback(result);
                });
            },
            editHealthHazards: function (data, callback) {
                var dictionary = 'healthHazards';
                var title = 'Edit Health Hazards';
                selectFromDictionary(dictionary, data, title, callback);
            },
            editHandlingPrecautions: function (data, callback) {
                var dictionary = 'handlingPrecautions';
                var title = 'Edit Handling Precautions';
                selectFromDictionary(dictionary, data, title, callback);
            },
            editStorageInstructions: function (data, callback) {
                var dictionary = 'storageInstructions';
                var title = 'Edit Storage Instructions';
                selectFromDictionary(dictionary, data, title, callback);
            }
        };
    });