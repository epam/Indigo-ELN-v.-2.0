angular
    .module('indigoeln')
    .factory('SdExportService', SdExportService);

/* @ngInject */
function SdExportService(SdService){

    return {
        exportItems: exportItems
    };

    function exportItems(items) {
        return SdService.export({}, convert(items)).$promise;
    }

   function getSubProperty(item, prop, subProp) {
        var subItem = item[prop];
        if (subItem) {
            return subItem[subProp];
        }
    }

    function convert(items) {
        return _.map(items, function (item) {
            return {
                molfile: item.structure.molfile,
                properties: {
                    REGISTRATION_STATUS: item.registrationStatus,
                    CONVERSATIONAL_BATCH_NUMBER: item.conversationalBatchNumber,
                    VIRTUAL_COMPOUND_ID: item.virtualCompoundId,
                    COMPOUND_SOURCE_CODE: getSubProperty(item, 'source', 'name'),
                    COMPOUND_SOURCE_DETAIL_CODE: getSubProperty(item, 'sourceDetail', 'name'),
                    STEREOISOMER_CODE: getSubProperty(item, 'stereoisomer', 'name'),
                    GLOBAL_SALT_CODE: getSubProperty(item, 'saltCode', 'regValue'),
                    GLOBAL_SALT_EQ: getSubProperty(item, 'saltEq', 'value'),
                    STRUCTURE_COMMENT: item.structureComments,
                    COMPOUND_STATE: getSubProperty(item, 'compoundState', 'name'),
                    PRECURSORS: item.precursors
                    //TODO: other properties
                }
            };
        });

    }
}
