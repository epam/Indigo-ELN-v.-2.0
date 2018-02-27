/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* @ngInject */
function registrationUtil(appValuesService, registrationMsg, registrationService) {
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
                    && batch.saltCode.value !== appValuesService.getDefaultSaltCode().value
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
