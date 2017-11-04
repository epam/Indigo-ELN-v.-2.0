var searchConfig = require('./search.config');
var SearchPanelController = require('./search-panel.controller');

module.exports = angular
    .module('indigoeln.entities.search', [])

    .controller('SearchPanelController', SearchPanelController)

    .config(searchConfig)

    .name;
