angular.module('indigoeln')
    .factory('SdExportService', function (SdService, sdPropertiesInfo) {

        var getSubProperty = function (item, prop, subProp) {
            var subItem = item[prop];
            if (subItem) {
                return subItem[subProp];
            }
        };

        var generateExportProperties = function(item) {

            var keys = Object.keys(sdPropertiesInfo);
            var properties = {};

            keys.forEach(function(key, i, keys){
                if (typeof(sdPropertiesInfo[key]) === 'object') {
                    properties[key] = getSubProperty(item, sdPropertiesInfo[key].prop, sdPropertiesInfo[key].subProp);
                } else {
                    properties[key] = item[sdPropertiesInfo[key]];
                }
            });

            return properties;
        };

        var convert = function (items) {
            return _.map(items, function (item) {
                generateExportProperties(item);
                return {
                    molfile: item.structure.molfile,
                    properties: generateExportProperties(item)
                };
            });
        };

        var exportItems = function (items) {
            return SdService.export({}, convert(items)).$promise;
        };

        return {
            exportItems: exportItems
        };

    });
