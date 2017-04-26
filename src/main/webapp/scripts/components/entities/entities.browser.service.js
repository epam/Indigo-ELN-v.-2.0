/**
 * Created by Stepan_Litvinov on 2/17/2016.
 */
angular.module('indigoeln')
    .factory('EntitiesBrowser', function ($rootScope, Experiment, Notebook, Project, $q, $state,
                                          Principal, TabKeyUtils, localStorageService) {
        var EntitiesBrowser = {};
        var tabs = EntitiesBrowser.tabs =  {};
        EntitiesBrowser.activeTab = {};
        var saveTabs;
        var resolvePrincipal = function (func) {
            return Principal.identity().then(func);
        };

        resolvePrincipal(function(user) {
            if (!user) return;
            var strorageKey = user.id + '.current-tabs', id = user.id;
            var t = tabs[id] =  JSON.parse(localStorageService.get(strorageKey));
            for (var key in t) {
                t[key].$$title = t[key].title
            }
            saveTabs = function() {
                localStorageService.set(strorageKey, angular.toJson(tabs[id]))
            }
        })

        var getUserId = function () {
            var id = Principal.getIdentity().id;
            EntitiesBrowser.tabs[id] = EntitiesBrowser.tabs[id] || {};
            return id;
        };

        var getTabKey = function (tab) {
            return tab && tab.tabKey ? tab.tabKey : TabKeyUtils.getTabKeyFromTab(tab);
        };

        function deleteClosedTabAndGoToActive(userId, tabKey) {
            var keys = _.keys(EntitiesBrowser.tabs[userId]);
            var positionForClose = _.indexOf(keys, tabKey);
            var curPosition = _.indexOf(keys, EntitiesBrowser.activeTab.tabKey);
            var nextKey;
            if (curPosition === positionForClose) {
                nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
            } else {
                nextKey = keys[curPosition];
            }
            delete EntitiesBrowser.tabs[userId][tabKey];
            if (keys.length > 1 && EntitiesBrowser.tabs[userId][nextKey]) {
                EntitiesBrowser.goToTab(EntitiesBrowser.tabs[userId][nextKey]);
            } else if (keys.length === 1) {
                $state.go('experiment');
            }
            saveTabs()
        }


        EntitiesBrowser.saveCurrentEntity = function () {
            return $q.resolve();
        };

        EntitiesBrowser.setCurrentExperiment = function (experiment) {
            EntitiesBrowser.activeExperiment = experiment;
        };

        EntitiesBrowser.getCurrentExperiment = function () {
           return  EntitiesBrowser.activeExperiment;
        }

        EntitiesBrowser.goToTab = function (tab) {
            var curTab = tab;
            return resolvePrincipal(function () {
                var userId = getUserId();
                var tabKey = getTabKey(curTab);
                if (EntitiesBrowser.tabs[userId][tabKey]) {
                    $state.go(tab.state, tab.params);
                }
            });
        };

        EntitiesBrowser.saveEntity = function (tab) {
            return $q.resolve();
        };
        // close tab
        EntitiesBrowser.close = function (tabKey) {
            return resolvePrincipal(function () {
                var userId = getUserId();
                deleteClosedTabAndGoToActive(userId, tabKey);
            });
        };

        EntitiesBrowser.setActiveTab = function (tab) {
            EntitiesBrowser.activeTab = tab;
        };

        EntitiesBrowser.getActiveTab = function () {
            return EntitiesBrowser.activeTab;
        };

        EntitiesBrowser.setCurrentTabTitle = function (tabTitle, stateParams) {
            return resolvePrincipal(function () {
                var userId = getUserId();
                var result = TabKeyUtils.getTabKeyFromParams(stateParams);
                var t = EntitiesBrowser.tabs[userId][result];
                if (t) {
                    t.$$title = t.title = tabTitle;
                    saveTabs();
                }
            });
        };

        EntitiesBrowser.changeDirtyTab = function (stateParams, dirty) {
            return resolvePrincipal(function () {
                var userId = getUserId();
                var result = TabKeyUtils.getTabKeyFromParams(stateParams);
                if (EntitiesBrowser.tabs[userId][result]) {
                    EntitiesBrowser.tabs[userId][result].dirty = dirty;
                }
            });
        };

        EntitiesBrowser.addTab = function (tab) {
            var self = this;
            var curTab = tab;
            return resolvePrincipal(function () {
                var userId = getUserId();
                var tabKey = TabKeyUtils.getTabKeyFromTab(curTab);
                if(!EntitiesBrowser.tabs[userId][tabKey]){
                    curTab.tabKey = tabKey;
                    EntitiesBrowser.tabs[userId][tabKey] = curTab;
                    if (saveTabs) saveTabs()
                }
                self.setActiveTab(EntitiesBrowser.tabs[userId][tabKey]);
            });
        };

        EntitiesBrowser.getTabByParams = function (params) {
            return resolvePrincipal(function () {
                var userId = getUserId();
                var tabKey = TabKeyUtils.getTabKeyFromParams(params);
                return EntitiesBrowser.tabs[userId][tabKey];
            });
        };

        return EntitiesBrowser;
    });