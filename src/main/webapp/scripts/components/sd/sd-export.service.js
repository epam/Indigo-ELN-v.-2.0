angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService){

   function getSubProperty(item, prop, subProp) {
        var subItem = item[prop];
        if (subItem) {
            return subItem[subProp];
        }
    }
        var getProperty = function (item, prop, subProp) {
            var subItem = item[prop];
            if (subItem && subProp) {
                return subItem[subProp];
            }
            return subItem;
        };

        var generateExportProperties = function(item) {
            var properties = { molfile: item.structure.molfile };
            sdProperties.constants.forEach(function(prop, i, constants) {
                var fields = prop.export;
                properties[fields.name] = getProperty(item, fields.prop, fields.subProp);
            });
            console.log("Export properties: ", properties);
            return properties;
        };

        var getExportProperties = function (items) {
            return _.map(items, function(item){console.log('item: ', item);
                    return generateExportProperties(item);
                    });
        };

        var exportItems = function (items) {
            return SdService.export({}, getExportProperties(items)).$promise;
        };

        return {
            exportItems: exportItems
        };

    });
