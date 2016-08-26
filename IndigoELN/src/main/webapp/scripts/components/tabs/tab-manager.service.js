angular.module('indigoeln')
    .factory('TabManager', function ($rootScope, $stateParams, Experiment, Notebook, Project, $q, $state) {
        var _tabs = {};
        var _cache = {};
        var _activeTab = {};

        return {
            addTab: function (tab) {
                _tabs[tab.name] = tab;
                _cache[tab.name] = tab;
                this.setActiveTab(tab);
                $rootScope.$broadcast('tabs-changed');
            },
            getTabs: function () {
                return _.values(_tabs);
            },
            getActiveTab: function () {
                return _activeTab;
            },
            setActiveTab: function (tab) {
                _activeTab = tab;
            },
            getPageInfoFromCache: function (tabName) {
                return _cache[tabName].pageInfo;
            },
            closeTab: function (tabName) {
                var keys = _.keys(_tabs);
                if (keys.length > 1) {
                    var positionForClose = _.indexOf(keys, tabName);
                    var curPosition = _.indexOf(keys, _activeTab.name);
                    var nextKey;
                    if (curPosition === positionForClose) {
                        nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
                    } else {
                        nextKey = keys[curPosition];
                    }
                    delete _tabs[tabName];
                    delete _cache[tabName];
                    $rootScope.$broadcast('tabs-changed');
                    this.goToTab(nextKey);
                }
            },
            goToTab: function (tabName) {
                var tab = _tabs[tabName];
                $state.go(tab.state);
            }
        };
    });