angular.module('indigoeln')
    .factory('SdImportService', function ($http, $q, $uibModal, Dictionary, AlertModal) {

        var properties = {
            'stereoisomer': {
                type: {
                    dict: 'Stereoisomer Code'
                },
                code: 'STEREOISOMER_CODE'
            }
        };

        var getWord = function (dicts, dictDescription, wordName) {
            var dict = _.find(dicts, function (dict) {
                return dict.description === dictDescription;
            });
            if (dict) {
                return _.find(dict.words, function (word) {
                    return word.name === wordName;
                });
            }
        };

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

        var render = function (mol) {
            var deferred = $q.defer();
            $http({
                url: 'api/renderer/molecule/image',
                method: 'POST',
                data: mol
            }).success(function (result) {
                deferred.resolve(result);
            });
            return deferred.promise;
        };

        var fillProperties = function (sdUnitToImport, itemToImport, dicts) {
            if (sdUnitToImport.properties) {
                _.each(properties, function (property, name) {
                    var value = sdUnitToImport.properties[property.code];
                    if (_.isObject(property.type) && property.type.dict) {
                        value = getWord(dicts, property.type.dict, value);
                    }
                    itemToImport[name] = value;
                });
            }
        };

        var importItems = function (sdUnitsToImport, dicts, i, addToTable, callback) {
            if (!sdUnitsToImport[i]) {
                return;
            }
            var sdUnitToImport = sdUnitsToImport[i];
            saveMolecule(sdUnitToImport.mol).then(function (structureId) {
                render(sdUnitToImport.mol).then(function (result) {
                    var itemToImport = {};
                    itemToImport.structure = itemToImport.structure || {};
                    itemToImport.structure.image = result.image;
                    itemToImport.structure.structureType = 'molecule';
                    itemToImport.structure.molfile = sdUnitToImport.mol;
                    itemToImport.structure.structureId = structureId;

                    fillProperties(sdUnitToImport, itemToImport, dicts);
                    addToTable(itemToImport).then(function (batch) {
                        if (callback && _.isFunction(callback)) {
                            callback(batch);
                        }
                        importItems(sdUnitsToImport, dicts, i + 1, addToTable, callback);
                    });
                });
            });
        };

        var importFile = function (addToTable, callback) {
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
                        importItems(result, dicts, 0, addToTable, callback);
                    });
                }, function () {
                    AlertModal.error('This file cannot be imported. Error occurred.');
                });
        };

        return {
            importFile: importFile
        };

    });
