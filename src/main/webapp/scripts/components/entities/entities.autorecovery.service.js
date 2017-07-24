angular
    .module('indigoeln')
    .factory('AutoRecoverEngine', autoRecoverEngine);

/* @ngInject */
function autoRecoverEngine(localStorageService, $timeout, EntitiesBrowser, Auth, Principal, $stateParams) {
    var trackerDelay = 1000;
    var servfields = ['lastModifiedBy', 'lastVersion', 'version', 'lastEditDate', 'creationDate', 'templateContent'];
    var kinds = ['experiment', 'project', 'notebook'];
    var save, get, clear, somethingUserChanged;
    var tracker = {};
    var states = {};

    init();

    return {
        canUndo: canUndo,
        canRedo: canRedo,
        undoAction: undoAction,
        redoAction: redoAction,
        track: trackEntityChanges,
        clearRecovery: clearRecovery,
        tracker : tracker
    };

    function resolvePrincipal(func) {
        return Principal.identity().then(func);
    };

    function init() {
        resolvePrincipal(function(user) {
            var subkey = user.id + '.autorecover.';
            var types = JSON.parse(localStorageService.get(subkey));
            if (!types) {
                types = {};
                kinds.forEach(function(kind) {
                    types[kind] = {
                        recoveries: {}
                    };
                });
            }

            function getKey(kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;

                return subkey + kind + '.entity.' + id;
            }

            clear = function(kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                var type = types[kind];
                var rec = type.recoveries[id];
                if (!rec) {
                    return;
                }
                delete type.recoveries[id];
                localStorageService.remove(getKey(kind, entity));
                localStorageService.set(subkey, angular.toJson(types));
                curtab = EntitiesBrowser.getActiveTab();
                delete curtab.tmpId;
                EntitiesBrowser.saveTabs();
            };

            save = function(kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                var clone = deleteServiceFields(angular.copy(entity));
                var type = types[kind];
                var rec = type.recoveries[id];
                if (!rec) {
                    rec = type.recoveries[id] = {
                        id: id,
                        name: entity.name,
                        kind: kind
                    };
                }
                rec.date = +new Date();
                delete rec.thisSession;
                localStorageService.set(subkey, angular.toJson(types));
                rec.thisSession = true;
                localStorageService.set(getKey(kind, entity), angular.toJson(clone));
            };

            get = function(kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                var type = types[kind];
                var rec = type.recoveries[id];
                if (!rec) {
                    return;
                }

                return {
                    entity: JSON.parse(localStorageService.get(getKey(kind, entity))),
                    rec: rec
                };
            };
        });

        angular.element(window).on('mousedown keydown', function() { //user really change something
            somethingUserChanged = true;
        })
    }

    function deleteServiceFields(entity) {
        servfields.forEach(function(field) {
            delete entity[field];
        });
        return entity;
    }

    function getFullId(entity) {
        if (!entity) {
            return false;
        }
        var curtab = EntitiesBrowser.getActiveTab();

        return entity.fullId || curtab.tmpId;
    }

    function canUndo(entity) {
        if (!entity) {
            return false;
        }
        var id = getFullId(entity);
        var state = states[id];

        return state && state.actions.length > 0 && state.aindex >= 0;
    }

    function canRedo(entity) {
        if (!entity) {
            return false;
        }
        var id = getFullId(entity);
        var state = states[id];

        return state && state.actions.length > 0 && state.aindex < state.actions.length - 1;
    }

    function undoAction(entity) {
        if (!entity) {
            return false;
        }
        var id = getFullId(entity);
        var state = states[id];
        if (state && state.aindex >= 0) {
            var act = state.actions[state.aindex--];
            angular.extend(entity, act);
            entity.$$undo = true;
        }
    }

    function redoAction(entity) {
        var id = getFullId(entity);
        if (!entity) {
            return false;
        }
        var state = states[id];
        if (states && state.aindex < state.actions.length - 1) {
            var act = (state.aindex < state.actions.length - 2) ? state.actions[state.aindex + 2] : state.last;
            state.aindex++;
            angular.extend(entity, act);
            entity.$$undo = true;
        }
    }

    function trackEntityChanges(kind, vm, onSetDirty) {
        var entity = vm[kind];
        var curtab = EntitiesBrowser.getActiveTab();
        if (!entity.id && !curtab.tmpId) {
            curtab.tmpId = +new Date();
            EntitiesBrowser.saveTabs();
        }
        var setDirty = function() {
            EntitiesBrowser.changeDirtyTab($stateParams, true);
            onSetDirty()
        }
        var fullId = getFullId(entity);
        var state = states[fullId] || {
            actions: []
        };
        states[fullId] = state;
        var rec = get(kind, entity);
        if (rec && !rec.rec.thisSession) {
            vm.restored = {
                rec: rec,
                resolve: function(val) {
                    if (val) {
                        angular.extend(entity, rec.entity);
                    }
                    clear(kind, entity);
                    setDirty()
                    vm.restored = null;
                }
            };
        } else if (rec && curtab.tmpId) {
            angular.extend(entity, rec.entity);
            setDirty();
        }
        vm.undoAction = function() {
            undoAction(vm[kind]);
            setDirty();
        };
        vm.redoAction = function() {
            redoAction(vm[kind]);
            setDirty();
        };
        vm.canUndo = function() {
            return canUndo(vm[kind]);
        };
        vm.canRedo = function() {
            return canRedo(vm[kind]);
        };
        var ctimeout;
        var prev, dirty = false;
        tracker.change = function(cur, old) {
            if (!somethingUserChanged) return;
            var entity = vm[kind];
            if (ctimeout) {
                $timeout.cancel(ctimeout);
            } else {
                prev = deleteServiceFields(angular.extend({}, old));
            }
            somethingUserChanged = false;
            if (dirty) {
                EntitiesBrowser.changeDirtyTab($stateParams, dirty);
            }
            ctimeout = $timeout(function() {
                if (dirty) {
                    save(kind, entity);
                    if (!entity.$$undo) {
                        if (state.aindex < state.actions.length - 1) {
                            state.actions = state.actions.slice(state.aindex);
                        }
                        state.actions.push(prev);
                        state.aindex = state.actions.length - 1;
                        state.last = angular.extend({}, entity);
                    }
                    Auth.prolong();
                }
                entity.$$undo = false;
                ctimeout = null;
            }, trackerDelay);
        };
        tracker.changeDirty = function(val, old) {
            var entity = vm[kind];
            dirty = val;
            if (!val && old) {
                clear(kind, entity);
                vm.restored = null;
            }
        }
    }

    function clearRecovery(kind, entity) {
        resolvePrincipal(function() {
            clear(kind, entity);
        });
    }
}