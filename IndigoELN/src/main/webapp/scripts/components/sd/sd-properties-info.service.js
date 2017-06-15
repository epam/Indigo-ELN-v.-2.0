angular.module('indigoeln')
    .factory('sdPropertiesInfo', function(sdProperties) {

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
            return _.map(items, function(item){
                    return generateExportProperties(item);
                    });
        };

        var getImportProperties = function() {
            var properties = {};
            sdProperties.constants.forEach(function(prop, i, constants) {
                var fields = prop.import;
                if (fields.format) {
                    properties[fields.name] = {code : fields.code,
                                               format : fields.format};
                } else {
                    properties[fields.name] = {code : fields.code};
                }
            });

            return properties;
        };

        return {
            getExportProperties: getExportProperties,
            getImportProperties: getImportProperties
        };

    });