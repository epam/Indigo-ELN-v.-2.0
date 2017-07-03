angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService, sdProperties, Alert, $q) {
    return {
        exportItems: exportItems
    };

    function getProperty(item, props) {
        var i = 0;
        var prop = props[i++];
        var subItem = item[prop];
        while (props[i]) {
            if (angular.isUndefined(subItem)) {
                return;
            }
            prop = props[i++];
            subItem = subItem[prop];
        }
        if (subItem && !_.isObject(subItem)) {
            return subItem;
        }
    }


    function generateExportProperties(item) {
        var properties = {};
        sdProperties.constants.forEach(function(prop) {
            var fields = prop.export;
            properties[fields.name] = getProperty(item, fields.prop, fields.subProp);
        });

        return properties;
    }

    function getExportProperties(items) {
        return _.map(items, function(item) {
            var properties = {
                molfile: item.structure,
                properties: generateExportProperties(item)
            };
            if (properties.molfile) {
                return properties;
            }
        });
    }

    function exportItems(items) {
        var properties = getExportProperties(items);
        if (properties[0]) {
            return SdService.export({}, properties).$promise;
        }
        Alert.error('Please add Batch structure before export sd file');

        return $q.reject();
    }
}
