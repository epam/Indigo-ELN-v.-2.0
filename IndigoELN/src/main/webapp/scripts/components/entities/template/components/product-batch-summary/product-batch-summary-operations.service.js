angular
    .module('indigoeln')
    .factory('ProductBatchSummaryOperations', productBatchSummaryOperations);

/* @ngInject */
function productBatchSummaryOperations($q, ProductBatchSummaryCache, RegistrationUtil, StoichTableCache,
                                       $log, Alert, $timeout, EntitiesBrowser, RegistrationService, SdImportService,
                                       SdExportService, AlertModal, $http, $stateParams, Notebook, CalculationService) {
    return {
        exportSDFile: exportSDFile,
        getSelectedNonEditableBatches: getSelectedNonEditableBatches,
        duplicateBatches: duplicateBatches,
        duplicateBatch: duplicateBatch,
        setStoicTable: setStoicTable,
        getIntendedNotInActual: getIntendedNotInActual,
        syncWithIntendedProducts: syncWithIntendedProducts,
        addNewBatch: addNewBatch,
        importSDFile: importSDFile,
        registerBatches: registerBatches,
        requestNbkBatchNumberAndAddToTable: requestNbkBatchNumberAndAddToTable,
        deleteBatches: deleteBatches

    };

    function exportSDFile() {
        var batches = ProductBatchSummaryCache.getProductBatchSummary();
        var selectedBatches = _.filter(batches, function(item) {
            return item.select;
        });

        SdExportService.exportItems(selectedBatches).then(function(data) {
            var file_path = 'api/sd/download?fileName=' + data.fileName;
            var a = document.createElement('A');
            a.href = file_path;
            a.download = file_path.substr(file_path.lastIndexOf('/') + 1);
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        });
    }


    function getSelectedNonEditableBatches() {
        var batches = ProductBatchSummaryCache.getProductBatchSummary();

        return _.chain(batches).filter(function(item) {
            return item.select;
        }).filter(function(item) {
            return RegistrationUtil.isRegistered(item);
        }).map(function(item) {
            return item.fullNbkBatch;
        }).value();
    }

    function duplicateBatches(batchesQueueToAdd, i, isSyncWithIntended) {
        if (!batchesQueueToAdd[i]) {
            return;
        }
        var batchToCopy = batchesQueueToAdd[i];
        var batchToDuplicate = angular.copy(batchToCopy);
        var p = requestNbkBatchNumberAndAddToTable(batchToDuplicate, isSyncWithIntended);
        p.then(function() {
            EntitiesBrowser.getCurrentForm().$setDirty(true);
            duplicateBatches(batchesQueueToAdd, i + 1, isSyncWithIntended);
        });

        return p;
    }

    function duplicateBatch() {
        var batches = ProductBatchSummaryCache.getProductBatchSummary();
        var batchesToDuplicate = _.filter(batches, function(item) {
            return item.select;
        });

        return duplicateBatches(batchesToDuplicate, 0);
    }

    function setStoicTable(_table) {
        StoichTableCache.setStoicTable(_table);
    }

    function getIntendedNotInActual() {
        var stoichTable = StoichTableCache.getStoicTable();
        if (stoichTable) {
            var intended = stoichTable.products;
            var intendedCandidateHashes = _.pluck(intended, '$$batchHash');
            var actual = ProductBatchSummaryCache.getProductBatchSummary();
            var actualHashes = _.compact(_.pluck(actual, '$$batchHash'));
            _.each(intendedCandidateHashes, function(intendedCandidateHash, i) {
                removeItemFromBothArrays(intendedCandidateHash, actualHashes, intendedCandidateHashes, i);
            });
            var hashesToAdd = _.compact(intendedCandidateHashes);

            return _.map(hashesToAdd, function(hash) {
                return _.find(intended, {
                    '$$batchHash': hash
                });
            });
        }
    }

    function removeItemFromBothArrays(item, array1, array2, i) {
        if (_.includes(array1, item)) {
            array2[i] = null;
            array1[_.indexOf(array1, item)] = null;
        }
    }

    function syncWithIntendedProducts() {
        var syncingIntendedProducts = $q.defer();
        var batchesQueueToAdd = getIntendedNotInActual();
        var stoichTable = StoichTableCache.getStoicTable();
        if (stoichTable && stoichTable.products && stoichTable.products.length) {
            if (!batchesQueueToAdd.length) {
                syncingIntendedProducts.resolve();
                AlertModal.info('Product Batch Summary is synchronized', 'sm');
            } else {
                return duplicateBatches(batchesQueueToAdd, 0, true);
            }
        }
        syncingIntendedProducts.resolve();
    }

    function addNewBatch() {
        var q = requestNbkBatchNumberAndAddToTable();
        q.then(function() {
            EntitiesBrowser.getCurrentForm().$setDirty(true);
        });

        return q;
    }


    function importSDFile(success) {
        SdImportService.importFile(requestNbkBatchNumberAndAddToTable, null, function() {
            EntitiesBrowser.getCurrentForm().$setDirty(true);
            if (success) {
                success();
            }
        });
    }

    function registerBatches() {
        var nonEditableBatches = getSelectedNonEditableBatches();
        if (nonEditableBatches && nonEditableBatches.length > 0) {
            Alert.warning('Batch(es) ' + _.uniq(nonEditableBatches).join(', ') + ' already have been registered.');

            return registerBatchesWith(nonEditableBatches);
        }

        return registerBatchesWith([]);
    }

    function requestNbkBatchNumberAndAddToTable(duplicatedBatch, isSyncWithIntended) {
        var experiment = EntitiesBrowser.getCurrentEntity();
        var batches = ProductBatchSummaryCache.getProductBatchSummary();
        var latest = getLatestNbkBatch();
        var deferred = $q.defer();
        $http.get('api/projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
                '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest)
            .then(function(result) {
                var batchNumber = result.data.batchNumber;
                Notebook.get({
                    projectId: $stateParams.projectId,
                    notebookId: $stateParams.notebookId
                })
                    .$promise.then(function(notebook) {
                        var fullNbkBatch = notebook.name + '-' + experiment.name + '-' + batchNumber;
                        var fullNbkImmutablePart = notebook.name + '-' + experiment.name + '-';
                        _.each(batches, function(row) {
                            row.$$selected = false;
                        });
                        var batch = {};
                        var stoichTable = StoichTableCache.getStoicTable();
                        if (stoichTable) {
                            batch = angular.copy(CalculationService.createBatch(stoichTable, true));
                        }
                        batch.nbkBatch = batchNumber;
                        batch.fullNbkBatch = fullNbkBatch;
                        batch.fullNbkImmutablePart = fullNbkImmutablePart;
                        batch.$$selected = true;
                        if (duplicatedBatch) {
                            duplicatedBatch.fullNbkBatch = batch.fullNbkBatch;
                            duplicatedBatch.fullNbkImmutablePart = batch.fullNbkImmutablePart;
                            duplicatedBatch.nbkBatch = batch.nbkBatch;
                            duplicatedBatch.conversationalBatchNumber = null;
                            duplicatedBatch.registrationDate = null;
                            duplicatedBatch.registrationStatus = null;
                            if (isSyncWithIntended) {
                            // to sync mapping of intended products with actual poducts
                                duplicatedBatch.theoMoles = duplicatedBatch.mol;
                                duplicatedBatch.theoWeight = duplicatedBatch.weight;
                            // total moles can be calculated when total weight or total Volume are added, or manually
                                duplicatedBatch.mol = null;
                                saveMolecule(duplicatedBatch.structure.molfile).then(function(structureId) {
                                    duplicatedBatch.structure.structureId = structureId;
                                }, function() {
                                    Alert.error('Cannot save the structure!');
                                });
                            }
                            batch = duplicatedBatch;
                        }
                        batches.push(batch);
                        $log.debug(batch);
                        deferred.resolve(batch);
                    });
            });

        return deferred.promise;
    }

    function deleteBatches(selectedBatch) {
        var deleted = 0;
        var batches = ProductBatchSummaryCache.getProductBatchSummary();
        if (!selectedBatch) {
            var nonEditableBatches = getSelectedNonEditableBatches();
            if (nonEditableBatches && nonEditableBatches.length > 0) {
                Alert.error('Following batches were registered or sent to registration and cannot be deleted: ' + _.uniq(nonEditableBatches).join(', '));
            }
            batches.concat([]).forEach(function(b) {
                if (b.select && !RegistrationUtil.isRegistered(b)) {
                    deleted++;
                    batches.splice(batches.indexOf(b), 1);
                }
            });
        } else if (!RegistrationUtil.isRegistered(b)) {
            batches.splice(batches.indexOf(), 1);
        }
        if (deleted > 0) {
            EntitiesBrowser.getCurrentForm().$setDirty(true);
        }

        return deleted;
    }

    function getLatestNbkBatch() {
        var batches = ProductBatchSummaryCache.getProductBatchSummary();

        return batches && batches.length > 0 && batches[batches.length - 1].nbkBatch ? batches[batches.length - 1].nbkBatch : 0;
    }

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

    function registerBatchesWith(excludes) {
        var batches = ProductBatchSummaryCache.getProductBatchSummary();
        var _batches = _.filter(batches, function(row) {
            return row.select && !_.includes(excludes, row.fullNbkBatch);
        });
        var message = '';
        var notFullBatches = RegistrationUtil.getNotFullForRegistrationBatches(_batches);
        if (notFullBatches.length) {
            _.each(notFullBatches, function(notFullBatch) {
                message = message + '<br><b>Batch ' + notFullBatch.nbkBatch + ':</b><br>' + notFullBatch.emptyFields.join('<br>');
            });
            AlertModal.error(message);
        } else {
            var batchNumbers = _.map(_batches, function(batch) {
                return batch.fullNbkBatch;
            });
            if (batchNumbers.length) {
                return saveAndRegister(batchNumbers, function() {
                    _batches.forEach(function(b) {
                        b.registrationStatus = 'IN_PROGRESS'; // EPMLSOPELN-403
                    });
                });
            }
            Alert.warning('No Batches was selected for Registration');
        }
    }

    function saveAndRegister(batchNumbers, success) {
        EntitiesBrowser.saveCurrentEntity()
            .then(function() {
                $timeout(function() {
                    RegistrationService.register({}, batchNumbers).$promise
                    .then(function() {
                        Alert.success('Selected Batches successfully sent to Registration');
                        success();
                    }, function() {
                        Alert.error('ERROR! Selected Batches registration failed');
                    });
                }, 1000);
            });
    }
}
