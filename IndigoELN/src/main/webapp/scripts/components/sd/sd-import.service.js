angular.module('indigoeln')
    .factory('SdImportService', function ($http, $q, $uibModal, AppValues, Dictionary, AlertModal, Alert, CalculationService, StoichTableCache, sdPropertiesInfo) {

        var auxPrefixes = [
            'COMPOUND_REGISTRATION_'
        ];

        var getItem = function (list, prop, value) {
            return _.find(list, function (item) {
                return item[prop].toUpperCase() === value.toUpperCase();
            });
        };

        var getWord = function (dicts, dictName, prop, value) {
            var dict = _.find(dicts, function (dict) {
                return dict.name === dictName;
            });
            if (dict) {
                return getItem(dict.words, prop, value);
            }
        };

        var generateImportProperties = function(){

            var keys = Object.keys(sdPropertiesInfo);
            var properties = {};

            keys.forEach(function(key, i, keys){
                if (typeof(sdPropertiesInfo[key]) === 'object') {
                    properties[sdPropertiesInfo[key].prop] = {code: key, format: sdPropertiesInfo[key].format};
                } else {
                    properties[sdPropertiesInfo[key]] = {code: key};
                }
            });

            return properties;
        };


        var properties = generateImportProperties();
//        var properties = {
//            'registrationStatus': {code: 'REGISTRATION_STATUS'},
//            'conversationalBatchNumber': {code: 'CONVERSATIONAL_BATCH_NUMBER'},
//            'virtualCompoundId': {code: 'VIRTUAL_COMPOUND_ID'},
//            'source': {
//                format: function (dicts, value) {
//                    return getWord(dicts, 'Source', 'name', value);
//                },
//                code: 'COMPOUND_SOURCE_CODE'
//            },
//            'sourceDetail': {
//                format: function (dicts, value) {
//                    return getWord(dicts, 'Source Details', 'name', value);
//                },
//                code: 'COMPOUND_SOURCE_DETAIL_CODE'
//            },
//            'stereoisomer': {
//                format: function (dicts, value) {
//                    return getWord(dicts, 'Stereoisomer Code', 'name', value);
//                },
//                code: 'STEREOISOMER_CODE'
//            },
//            'saltCode': {
//                format: function (dicts, value) {
//                    return getItem(AppValues.getSaltCodeValues(), 'regValue', value);
//                },
//                code: 'GLOBAL_SALT_CODE'
//            },
//            'saltEq': {
//                format: function (dicts, value) {
//                    return {
//                        value: parseInt(value),
//                        entered: false
//                    };
//                },
//                code: 'GLOBAL_SALT_EQ'
//            },
//            'structureComments': {code: 'STRUCTURE_COMMENT'},
//            'compoundState': {
//                format: function (dicts, value) {
//                    return getWord(dicts, 'Compound State', 'name', value);
//                },
//                code: 'COMPOUND_STATE'
//            },
//            'precursors': {code: 'PRECURSORS'}
//        };

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
                    if (value && _.isFunction(property.format)) {
                        value = property.format(dicts, value);
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

    });
