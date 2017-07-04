angular
    .module('indigoeln')
    .factory('SdImportService', sdImportService);

/* @ngInject */
function sdImportService($http, $q, $uibModal, AppValues, Dictionary, SdConstants,
                         AlertModal, Alert, CalculationService, StoichTableCache, sdProperties, SdImportHelperService) {
    var auxPrefixes = [
        'COMPOUND_REGISTRATION_'
    ];

    return {
        importFile: importFile
    };

    function saveMolecule(mol) {
        var deferred = $q.defer();
        $http({
            url: 'api/bingodb/molecule/',
            method: 'POST',
            data: mol
        }).success(function(structureId) {
            deferred.resolve(structureId);
        });

        return deferred.promise;
    }

    function importValues(sdUnitToImport, property, index, dicts, itemToImport) {
        var propCode = getPropertyCode(property, index);
        var value = sdUnitToImport.properties[propCode];
        if (!value) {
            value = _.chain(auxPrefixes)
                .map(function(auxPrefix) {
                    return auxPrefix + property.code;
                })
                .map(function(code) {
                    return sdUnitToImport.properties[code];
                })
                .find(function(val) {
                    return !_.isUndefined(val);
                })
                .value();
        }
        if (value) {
            var formattedProperty = SdImportHelperService.formatProperty(property, value, dicts, index);
            if (itemToImport[property.name]) {
                _.defaultsDeep(itemToImport[property.name], formattedProperty);
            } else {
                itemToImport[property.name] = formattedProperty;
            }
        }
    }

    function fillProperties(sdUnitToImport, itemToImport, dicts) {
        if (sdUnitToImport.properties) {
            _.each(SdConstants.sdProperties, function(property) {
                if (property.childrenLength) {
                    for (var i = 0; i < property.childrenLength; i++) {
                        importValues(sdUnitToImport, property, i, dicts, itemToImport);
                    }
                }
                importValues(sdUnitToImport, property, null, dicts, itemToImport);
            });
        }
    }

    function getPropertyCode(property, index) {
        if (_.isNull(index)) {
            return property.code;
        }

        return property.code + '_' + index;
    }

    function importItems(sdUnitsToImport, dicts, i, addToTable, callback, complete) {
        if (!sdUnitsToImport[i]) {
            if (complete) {
                complete();
            }
            Alert.info(sdUnitsToImport.length + ' batches successfully imported');

            return;
        }
        var sdUnitToImport = sdUnitsToImport[i];
        saveMolecule(sdUnitToImport.mol).then(function(structureId) {
            CalculationService.getImageForStructure(sdUnitToImport.mol, 'molecule', function(result) {
                var stoichTable = StoichTableCache.getStoicTable();
                var itemToImport = angular.copy(CalculationService.createBatch(stoichTable, true));
                itemToImport.structure = itemToImport.structure || {};
                itemToImport.structure.image = result;
                itemToImport.structure.structureType = 'molecule';
                itemToImport.structure.molfile = sdUnitToImport.mol;
                itemToImport.structure.structureId = structureId;

                fillProperties(sdUnitToImport, itemToImport, dicts);
                CalculationService.recalculateSalt(itemToImport).then(function() {
                    addToTable(itemToImport).then(function(batch) {
                        if (callback && _.isFunction(callback)) {
                            callback(batch);
                        }
                        importItems(sdUnitsToImport, dicts, i + 1, addToTable, callback, complete);
                    });
                });
            });
        });
    }

    function importFile(addToTable, callback, complete) {
        $uibModal.open({
            animation: true,
            size: 'lg',
            templateUrl: 'scripts/components/fileuploader/single-file-uploader/single-file-uploader-modal.html',
            controller: 'SingleFileUploaderController',
            controllerAs: 'vm',
            resolve: {
                url: function() {
                    return 'api/sd/import';
                }
            }
        }).result.then(function(result) {
            Dictionary.all({}, function(dicts) {
                importItems(result, dicts, 0, addToTable, callback, complete);
            });
        }, function() {
            complete();
            AlertModal.error('This file cannot be imported. Error occurred.');
        });
    }
}
