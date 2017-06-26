angular
    .module('indigoeln')
    .factory('AutoRecoverEngine', autoRecoverEngine);

/* @ngInject */
function autoRecoverEngine(Principal, localStorageService, $timeout, EntitiesBrowser, Auth) {
    var servfields = ['lastModifiedBy', 'lastVersion', 'version', 'lastEditDate', 'creationDate', 'templateContent'];
    var kinds = ['experiment', 'project', 'notebook'],
        save, get, clear, states = {};

    var resolvePrincipal = function (func) {
        return Principal.identity().then(func);
    };

    init();

    return {
        canUndo: canUndo,
        canRedo: canRedo,
        undoAction: undoAction,
        redoAction: redoAction,
        trackEntityChanges: trackEntityChanges,
        clearRecovery: clearRecovery
    };

    function init() {
        resolvePrincipal(function (user) {
            var subkey = user.id + '.autorecover.';
            var types = JSON.parse(localStorageService.get(subkey));
            if (!types) {
                types = {};
                kinds.forEach(function (kind) {
                    types[kind] = {recoveries: {}};
                });
            }

            function getKey(kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                return subkey + kind + '.entity.' + id;
            }

            clear = function (kind, entity) {
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

            save = function (kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                var clone = angular.copy(entity);
                servfields.forEach(function (field) {
                    delete clone[field];
                });
                var type = types[kind];
                var rec = type.recoveries[id];
                if (!rec) {
                    rec = type.recoveries[id] = {id: id, name: entity.name, kind: kind};
                }
                rec.date = +new Date();

                delete rec.thisSession;
                localStorageService.set(subkey, angular.toJson(types));
                rec.thisSession = true;
                localStorageService.set(getKey(kind, entity), angular.toJson(clone));
            };

            get = function (kind, entity) {
                var curtab = EntitiesBrowser.getActiveTab();
                var id = entity.id || curtab.tmpId;
                var type = types[kind];
                var rec = type.recoveries[id];
                if (!rec) {
                    return;
                }
                return {entity: JSON.parse(localStorageService.get(getKey(kind, entity))), rec: rec};
            };
        });
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

    //TODO: remove $scope
    function trackEntityChanges(entity, form, $scope, kind, vm) {
        resolvePrincipal(function () {
            var curtab = EntitiesBrowser.getActiveTab();
            if (!entity.id && !curtab.tmpId) {
                curtab.tmpId = +new Date();
                EntitiesBrowser.saveTabs();
            }

            var fullId = getFullId(entity);
            var state = states[fullId] || {actions: []};
            states[fullId] = state;
            var rec = get(kind, entity);
            if (rec && !rec.rec.thisSession) {
                vm.restored = {
                    rec: rec,
                    resolve: function (val) {
                        if (val) {
                            angular.extend(entity, rec.entity);
                        }
                        clear(kind, entity);
                        form.$setDirty(true);
                        vm.restored = null;
                    }
                };
            } else if (rec && curtab.tmpId) {
                angular.extend(entity, rec.entity);
                form.$setDirty(true);
            }
            vm.undoAction = function () {
                undoAction(entity);
                form.$setDirty(true);
            };
            vm.redoAction = function () {
                redoAction(entity);
                form.$setDirty(true);
            };
            vm.canUndo = function () {
                return canUndo(entity);
            };
            vm.canRedo = function () {
                return canRedo(entity);
            };
            var ctimeout, prev;
            var onChange = function (entity, old) {
                if (ctimeout) {
                    $timeout.cancel(ctimeout);
                } else {
                    prev = angular.extend({}, old);
                    delete prev.version;
                }
                ctimeout = $timeout(function () {
                    if (form.$dirty) {
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
                }, 1000);
            };
            var unbind = $scope.$watch(function () {
                return vm[kind];
            }, onChange, true);
            var unbindDirty = $scope.$watch(form.$name + '.$dirty', function (val, old) {
                if (!val && old) {
                    clear(kind, entity);
                    vm.restored = null;
                }
            }, true);
            $scope.$on('$destroy', function () {
                unbind();
                unbindDirty();
            });
        });
    }

    function clearRecovery(kind, entity) {
        resolvePrincipal(function () {
            clear(kind, entity);
        });
    }
}


