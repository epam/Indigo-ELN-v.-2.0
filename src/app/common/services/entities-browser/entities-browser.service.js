entitiesBrowserService.$inject = ['$q', '$state', 'principalService', 'tabKeyService', 'CacheFactory'];

function entitiesBrowserService($q, $state, principalService, tabKeyService, CacheFactory) {
    var tabs = {};
    var activeTab = {};
    var entityActions;
    var restored = false;

    var resolvePrincipal = function(func) {
        return principalService.checkIdentity().then(func);
    };

    var tabCache = CacheFactory.createCache('tabCache', {
        storageMode: 'localStorage',
        // a week
        maxAge: 60 * 60 * 1000 * 24 * 7,
        deleteOnExpire: 'aggressive',
        // 10 minutes
        recycleFreq: 60 * 10000
    });

    return {
        getTabs: getTabs,
        setEntityActions: setEntityActions,
        getEntityActions: getEntityActions,
        saveCurrentEntity: saveCurrentEntity,
        goToTab: goToTab,
        saveEntity: saveEntity,
        close: close,
        getActiveTab: getActiveTab,
        setCurrentTabTitle: setCurrentTabTitle,
        changeDirtyTab: changeDirtyTab,
        addTab: addTab,
        getTabByParams: getTabByParams,
        restoreTabs: restoreTabs,
        setExperimentTab: setExperimentTab,
        getExperimentTab: getExperimentTab
    };

    function getExperimentTabById(user, experimentFullId) {
        return user.id + '.' + experimentFullId + '.current-exp-tab';
    }

    function setExperimentTab(index, experimentFullId) {
        return resolvePrincipal().then(function(user) {
            tabCache.put(getExperimentTabById(user, experimentFullId), index);
        });
    }

    function getExperimentTab(experimentFullId) {
        return resolvePrincipal().then(function(user) {
            return tabCache.get(getExperimentTabById(user, experimentFullId));
        });
    }

    function getUserId(user) {
        var id = user.id;
        tabs[id] = tabs[id] || {};

        return id;
    }

    function getTabKey(tab) {
        return tab && tab.tabKey ? tab.tabKey : tabKeyService.getTabKeyFromTab(tab);
    }

    function getTabs(success) {
        resolvePrincipal(function(user) {
            success(tabs[user.id]);
        });
    }

    function setEntityActions(actions) {
        entityActions = actions;
    }

    function getEntityActions() {
        return entityActions;
    }

    function saveCurrentEntity() {
        return $q.resolve();
    }

    function goToTab(tab) {
        var curTab = tab;

        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = getTabKey(curTab);
            if (tabs[userId][tabKey]) {
                $state.go(tab.state, tab.params);
            }
        });
    }

    function saveEntity() {
        return $q.resolve();
    }

    function close(tabKey) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            deleteClosedTabAndGoToActive(userId, tabKey);
        });
    }

    function setActiveTab(tab) {
        activeTab = tab;
        entityActions = null;
    }

    function getActiveTab() {
        return activeTab;
    }

    function getTab(user, stateParams) {
        var userId = getUserId(user);
        var result = tabKeyService.getTabKeyFromParams(stateParams);

        return tabs[userId][result];
    }

    function setCurrentTabTitle(tabTitle, stateParams) {
        return resolvePrincipal(function(user) {
            var tab = getTab(user, stateParams);
            if (tab) {
                tab.$$title = tabTitle;
                tab.title = tabTitle;
                saveTabs(user);
            }
        });
    }

    function saveTabs(user) {
        if (!user) {
            return;
        }
        var storageKey = user.id + '.current-tabs';
        var id = user.id;
        var tabsToSave = angular.copy(tabs[id]);
        _.forEach(tabsToSave, function(tab) {
            delete tab.dirty;
        });
        tabCache.put(storageKey, tabsToSave);
    }

    function restoreTabs(user) {
        var storageKey = user.id + '.current-tabs';
        var restoredTabs = tabCache.get(storageKey);
        restored = true;
        _.forEach(restoredTabs, function(tab, tabKey) {
            if (!tabs[user.id][tabKey]) {
                tab.tabKey = tabKey;
                tab.$$title = tab.title;
                tabs[user.id][tabKey] = tab;
            }
        });
    }

    function changeDirtyTab(stateParams, dirty) {
        return resolvePrincipal(function(user) {
            var tab = getTab(user, stateParams);
            if (tab) {
                tab.dirty = dirty;
            }
        });
    }

    function addTab(tab) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = tabKeyService.getTabKeyFromTab(tab);

            if (!tabs[userId][tabKey]) {
                tab.tabKey = tabKey;
                tabs[userId][tabKey] = tab;
                if (restored) {
                    saveTabs(user);
                }
            }

            setActiveTab(tab);
        });
    }

    function getTabByParams(params) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = tabKeyService.getTabKeyFromParams(params);

            return tabs[userId][tabKey];
        });
    }

    function deleteClosedTabAndGoToActive(userId, tabKey) {
        var keys = _.keys(tabs[userId]);
        var positionForClose = _.indexOf(keys, tabKey);
        var curPosition = _.indexOf(keys, activeTab.tabKey);
        var nextKey;
        var storageKey = userId + '.current-tabs';
        if (curPosition === positionForClose) {
            nextKey = keys[positionForClose - 1] || keys[positionForClose + 1];
        } else {
            nextKey = keys[curPosition];
        }
        delete tabs[userId][tabKey];
        if (tabs[userId][nextKey]) {
            goToTab(tabs[userId][nextKey]);
        } else {
            $state.go('experiment');
        }
        removeTabFromCache(storageKey, tabKey);
        saveTabs();
    }

    function removeTabFromCache(storageKey, tabKey) {
        var cacheTabs = tabCache.get(storageKey);

        cacheTabs = _.omit(cacheTabs, tabKey);
        tabCache.put(storageKey, cacheTabs);
    }
}

module.exports = entitiesBrowserService;
