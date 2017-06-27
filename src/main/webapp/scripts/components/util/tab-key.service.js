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
        var matchParams = !_.isEmpty(tab.params) ? getMatchParams(tab.params) : {
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
        var matchParams = getMatchParams(params);

        return tabParamsToString(matchParams);
    }

    function tabParamsToString(matchParams) {
        function safeString(input) {
            return !input ? input : input.toString();
        }

        var paramsToString = {};
        _.each(matchParams, function(val, name) {
            paramsToString[name] = safeString(val);
        });

        return angular.toJson(paramsToString);
    }

    function getMatchParams(params) {
        var matchParams = {};
        _.each(_.keys(params).sort(), function(name) {
            matchParams[name] = params[name];
        });

        return matchParams;
    }
}
