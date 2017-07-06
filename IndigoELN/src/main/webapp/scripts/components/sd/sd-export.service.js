angular
    .module('indigoeln')
    .factory('SdExportService', sdExportService);

/* @ngInject */
function sdExportService(SdService, SdConstants, Alert, $q, $log) {
    return {
        exportItems: exportItems
    };

    function exportItems(items) {
        var properties = getExportProperties(items);
        if (properties.length) {

            return SdService.export({}, properties).$promise;
        }
        Alert.error('Please add Batch structure before export sd file');

        return $q.reject();
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

    function generateExportProperties(item) {
        var properties = {};
        _.each(SdConstants, function(property) {
            properties = _.defaultsDeep(properties, getProperty(item, property));
        });

        return properties;
    }

    function getProperty(item, prop) {
        return prop.childrenLength ? getMultipleProperty(item, prop) : getSingleProperty(item, prop);
    }

    function getSingleProperty(item, prop) {
        return _.set({}, prop.code, _.result(item, prop.path ? _.join([prop.name, prop.path], '.') : prop.name));
    }

    function getMultipleProperty(item, prop) {
        var property = {};
        var path = '';
        var value;
        for (var index = 0; index < prop.childrenLength; index++) {
            path = prop.path.replace(/<%= index =>/, index);
            value = _.result(item, _.join([prop.name, path], '.'));
            if (!_.isObject(value)) {
                _.set(property, getMultipleCode(prop, index), value);
            }
        }

        return property;
    }

    function getMultipleCode(property, index) {
        return _.join([property.code, index], '_');
    }
}
