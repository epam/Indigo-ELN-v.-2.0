angular.module('indigoeln')
    .factory('SdExportService', function (SdService, sdPropertiesInfo) {

        var exportItems = function (items) {
            return SdService.export({}, sdPropertiesInfo.convert(items)).$promise;
        };

        return {
            exportItems: exportItems
        };

    });
