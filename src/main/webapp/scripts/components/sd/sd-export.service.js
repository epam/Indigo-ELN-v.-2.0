angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService, sdProperties){

    var getProperty = function (item, props, subProp) {
        var i = 0;
        var prop = props[i++];
        var subItem = item[prop];
        while (props[i]) {
            prop = props[i++];
            subItem = subItem[prop];
        };
        return subItem;
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
        return _.map(items, function(item){console.log('item: ', item);
                var properties = { molfile: item.structure.molfile,
                                   properties: generateExportProperties(item)};console.log("Export properties: ", properties);
                return properties;
                });
    };

    var exportItems = function (items) {
        return SdService.export({}, getExportProperties(items)).$promise;
    };

    return {
        exportItems: exportItems
    };
}
