angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService, sdProperties, Alert, $q, $log) {
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

    function getExportProperties(exportObject) {
        var result = [];
        _.forEach(exportObject, function(exportProperty) {
            if (!exportProperty.structure.molfile) {
                $log.debug('Error, molfile is undefined', exportProperty);
                return;
            }
            result.push({
                molfile: exportProperty.structure.molfile,
                properties: generateExportProperties(exportProperty)
            });
        });

        return result;
    }

    function exportItems(items) {
        var properties = getExportProperties(items);
        if (properties.length) {
            return SdService.export({}, properties).$promise;
        }
        Alert.error('Please add Batch structure before export sd file');

        return $q.reject();
    }
}
