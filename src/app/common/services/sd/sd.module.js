var sdService = require('./sd.service');
var sdConstants = require('./sd-constants.service');
var sdExportService = require('./sd-export.service');
var sdImportService = require('./sd-import.service');
var sdImportHelperService = require('./sd-import-helper.service');

module.exports = angular
    .module('indigoeln.common.services.sd', [])

    .factory('sdService', sdService)
    .factory('sdExportService', sdExportService)
    .factory('sdImportService', sdImportService)
    .factory('sdImportHelperService', sdImportHelperService)

    .constant('sdConstants', sdConstants)

    .name;
