/* @ngInject */
function registrationUtil(appValues, registrationMsg, registrationService) {
    var regServiceInfo;

    var isStereoisomerNeedComment = function(stereoisomer) {
        var stereocodesWithComment = registrationMsg.STEREOCODES;

        return _.some(stereocodesWithComment, function(stereocode) {
            return stereoisomer.indexOf(stereocode) > -1;
        });
    };

    var errors = [
        {
            message: registrationMsg.TOTAL_WEIGHT_ERROR,
            test: function(batch) {
                return !batch.totalWeight.value && !batch.totalVolume.value;
            }
        },
        {
            message: registrationMsg.COMPOUND_SOURCE_ERROR,
            test: function(batch) {
                return !batch.source || !batch.source.name;
            }
        },
        {
            message: registrationMsg.STRUCTURE_ERROR,
            test: function(batch) {
                return !batch.structure || !batch.structure.image;
            }
        },
        {
            message: registrationMsg.SOURCE_DETAIL_ERROR,
            test: function(batch) {
                return !batch.sourceDetail || !batch.sourceDetail.name;
            }
        },
        {
            message: registrationMsg.STEREOCODE_ERROR,
            test: function(batch) {
                return !batch.stereoisomer || !batch.stereoisomer.name;
            }
        },
        {
            message: registrationMsg.SALT_EQ_ERROR,
            test: function(batch) {
                return _.get(batch.saltCode, 'value')
                    && batch.saltCode.value !== appValues.getDefaultSaltCode().value
                    && !_.get(batch.saltEq, 'value');
            }
        },
        {
            message: registrationMsg.STRUCTURE_COMMENTS_ERROR,
            test: function(batch) {
                return batch.stereoisomer
                    && batch.stereoisomer.name
                    && isStereoisomerNeedComment(batch.stereoisomer.name)
                    && !batch.structureComments;
            }
        }
    ];

    return {
        isRegistered: isRegistered,
        getNotFullForRegistrationBatches: getNotFullForRegistrationBatches,
        hasRegistrationService: hasRegistrationService
    };

    function isRegistered(row) {
        return row.registrationStatus === 'PASSED' || row.registrationStatus === 'IN_PROGRESS';
    }

    function getNotFullForRegistrationBatches(batches) {
        var notFullBatches = [];
        _.forEach(batches, function(batch) {
            var notFullBatch = {
                nbkBatch: batch.fullNbkBatch,
                emptyFields: _.map(_.filter(errors, function(error) {
                    return error.test(batch);
                }), 'message')
            };

            if (notFullBatch.emptyFields.length) {
                notFullBatches.push(notFullBatch);
            }
        });

        return notFullBatches;
    }

    function hasRegistrationService() {
        if (regServiceInfo) {
            return regServiceInfo;
        }

        regServiceInfo = registrationService.info({}).$promise.then(function(info) {
            return _.isArray(info) && info.length > 0;
        });

        return regServiceInfo;
    }
}

module.exports = registrationUtil;
