angular.module('indigoeln')
    .factory('sdPropertiesInfo', function(sdPropertiesConstants) {

        var getProperty = function (item, prop, subProp) {
            var subItem = item[prop];
            if (subProp) {
                return subItem[subProp];
            }
            return subItem;
        };

        var getExportProperties = function(item) {
            var properties = { molfile: item.structure.molfile };
            sdPropertiesConstants.forEach(function(prop, i, constants) {
                var fields = prop.export;
                properties[fields.name] = getProperty(item, fields.prop, fields.subProp);
            });

            return properties;
        };

        var getImportProperties = function() {
            var properties = {};
            sdPropertiesConstants.forEach(function(prop, i, constants) {
                //TODO: 
            });

            return properties;
        };

        var convert = function (items) {
                        return _.map(items, getExportProperties(item));
                    };
    });