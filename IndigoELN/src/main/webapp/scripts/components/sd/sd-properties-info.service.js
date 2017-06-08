angular.module('indigoeln')
    .constant('sdPropertiesInfo', {
            REGISTRATION_STATUS             :       "registrationStatus",
            CONVERSATIONAL_BATCH_NUMBER     :       "conversationalBatchNumber",
            VIRTUAL_COMPOUND_ID             :       "virtualCompoundId",
            STRUCTURE_COMMENT               :       "structureComments",
            PRECURSORS                      :       "precursors",
            COMPOUND_SOURCE_CODE            :       {prop: 'source',        subProp: 'name',
                                                        format: function (dicts, value) {
                                                                    return getWord(dicts, 'Source', 'name', value);
                                                                }
                                                    },
            COMPOUND_SOURCE_DETAIL_CODE     :       {prop: 'sourceDetail',  subProp: 'name',
                                                        format: function (dicts, value) {
                                                                    return getWord(dicts, 'Source Details', 'name', value);
                                                                }
                                                    },
            STEREOISOMER_CODE               :       {prop: 'stereoisomer',  subProp: 'name',
                                                        format: function (dicts, value) {
                                                                    return getWord(dicts, 'Stereoisomer Code', 'name', value);
                                                                }
                                                    },
            GLOBAL_SALT_CODE                :       {prop: 'saltCode',      subProp: 'regValue',
                                                        format: function (dicts, value) {
                                                                    return getItem(AppValues.getSaltCodeValues(), 'regValue', value);
                                                                }
                                                    },
            GLOBAL_SALT_EQ                  :       {prop: 'saltEq',        subProp: 'value',
                                                        format: function (dicts, value) {
                                                                    return {
                                                                        value: parseInt(value),
                                                                        entered: false
                                                                    };
                                                                }
                                                    },
            COMPOUND_STATE                  :       {prop: 'compoundState', subProp: 'name',
                                                        format: function (dicts, value) {
                                                                    return getWord(dicts, 'Compound State', 'name', value);
                                                                }
                                                    }
    });