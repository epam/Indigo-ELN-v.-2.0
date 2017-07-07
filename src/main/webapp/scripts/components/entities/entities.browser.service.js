angular
    .module('indigoeln')
    .factory('EntitiesBrowser', entitiesBrowser);

/* @ngInject */
function entitiesBrowser($q, $state, Principal, TabKeyUtils, localStorageService) {
    var tabs = {};
    var activeTab = {};
    var entityActions;
    var updateCurrentEntityFunction;
    var saveCurrentEntityFunction;
    var activeEntity;
    var curForm;

    var resolvePrincipal = function(func) {
        return Principal.identity().then(func);
    };

    return {
        getTabs: getTabs,
        setEntityActions: setEntityActions,
        getEntityActions: getEntityActions,
        setUpdateCurrentEntity: setUpdateCurrentEntity,
        getUpdateCurrentEntityFunc: getUpdateCurrentEntityFunc,
        callUpdateCurrentEntity: callUpdateCurrentEntity,
        setSaveCurrentEntity: setSaveCurrentEntity,
        getSaveCurrentEntityFunc: getSaveCurrentEntityFunc,
        callSaveCurrentEntity: callSaveCurrentEntity,
        saveCurrentEntity: saveCurrentEntity,
        setCurrentEntity: setCurrentEntity,
        getCurrentEntity: getCurrentEntity,
        setCurrentForm: setCurrentForm,
        setCurrentFormDirty: setCurrentFormDirty,
        getCurrentForm: getCurrentForm,
        goToTab: goToTab,
        saveEntity: saveEntity,
        close: close,
        setActiveTab: setActiveTab,
        getActiveTab: getActiveTab,
        setCurrentTabTitle: setCurrentTabTitle,
        changeDirtyTab: changeDirtyTab,
        addTab: addTab,
        getTabByParams: getTabByParams,
        saveTabs: saveTabs
    };

    function getUserId(user) {
        var id = user.id;
        tabs[id] = tabs[id] || {};

        return id;
    }

    function getTabKey(tab) {
        return tab && tab.tabKey ? tab.tabKey : TabKeyUtils.getTabKeyFromTab(tab);
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


    function setUpdateCurrentEntity(f) {
        updateCurrentEntityFunction = f;
    }

    function getUpdateCurrentEntityFunc() {
        return updateCurrentEntityFunction;
    }

    function callUpdateCurrentEntity() {
        updateCurrentEntityFunction();
    }

    function setSaveCurrentEntity(f) {
        saveCurrentEntityFunction = f;
    }

    function getSaveCurrentEntityFunc() {
        return saveCurrentEntityFunction;
    }

    function callSaveCurrentEntity(b) {
        saveCurrentEntityFunction(b);
    }

    function saveCurrentEntity() {
        return $q.resolve();
    }

    function setCurrentEntity(entity) {
        activeEntity = entity;
    }

    function getCurrentEntity() {
        return activeEntity;
    }

    function setCurrentForm(form) {
        curForm = form;
    }

    function setCurrentFormDirty() {
        if (curForm) {
            curForm.$setDirty();
        }
    }

    function getCurrentForm() {
        return curForm;
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
        updateCurrentEntityFunction = null;
        saveCurrentEntityFunction = null;
        activeEntity = null;
        activeTab = tab;
        entityActions = null;
        curForm = null;
    }

    function getActiveTab() {
        return activeTab;
    }


    function setCurrentTabTitle(tabTitle, stateParams) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var result = TabKeyUtils.getTabKeyFromParams(stateParams);
            var t = tabs[userId][result];
            if (t) {
                t.$$title = t.title = tabTitle;
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
        for (var key in tabsToSave) {
            delete tabsToSave[key].dirty;
        }
        localStorageService.set(storageKey, angular.toJson(tabsToSave));
    }

    function changeDirtyTab(stateParams, dirty) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var result = TabKeyUtils.getTabKeyFromParams(stateParams);
            if (tabs[userId][result]) {
                tabs[userId][result].dirty = dirty;
            }
        });
    }


    function addTab(tab) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = TabKeyUtils.getTabKeyFromTab(tab);

            if (!tabs[userId][tabKey]) {
                tab.tabKey = tabKey;
                tabs[userId][tabKey] = tab;
                saveTabs(user);
            }

            setActiveTab(tabs[userId][tabKey]);
        });
    }

    function getTabByParams(params) {
        return resolvePrincipal(function(user) {
            var userId = getUserId(user);
            var tabKey = TabKeyUtils.getTabKeyFromParams(params);

            return tabs[userId][tabKey];
        });
    }

    function deleteClosedTabAndGoToActive(userId, tabKey) {
        var keys = _.keys(tabs[userId]);
        var positionForClose = _.indexOf(keys, tabKey);
        var curPosition = _.indexOf(keys, activeTab.tabKey);
        var nextKey;
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
        saveTabs();
    }
}
