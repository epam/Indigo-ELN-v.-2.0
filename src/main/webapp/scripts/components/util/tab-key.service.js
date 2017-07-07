angular
    .module('indigoeln')
    .factory('TabKeyUtils', tabKeyUtils);

function tabKeyUtils() {
    return {
        getTabKeyFromTab: getTabKeyFromTab,
        getTabKeyFromName: getTabKeyFromName,
        getTabKeyFromParams: getTabKeyFromParams
    };

    function getTabKeyFromTab(tab) {
        var matchParams = !_.isEmpty(tab.params) ? tab.params : {
            name: tab.name
        };

        return tabParamsToString(matchParams);
    }

    function getTabKeyFromName(tabName) {
        var matchParams = {
            name: tabName
        };

        return tabParamsToString(matchParams);
    }

    function getTabKeyFromParams(params) {
        return tabParamsToString(params);
    }

    function safeString(input) {
        return !input ? input : input.toString();
    }

    function tabParamsToString(matchParams) {
        var paramsToString = _.map(matchParams, function(value) {
            return safeString(value);
        });

        return angular.toJson(paramsToString);
    }
}
