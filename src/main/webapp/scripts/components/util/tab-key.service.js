angular.module('indigoeln')
    .service('TabKeyUtils', function() {


        var tabParamsToString = function (matchParams) {
            function safeString(input) {
                return !input ? input : input.toString();
            }

            var paramsToString = {};
            _.each(matchParams, function (val, name) {
                paramsToString[name] = safeString(val);
            });
            return angular.toJson(paramsToString);
        };

        var getMatchParams = function (params) {
            var matchParams = {};
            _.each(_.keys(params).sort(), function (name) {
                matchParams[name] = params[name];
            });
            return matchParams;
        };


        return {

            getTabKeyFromTab : function (tab) {
                var matchParams = !_.isEmpty(tab.params) ? getMatchParams(tab.params) : {name: tab.name};
                return tabParamsToString(matchParams);
            },

            getTabKeyFromName : function (tabName) {
                var matchParams = {name: tabName};
                return tabParamsToString(matchParams);
            },

            getTabKeyFromParams : function (params) {
                var matchParams = getMatchParams(params);
                return tabParamsToString(matchParams);
            }
        };
    });