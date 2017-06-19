angular.module('indigoeln')
    .factory('sdProperties', function(AppValues) {

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

        var constants = [
            {   export : {  name: 'REGISTRATION_STATUS',
                            prop: 'registrationStatus',
                            subProp: undefined},
                import : {  name: 'registrationStatus',
                            code: 'REGISTRATION_STATUS',
                            format: undefined}
            },
            {   export : {  name: 'CONVERSATIONAL_BATCH_NUMBER',
                            prop: 'conversationalBatchNumber',
                            subProp: undefined},
                import : {  name: 'conversationalBatchNumber',
                            code: 'CONVERSATIONAL_BATCH_NUMBER',
                            format: undefined}
            },
            {   export : {  name: 'VIRTUAL_COMPOUND_ID',
                            prop: 'virtualCompoundId',
                            subProp: undefined},
                import : {  name: 'virtualCompoundId',
                            code: 'VIRTUAL_COMPOUND_ID',
                            format: undefined}
            },
            {   export : {  name: 'COMPOUND_SOURCE_CODE',
                            prop: 'source',
                            subProp: 'name'},
                import : {  name: 'source',
                            code: 'COMPOUND_SOURCE_CODE',
                            format: function (dicts, value) {
                                        return getWord(dicts, 'Source', 'name', value);
                                    }}
            },
            {   export : {  name: 'COMPOUND_SOURCE_DETAIL_CODE',
                            prop: 'sourceDetail',
                            subProp: 'name'},
                import : {  name: 'sourceDetail',
                            code: 'COMPOUND_SOURCE_DETAIL_CODE',
                            format: function (dicts, value) {
                                        return getWord(dicts, 'Source Details', 'name', value);
                                    }}
            },
            {   export : {  name: 'STEREOISOMER_CODE',
                            prop: 'stereoisomer',
                            subProp: 'name'},
                import : {  name: 'stereoisomer',
                            code: 'STEREOISOMER_CODE',
                            format: function (dicts, value) {
                                        return getWord(dicts, 'Stereoisomer Code', 'name', value);
                                     }}
            },
            {   export : {  name: 'GLOBAL_SALT_CODE',
                            prop: 'saltCode',
                            subProp: 'regValue'},
                import : {  name: 'saltCode',
                            code: 'GLOBAL_SALT_CODE',
                            format: function (dicts, value) {
                                        return getItem(AppValues.getSaltCodeValues(), 'regValue', value);
                                     }}
            },
            {   export : {  name: 'GLOBAL_SALT_EQ',
                            prop: 'saltEq',
                            subProp: 'value'},
                import : {  name: 'saltEq',
                            code: 'GLOBAL_SALT_EQ',
                            format: function (dicts, value) {
                                         return {
                                             value: parseInt(value),
                                             entered: false
                                         };
                                     }}
            },
            {   export : {  name: 'STRUCTURE_COMMENT',
                            prop: 'structureComments',
                            subProp: undefined},
                import : {  name: 'structureComments',
                            code: 'STRUCTURE_COMMENT',
                            format: undefined}
            },
            {   export : {  name: 'COMPOUND_STATE',
                            prop: 'compoundState',
                            subProp: 'name'},
                import : {  name: 'compoundState',
                            code: 'COMPOUND_STATE',
                            format: function (dicts, value) {
                                        return getWord(dicts, 'Compound State', 'name', value);
                                    }}
            },
            {   export : {  name: 'PRECURSORS',
                            prop: 'precursors',
                            subProp: undefined},
                import : {  name: 'precursors',
                            code: 'PRECURSORS',
                            format: undefined}
            }//,
//            {
//                export : {name: 'PURITY',
//                          prop: 'purity',
//                          subProp: 'data'},
//                import : {}
//            },
//            {
//                export : {name: 'MELTING_POINT',
//                          prop: 'meltingPoint',
//                          subProp: 'asString'},
//                import : {}
//            },
//            {
//                export : {name: '',
//                          prop: '',
//                          subProp: ''},
//                import : {}
//            },
//            {
//                export : {name: '',
//                          prop: '',
//                          subProp: ''},
//                import : {}
//            }

            //TODO:
        ];

        return {
            constants: constants
        };

    });