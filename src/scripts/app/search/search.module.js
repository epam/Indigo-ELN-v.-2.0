var searchConfig = require('./search.config');
var SearchPanelController = require('./component/search-panel.controller');

module.exports = angular
    .module('indigoeln.search', [])

    .controller('SearchPanelController', SearchPanelController)

    .config(searchConfig)

    .name;
