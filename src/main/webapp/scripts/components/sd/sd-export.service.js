angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService, sdProperties, Alert){

    var getProperty = function (item, props, subProp) {
        var i = 0;
        var prop = props[i++];
        var subItem = item[prop];
        while (props[i]) {
            if (subItem === undefined) {return;}
            prop = props[i++];
            subItem = subItem[prop];
        };
        if (subItem && !_.isObject(subItem)){
            return subItem;
        }
    };


    var generateExportProperties = function(item) {
        var properties = {};
        sdProperties.constants.forEach(function(prop, i, constants) {
            var fields = prop.export;
            properties[fields.name] = getProperty(item, fields.prop, fields.subProp);
        });
        return properties;
    };

    var getExportProperties = function (items) {
        return _.map(items, function(item){
                var properties = { molfile: item.structure.molfile,
                                   properties: generateExportProperties(item)};
                    if (properties.molfile) {
                        return properties;
                    }
                });
    };

    var exportItems = function (items) {
        var properties = getExportProperties(items); console.log('properties', properties);
        if (properties[0]) {
            return SdService.export({}, properties).$promise;
        }
        Alert.error('Please add Batch structure before export sd file');
    };

    return {
        exportItems: exportItems
    };
}
