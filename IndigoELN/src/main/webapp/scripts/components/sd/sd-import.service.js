angular
    .module('indigoeln')
    .factory('SdImportService', sdImportService);

/* @ngInject */
function sdImportService($http, $q, $uibModal, AppValues, Dictionary,
                         AlertModal, Alert, CalculationService, StoichTableCache, sdProperties) {

        var auxPrefixes = [
            'COMPOUND_REGISTRATION_'
        ];

        var getImportProperties = function() {
           var properties = {};
           sdProperties.constants.forEach(function(prop, i, constants) {
               var fields = prop.import;
               if (fields.format) {
                   properties[fields.name] = {code : fields.code,
                                              format : fields.format};
               } else {
                   properties[fields.name] = {code : fields.code};
               }
           });

           return properties;
        };
        var properties = getImportProperties();
        console.log("Import properties: ", properties);

        var saveMolecule = function (mol) {
            var deferred = $q.defer();
            $http({
                url: 'api/bingodb/molecule/',
                method: 'POST',
                data: mol
            }).success(function (structureId) {
                deferred.resolve(structureId);
            });
            return deferred.promise;
        };

        var fillProperties = function (sdUnitToImport, itemToImport, dicts) {
            if (sdUnitToImport.properties) {
                _.each(properties, function (property, name) {
                    var value = sdUnitToImport.properties[property.code];
                    if (!value) {
                        value = _.chain(auxPrefixes)
                            .map(function (auxPrefix) {
                                return auxPrefix + property.code;
                            })
                            .map(function (code) {
                                return sdUnitToImport.properties[code];
                            })
                            .find(function (val) {
                                return !_.isUndefined(val);
                            }).
                            value();
                    }
                    if (value && _.isFunction(property.format)) {console.log('value1: ', value);
                        value = property.format(dicts, value);console.log('value2: ', value);
                    }
                    if (value) {
                        itemToImport[name] = value;
                    }
                });
            }
        };

        var importItems = function (sdUnitsToImport, dicts, i, addToTable, callback, complete) {
            if (!sdUnitsToImport[i]) {
                if (complete) complete();
                Alert.info(sdUnitsToImport.length + ' batches successfully imported');
                return;
            }
            var sdUnitToImport = sdUnitsToImport[i];
            saveMolecule(sdUnitToImport.mol).then(function (structureId) {
                CalculationService.getImageForStructure(sdUnitToImport.mol, 'molecule', function (result) {
                    var stoichTable = StoichTableCache.getStoicTable();
                    var itemToImport = angular.copy(CalculationService.createBatch(stoichTable, true));
                    itemToImport.structure = itemToImport.structure || {};
                    itemToImport.structure.image = result;
                    itemToImport.structure.structureType = 'molecule';
                    itemToImport.structure.molfile = sdUnitToImport.mol;
                    itemToImport.structure.structureId = structureId;

                    fillProperties(sdUnitToImport, itemToImport, dicts);
                    CalculationService.recalculateSalt(itemToImport).then(function () {
                        addToTable(itemToImport).then(function (batch) {
                            if (callback && _.isFunction(callback)) {
                                callback(batch);
                            }
                            importItems(sdUnitsToImport, dicts, i + 1, addToTable, callback, complete);
                        });
                    });
                });
            });
        };

        var importFile = function (addToTable, callback, complete) {
            $uibModal.open({
                animation: true,
                size: 'lg',
                templateUrl: 'scripts/components/fileuploader/single-file-uploader/single-file-uploader-modal.html',
                controller: 'SingleFileUploaderController',
                resolve: {
                    url: function () {
                        return 'api/sd/import';
                    }
                }
            }).result.then(function (result) {
                    Dictionary.all({}, function (dicts) {
                        importItems(result, dicts, 0, addToTable, callback, complete);
                    });
                }, function () {
                    complete();
                    AlertModal.error('This file cannot be imported. Error occurred.');
                });
        };

        return {
            importFile: importFile
        };
}
