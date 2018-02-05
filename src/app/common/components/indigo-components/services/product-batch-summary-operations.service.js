var BatchViewRow = require('../directives/indigo-components/domain/batch-row/view-row/batch-view-row');
var fieldTypes = require('../services/calculation/field-types');

/* @ngInject */
function productBatchSummaryOperations($q, productBatchSummaryCache, registrationUtil, calculationHelper,
                                       notifyService, batchHelper,
                                       registrationService, sdImportService,
                                       sdExportService, alertModal, $http, $stateParams, notebookService,
                                       calculationService, apiUrl, $document) {
    var curNbkOperation = $q.when();

    return {
        exportSDFile: exportSDFile,
        duplicateBatches: chainPromises(duplicateBatches),
        getIntendedNotInActual: getIntendedNotInActual,
        syncWithIntendedProducts: syncWithIntendedProducts,
        addNewBatch: chainPromises(addNewBatch),
        importSDFile: chainPromises(importSDFile),
        registerBatches: registerBatches,
        deleteBatches: deleteBatches,
        getSelectedNonEditableBatches: getNonEditableBatches,
        getStoichFromExperiment: getStoichFromExperiment
    };

    function downloadLink(filePath) {
        var a = $document[0].createElement('A');
        a.href = filePath;
        a.download = filePath.substr(filePath.lastIndexOf('/') + 1);
        $document[0].body.appendChild(a);
        a.click();
        $document[0].body.removeChild(a);
    }

    function exportSDFile(exportBatches) {
        var selectedBatches = !_.isArray(exportBatches) ?
            [exportBatches] :
            _.filter(exportBatches, '$$select');

        sdExportService.exportItems(selectedBatches).then(function(data) {
            downloadLink(apiUrl + 'sd/download?fileName=' + data.fileName);
        });
    }

    function getNonEditableBatches(batches) {
        return _.map(_.filter(batches, registrationUtil.isRegistered), 'fullNbkBatch');
    }

    function chainPromises(fn) {
        return function() {
            var args = arguments;
            curNbkOperation = curNbkOperation.then(function() {
                return fn.apply(this, args);
            }, function() {
                return fn.apply(this, args);
            });

            return curNbkOperation;
        };
    }

    function updateNbkBatches(batches, experiment) {
        return notebookService
            .get({
                projectId: $stateParams.projectId,
                notebookId: $stateParams.notebookId
            })
            .$promise
            .then(function(notebook) {
                var promise = $q.when();
                _.forEach(batches, function(batch) {
                    promise = promise
                        .then(function(beforeBatch) {
                            return requestNbkBatchNumber(beforeBatch).then(function(batchNumber) {
                                setNbkBatch(batch, batchNumber, notebook.name, experiment.name);

                                return batchNumber;
                            });
                        });
                });

                return promise;
            });
    }

    function duplicateBatches(batchesQueueToAdd, isSyncWithIntended, experiment) {
        var promises = _.map(batchesQueueToAdd, function(batch) {
            return createBatch(angular.copy(batch), isSyncWithIntended, experiment);
        });

        return successAddedBatches(promises, experiment);
    }

    function successAddedBatches(promises, experiment) {
        return $q
            .all(promises)
            .then(function(batches) {
                return updateNbkBatches(batches, experiment)
                    .then(function() {
                        return batches;
                    });
            });
    }

    function getIntendedNotInActual(stoich) {
        var stoichTable = stoich;
        if (stoichTable) {
            var intended = stoichTable.products;
            var intendedCandidateHashes = _.map(intended, '$$batchHash');
            var actual = productBatchSummaryCache.getProductBatchSummary();
            var actualHashes = _.compact(_.map(actual, '$$batchHash'));
            _.each(intendedCandidateHashes, function(intendedCandidateHash, i) {
                removeItemFromBothArrays(intendedCandidateHash, actualHashes, intendedCandidateHashes, i);
            });
            var hashesToAdd = _.compact(intendedCandidateHashes);

            var result = _.map(hashesToAdd, function(hash) {
                return _.find(intended, {
                    $$batchHash: hash
                });
            });

            return result;
        }

        return [];
    }

    function removeItemFromBothArrays(item, array1, array2, i) {
        if (_.includes(array1, item)) {
            array2[i] = null;
            array1[_.indexOf(array1, item)] = null;
        }
    }

    function getStoichFromExperiment(experiment) {
        return _.get(experiment, 'components.stoichTable');
    }

    function syncWithIntendedProducts(experiment) {
        var stoichTable = getStoichFromExperiment(experiment);
        var batchesQueue = getIntendedNotInActual(stoichTable);
        var areProductsExist = stoichTable && !_.isEmpty(stoichTable.products);

        if (!areProductsExist) {
            return $q.resolve([]);
        }

        if (batchesQueue.length) {
            var batchesQueueToAdd = _.map(batchesQueue, function(batch) {
                return _.assign(new BatchViewRow(), batch);
            });

            return duplicateBatches(batchesQueueToAdd, true, experiment);
        }

        alertModal.info('Product Batch Summary is synchronized', 'sm');

        return $q.resolve([]);
    }

    function addNewBatch(experiment) {
        return createBatch(null, null, experiment).then(function(batch) {
            return updateNbkBatches([batch], experiment).then(function() {
                return batch;
            });
        });
    }

    function importSDFile(experiment) {
        return sdImportService.importFile().then(function(sdUnits) {
            var promises = _.map(sdUnits, function(unit) {
                return createBatch(unit, null, experiment);
            });

            return successAddedBatches(promises, experiment);
        });
    }

    function requestNbkBatchNumber(lastNbkBatch) {
        var latest = lastNbkBatch || getLatestNbkBatch();
        var request = apiUrl + 'projects/' + $stateParams.projectId + '/notebooks/' + $stateParams.notebookId +
            '/experiments/' + $stateParams.experimentId + '/batch_number?latest=' + latest;

        return $http.get(request)
            .then(function(result) {
                return result.data.batchNumber;
            });
    }

    function setNbkBatch(batch, batchNumber, notebookName, experimentName) {
        batch.nbkBatch = batchNumber;
        batch.fullNbkBatch = notebookName + '-' + experimentName + '-' + batchNumber;
        batch.fullNbkImmutablePart = notebookName + '-' + experimentName + '-';

        return batch;
    }

    function createBatch(parentBatch, isSyncWithIntended, experiment) {
        var batch = new BatchViewRow();
        var stoichTable = getStoichFromExperiment(experiment);

        _.assign(batch, parentBatch, {
            conversationalBatchNumber: undefined,
            registrationDate: undefined,
            registrationStatus: undefined
        });

        if (stoichTable && !isSyncWithIntended) {
            var limitingRow = calculationHelper.findLimitingRow(stoichTable.reactants);
            batchHelper.calculateValuesDependingOnTheoMoles(batch, limitingRow);
        }

        if (parentBatch) {
            return $q
                .all([
                    calculationHelper.recalculateSalt(batch),
                    checkImage(batch.structure)
                ])
                .then(function() {
                    return saveMolecule(batch.structure).then(function(structureId) {
                        if (batch.structure) {
                            batch.structure.structureId = structureId;
                        }
                    }, function() {
                        notifyService.error('Cannot save the structure!');
                    });
                })
                .then(function() {
                    batchHelper.calculateRow({changedRow: batch, changedField: fieldTypes.molWeight});

                    return batch;
                });
        }

        return $q.when(batch);
    }

    function checkImage(structure) {
        if (structure && structure.molfile && !structure.image) {
            return calculationService.getImageForStructure(structure.molfile, 'molecule').then(function(image) {
                structure.image = image;
            });
        }

        return $q.resolve();
    }

    function checkNonRemovableBatches(batches) {
        var nonEditableBatches = getNonEditableBatches(batches);
        if (!_.isEmpty(nonEditableBatches)) {
            var errorMessage = 'Following batches were registered or sent to registration and cannot be deleted: ';

            notifyService.error(
                errorMessage + _.uniq(nonEditableBatches)
                    .join(', ')
            );
        }
    }

    function deleteBatches(batches, batchesForRemove) {
        if (!_.isEmpty(batches) && !_.isEmpty(batchesForRemove)) {
            checkNonRemovableBatches(batches);

            _.remove(batches, function(batch) {
                return !registrationUtil.isRegistered(batch) && _.includes(batchesForRemove, batch);
            });
        }
    }

    function getLatestNbkBatch() {
        var batches = productBatchSummaryCache.getProductBatchSummary();
        var last = _.last(batches);

        return (last && last.nbkBatch) || 0;
    }

    function saveMolecule(structure) {
        if (structure && structure.molfile) {
            return $http.post(apiUrl + 'bingodb/molecule/', structure.molfile).then(function(response) {
                return response.data;
            });
        }

        return $q.resolve();
    }

    function registerBatches(batches) {
        var batchNumbers = _.map(batches, 'fullNbkBatch');

        if (!batchNumbers.length) {
            notifyService.warning('No Batches was selected for Registration');

            return $q.reject();
        }

        return registrationService
            .register({}, batchNumbers)
            .$promise
            .then(function() {
                notifyService.success('Selected Batches successfully sent to Registration');
            }, function() {
                notifyService.error('ERROR! Selected Batches registration failed');
            });
    }
}

module.exports = productBatchSummaryOperations;
