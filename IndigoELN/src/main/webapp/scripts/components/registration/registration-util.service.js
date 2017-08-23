angular
    .module('indigoeln')
    .factory('RegistrationUtil', registrationUtil);

/* @ngInject */
function registrationUtil(AppValues) {
    var isStereoisomerNeedComment = function(stereoisomer) {
        var stereocodesWithComment = ['SNENU', 'LRCMX', 'ENENU', 'DSTRU', 'UNKWN', 'HSREG'];

        return _.some(stereocodesWithComment, function(stereocode) {
            return stereoisomer.indexOf(stereocode) > -1;
        });
    };

    var errors = [
        {
            message: 'Total Weight or Volume should be grater then zero',
            test: function(batch) {
                return !batch.totalWeight.value && !batch.totalVolume.value;
            }
        },
        {
            message: 'Compound Source is required',
            test: function(batch) {
                return !batch.source || !batch.source.name;
            }
        },
        {
            message: 'Structure is required',
            test: function(batch) {
                return !batch.structure || !batch.structure.image;
            }
        },
        {
            message: 'Source Detail is required',
            test: function(batch) {
                return !batch.sourceDetail || !batch.sourceDetail.name;
            }
        },
        {
            message: 'Stereocode is required',
            test: function(batch) {
                return !batch.stereoisomer || !batch.stereoisomer.name;
            }
        },
        {
            message: 'Salt EQ is required and must be grater then zero',
            test: function(batch) {
                if (batch.saltEq && batch.saltEq.value) {
                    return false;
                }
                return batch.saltCode && batch.saltCode.value && batch.saltCode.value !== AppValues.getDefaultSaltCode().value;
            }
        },
        {
            message: 'Structure comments is required',
            test: function(batch) {
                return batch.stereoisomer && batch.stereoisomer.name && isStereoisomerNeedComment(batch.stereoisomer.name) && !batch.structureComments;
            }
        }
    ];

    return {
        isRegistered: isRegistered,
        getNotFullForRegistrationBatches: getNotFullForRegistrationBatches
    };

    function isRegistered(row) {
        return row.registrationStatus === 'PASSED' || row.registrationStatus === 'IN_PROGRESS';
    }

    function getNotFullForRegistrationBatches(batches) {
        var notFullBatches = [];
        _.each(batches, function(batch) {
            var notFullBatch = {
                nbkBatch: batch.fullNbkBatch, emptyFields: []
            };

            _.forEach(errors, function(error) {
                if (error.test(batch)) {
                    notFullBatch.emptyFields.push(error.message);
                }
            });

            if (notFullBatch.emptyFields.length) {
                notFullBatches.push(notFullBatch);
            }
        });

        return notFullBatches;
    }
}
