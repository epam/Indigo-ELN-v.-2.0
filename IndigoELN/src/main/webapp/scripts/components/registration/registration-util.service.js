angular.module('indigoeln')
    .factory('RegistrationUtil', function (AppValues) {
        var isRegistered = function (row) {
            return row.registrationStatus === 'PASSED' || row.registrationStatus === 'IN_PROGRESS';
        };
        var isStereoisomerNeedComment = function (stereoisomer) {
            var stereocodesWithComment = ['SNENU', 'LRCMX', 'ENENU', 'DSTRU', 'UNKWN', 'HSREG'];
            return _.some(stereocodesWithComment, function (stereocode) {
                return stereoisomer.indexOf(stereocode) > -1;
            });
        };

        function getNotFullForRegistrationBatches(batches) {
            var notFullBatches = [];
            _.each(batches, function (batch) {
                var notFullBatch = {nbkBatch: batch.fullNbkBatch, emptyFields: []};
                if (!batch.totalWeight.value && !batch.totalVolume.value) {
                    notFullBatch.emptyFields.push('Total Weight or Volume should be grater then zero');
                }
                if (!batch.source || !batch.source.name) {
                    notFullBatch.emptyFields.push('Compound Source is required');
                }
                if (!batch.sourceDetail || !batch.sourceDetail.name) {
                    notFullBatch.emptyFields.push('Source Detail is required');
                }
                if (!batch.stereoisomer || !batch.stereoisomer.name) {
                    notFullBatch.emptyFields.push('Stereocode is required');
                }
                if (batch.saltCode && batch.saltCode.value && batch.saltCode.value !== AppValues.getDefaultSaltCode().value && !batch.saltEq) {
                    notFullBatch.emptyFields.push('Salt EQ is required and must be grater then zero');
                }
                if (batch.stereoisomer && batch.stereoisomer.name && isStereoisomerNeedComment(batch.stereoisomer.name) && !batch.structureComments) {
                    notFullBatch.emptyFields.push('Structure comments is required');
                }
                if (notFullBatch.emptyFields.length) {
                    notFullBatches.push(notFullBatch);
                }
            });
            return notFullBatches;
        }

        return {
            isRegistered: isRegistered,
            getNotFullForRegistrationBatches: getNotFullForRegistrationBatches
        };
    });


