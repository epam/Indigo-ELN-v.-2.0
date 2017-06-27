angular
    .module('indigoeln')
    .controller('SidebarController', function($scope, $state, $q, localStorageService, Project, Notebook, Experiment,
                                               AllProjects, AllNotebooks, AllExperiments, Principal, $rootScope, EntitiesBrowser, $uibTooltip, $timeout) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.USER_EDITOR = 'USER_EDITOR';
        $scope.ROLE_EDITOR = 'ROLE_EDITOR';
        $scope.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        $scope.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
        $scope.POPOVER_TEMPLATE = 'scripts/components/sidebar/sidebar-popover-template.html';
        $scope.ADMINISTRATION_AUTHORITIES = [$scope.USER_EDITOR, $scope.ROLE_EDITOR, $scope.TEMPLATE_EDITOR, $scope.DICTIONARY_EDITOR].join(',');
        $scope.myBookmarks = {};
        $scope.allProjects = {};
        $scope.$state = $state;

        var etimeout,
            popExperiment;
        $scope.experimentEnter = function(experiment, notebook, project) {
            if (etimeout) {
                $timeout.cancel(etimeout);
            }
            popExperiment = null;
            etimeout = $timeout(function() {
                if (experiment.components) {
                    popExperiment = experiment;
                    console.log('popped', popExperiment);

                    return;
                }
                Experiment.get({
                    experimentId: experiment.id,
                    notebookId: notebook.id,
                    projectId: project.id
                }).$promise.then(function(data) {
                    popExperiment = experiment;
                    angular.extend(experiment, data);
                    wrapName(experiment);
                });
            }, 500);
        };

        function wrapName(exp) {
            if (!exp.lastVersion || (exp.lastVersion && exp.experimentVersion > 1)) {
                exp.name += ' v' + exp.experimentVersion;
            }
        }

        $scope.experimentLeave = function() {
            if (etimeout) {
                $timeout.cancel(etimeout);
            }
            popExperiment = null;
        };
        $scope.isOpen = function(experiment) {
            return experiment == popExperiment;
        };


        var updateExperiments = function(projects, callback) {
            angular.forEach(projects, function(project) {
                angular.forEach(project.children, function(notebook) {
                    angular.forEach(notebook.children, callback);
                });
            });
        };

        var updateStatuses = function(statuses) {
            var updStatus = function(experiment) {
                var status = statuses[experiment.fullId];
                if (status) {
                    experiment.status = status;
                }
            };
            updateExperiments($scope.myBookmarks.projects, updStatus);
            updateExperiments($scope.allProjects.projects, updStatus);
        };

        var onProjectCreatedEvent = $scope.$on('project-created', function() {
            Project.query(function(result) {
                $scope.projects = result;
                $scope.myBookmarks.projects = result;
            });
        });

        $scope.getTreeItemById = function(items, itemId) {
            return _.find(items, function(projectItem) {
                return projectItem.id === itemId;
            });
        };

        var onNotebookCreatedEvent = $scope.$on('notebook-created', function(event, data) {
            var project;
            if ($scope.myBookmarks.projects) {
                // find  existing project and update children
                project = $scope.getTreeItemById($scope.myBookmarks.projects, data.projectId);
                $scope.projects = $scope.myBookmarks.projects;
                Notebook.query({
                    projectId: project.id
                }, function(notebookResult) {
                    // fetch children only
                    project.children = notebookResult;
                    project.isOpen = true;
                });
            } else {
                // otherwise fetch all projects
                Project.query(function(result) {
                    $scope.projects = result;
                    $scope.myBookmarks.projects = result;

                    project = $scope.getTreeItemById($scope.projects, data.projectId);
                    if (project) {
                        project.isOpen = true;
                    }
                });
            }
        });

        var updateTreeForExperiments = function(event, data) {
            var project = null,
                notebook = null;
            $scope.projects = $scope.myBookmarks.projects;
            project = $scope.getTreeItemById($scope.projects, data.projectId);
            notebook = $scope.getTreeItemById(project.children, data.notebookId);

            if ($scope.projects && project && notebook) {
                // find existing notebook and update children only
                Experiment.query({
                    notebookId: notebook.id, projectId: project.id
                }, function(expResult) {
                    notebook.children = expResult;
                    project.isOpen = true;
                    notebook.isOpen = true;
                });
            } else {
                // find and update projects
                Project.query(function(result) {
                    $scope.projects = result;
                    $scope.myBookmarks.projects = result;

                    if (project && notebook) {
                        project.isOpen = true;
                        notebook.isOpen = true;
                    }
                });
            }
        };

        var onExperimentCreatedEvent = $scope.$on('experiment-created', function(event, data) {
            updateTreeForExperiments(event, data);
        });

        var onExperimentStatusChangedEvent = $scope.$on('experiment-status-changed', function(event, data) {
            updateStatuses(data);
        });

        var onExperimentChangedEvent = $scope.$on('experiment-updated', function(event, experiment) {
            if (experiment) {
                var updExp = function(exp) {
                    if (exp.fullId === experiment.fullId) {
                        angular.extend(exp, experiment);
                        wrapName(exp);
                    }
                };
                updateExperiments($scope.myBookmarks.projects, updExp);
                updateExperiments($scope.allProjects.projects, updExp);
            }
        });

        $scope.$on('$destroy', function() {
            onExperimentCreatedEvent();
            onNotebookCreatedEvent();
            onProjectCreatedEvent();
            onExperimentStatusChangedEvent();
            onExperimentChangedEvent();
        });

        // ------------localstorage----------------
        Principal.identity(true)
            .then(function(user) {
                var USER_PREFIX = user.id + '.';
                var FIELD_NAME = 'name';
                var FIELD_ID = 'id';

                var BOOKMARKS = 'bookmarks';
                var SIDEBAR_BOOKMARKS = USER_PREFIX + 'sidebar.bookmarks';
                var SIDEBAR_BOOKMARKS_NOTEBOOKS = USER_PREFIX + 'sidebar.bookmarks.notebooks';
                var SIDEBAR_BOOKMARKS_EXPERIMENTS = USER_PREFIX + 'sidebar.bookmarks.experiments';

                var MYPROJECTS = 'myprojects';
                var SIDEBAR_MYPROJECTS = USER_PREFIX + 'sidebar.myprojects';
                var SIDEBAR_MYPROJECTS_NOTEBOOKS = USER_PREFIX + 'sidebar.myprojects.notebooks';
                var SIDEBAR_MYPROJECTS_EXPERIMENTS = USER_PREFIX + 'sidebar.myprojects.experiments';

                var SIDEBAR_ADMINISTRATION = USER_PREFIX + 'sidebar.administration';
                var saveInStorage = function(data, file, field) {
                    if (angular.isUndefined(typeof (field))) {
                        field = FIELD_ID;
                    }
                    var initialValue = JSON.parse(localStorageService.get(file));
                    if (initialValue) {
                        var index = _.findIndex(initialValue, function(item) {
                            return item[field] === data[field];
                        });
                        if (index !== -1) {
                            initialValue[index] = data;
                        } else {
                            initialValue.push(data);
                        }
                    } else {
                        initialValue = [];
                        initialValue.push(data);
                    }

                    localStorageService.set(file, JSON.stringify(initialValue));
                };

                var closeNotebooksAndExperimentInLocalStorage = function(file) {
                    var ls = file.split('.')[2];
                    var notebooks;
                    var experiments;
                    if (ls === BOOKMARKS) {
                        notebooks = JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_NOTEBOOKS));
                        experiments = JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_EXPERIMENTS));
                    } else {
                        notebooks = JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_NOTEBOOKS));
                        experiments = JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_EXPERIMENTS));
                    }
                    angular.forEach(notebooks, function(item) {
                        item.isOpen = false;
                    });
                    angular.forEach(experiments, function(item) {
                        item.isOpen = false;
                    });
                    if (ls === BOOKMARKS) {
                        localStorageService.set(SIDEBAR_BOOKMARKS_EXPERIMENTS, JSON.stringify(experiments));
                        localStorageService.set(SIDEBAR_BOOKMARKS_NOTEBOOKS, JSON.stringify(notebooks));
                    } else {
                        localStorageService.set(SIDEBAR_MYPROJECTS_EXPERIMENTS, JSON.stringify(experiments));
                        localStorageService.set(SIDEBAR_MYPROJECTS_NOTEBOOKS, JSON.stringify(notebooks));
                    }
                };

                var closeExperimentsInLocalStorage = function(file, id) {
                    var ls = file.split('.')[2];
                    var experiments;
                    if (ls === BOOKMARKS) {
                        experiments = JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_EXPERIMENTS));
                    } else {
                        experiments = JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_EXPERIMENTS));
                    }
                    angular.forEach(experiments, function(item) {
                        if (item.projectId === id) {
                            item.isOpen = false;
                        }
                    });
                    if (ls === BOOKMARKS) {
                        localStorageService.set(SIDEBAR_BOOKMARKS_EXPERIMENTS, JSON.stringify(experiments));
                    } else {
                        localStorageService.set(SIDEBAR_MYPROJECTS_EXPERIMENTS, JSON.stringify(experiments));
                    }
                };

                // --------------promises----------------
                var promiseToggleProject = function(agent, parent, file) {
                    return agent.query({}, function(result) {
                        if (result.length) {
                            parent.projects = result;
                            var data = {
                                id: result[0].id, isOpen: true
                            };
                            localStorageService.set(file, JSON.stringify(data));
                        }
                    });
                };

                var promiseToggleNotebook = function(agent, project, file) {
                    return agent.query({
                        projectId: project.id
                    }, function(result) {
                        project.children = result;
                        project.isOpen = true;
                        saveInStorage({
                            id: project.id, isOpen: true
                        }, file);
                    });
                };

                var promiseToggleExperiment = function(agent, notebook, project, file, save) {
                    return agent.query({
                        notebookId: notebook.id, projectId: project.id
                    }, function(result) {
                        notebook.children = result;
                        notebook.isOpen = true;
                        if (save) {
                            saveInStorage({
                                name: notebook.name,
                                isOpen: true,
                                projectId: notebook.projectId
                            }, file, FIELD_NAME);
                        }
                    });
                };

                var toggleProjects = function(parent, needAll, fun, file) {
                    var initialProject = JSON.parse(localStorageService.get(file));
                    if (!parent.projects) {
                        var agent = needAll ? AllProjects : Project;
                        fun(agent, parent);
                    } else {
                        parent.projects = null;
                        var data = {
                            id: initialProject.id, isOpen: false
                        };
                        localStorageService.set(file, JSON.stringify(data));
                        // close notebooks + experiments
                        closeNotebooksAndExperimentInLocalStorage(file);
                    }
                };

                var toggleNotebooks = function(project, needAll, fun, file) {
                    $state.go('entities.project-detail', {
                        projectId: project.id
                    });
                    if (project.isOpen) {
                        project.children = null;
                        project.isOpen = false;
                        saveInStorage({
                            id: project.id, isOpen: false
                        }, file);
                        // close experiments with id == experiment.projectId
                        closeExperimentsInLocalStorage(file, project.id);
                    } else if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'NOTEBOOK_READER'])) {
                        var agent = needAll ? AllNotebooks : Notebook;
                        fun(agent, project);
                    } else {
                        project.children = null;
                        project.isOpen = true;
                        saveInStorage({
                            id: project.id, isOpen: true
                        }, file);
                    }
                };

                var toggleExperiments = function(notebook, project, needAll, fun, file) {
                    $state.go('entities.notebook-detail', {
                        notebookId: notebook.id, projectId: project.id
                    });
                    if (notebook.isOpen) {
                        notebook.children = null;
                        notebook.isOpen = false;
                        saveInStorage({
                            name: notebook.name,
                            isOpen: false,
                            projectId: notebook.projectId
                        }, file, FIELD_NAME);
                    } else if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'EXPERIMENT_READER'])) {
                        var agent = needAll ? AllExperiments : Experiment;
                        fun(agent, notebook, project, true);
                    } else {
                        notebook.children = null;
                        notebook.isOpen = true;
                        saveInStorage({
                            name: notebook.name,
                            isOpen: true,
                            projectId: notebook.projectId
                        }, file, FIELD_NAME);
                    }
                };

                // ----------------projects----------------

                var promiseToggleMyprojectsProject = function(agent, parent) {
                    return promiseToggleProject(agent, parent, SIDEBAR_MYPROJECTS);
                };

                var promiseToggleMyprojectsNotebook = function(agent, project) {
                    return promiseToggleNotebook(agent, project, SIDEBAR_MYPROJECTS_NOTEBOOKS);
                };

                var promiseToggleMyprojectsExperiment = function(agent, notebook, project, save) {
                    return promiseToggleExperiment(agent, notebook, project, SIDEBAR_MYPROJECTS_EXPERIMENTS, save);
                };


                $scope.toggleMyprojects = function(parent, needAll) {
                    toggleProjects(parent, needAll, promiseToggleMyprojectsProject, SIDEBAR_MYPROJECTS);
                };

                $scope.toggleMyprojectsNotebooks = function(project, needAll) {
                    toggleNotebooks(project, needAll, promiseToggleMyprojectsNotebook, SIDEBAR_MYPROJECTS_NOTEBOOKS);
                };

                $scope.toggleMyprojectsExperiments = function(notebook, project, needAll) {
                    toggleExperiments(notebook, project, needAll, promiseToggleMyprojectsExperiment, SIDEBAR_MYPROJECTS_EXPERIMENTS);
                };

                // -------------------bookmarks------------------
                var promiseToggleBookmarksProject = function(agent, parent) {
                    return promiseToggleProject(agent, parent, SIDEBAR_BOOKMARKS);
                };

                var promiseToggleBookmarksNotebook = function(agent, project) {
                    return promiseToggleNotebook(agent, project, SIDEBAR_BOOKMARKS_NOTEBOOKS);
                };

                var promiseToggleBookmarksExperiment = function(agent, notebook, project, save) {
                    return promiseToggleExperiment(agent, notebook, project, SIDEBAR_BOOKMARKS_EXPERIMENTS, save);
                };


                $scope.toggleBookmarksProjects = function(parent, needAll) {
                    toggleProjects(parent, needAll, promiseToggleBookmarksProject, SIDEBAR_BOOKMARKS);
                };

                $scope.toggleBookmarksNotebooks = function(project, needAll) {
                    toggleNotebooks(project, needAll, promiseToggleBookmarksNotebook, SIDEBAR_BOOKMARKS_NOTEBOOKS);
                };

                $scope.toggleBookmarksExperiments = function(notebook, project, needAll) {
                    toggleExperiments(notebook, project, needAll, promiseToggleBookmarksExperiment, SIDEBAR_BOOKMARKS_EXPERIMENTS);
                };

                // -----------restoration loop ---------------

                var compareNames = function(notebookName) {
                    return function(item) {
                        return item.name === notebookName;
                    };
                };

                var compareIds = function(notebookId) {
                    return function(item) {
                        return item.id === notebookId;
                    };
                };

                var openExperiments = function(file, fun, tabsOnScope, needAll) {
                    var experimentsMyprojectsTabStatus = JSON.parse(localStorageService.get(file));
                    if (experimentsMyprojectsTabStatus) {
                        _.each(experimentsMyprojectsTabStatus, function(status) {
                            if (status.isOpen) {
                                _.each(tabsOnScope.projects, function(project) {
                                    var index = _.findIndex(project.children, compareNames(status.name));
                                    if (index > -1) {
                                        var agent;
                                        if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'EXPERIMENT_READER'])) {
                                            agent = needAll ? AllExperiments : Experiment;
                                        }
                                        fun(agent, project.children[index], project);
                                    }
                                });
                            }
                        });
                    }
                };

                var findNotebookIndexesToOpen = function(file, tabsOnScope) {
                    var def = $q.defer();
                    var secondLevelTab = JSON.parse(localStorageService.get(file));
                    if (secondLevelTab) {
                        var notebooksToOpen = [];
                        _.each(secondLevelTab, function(status) {
                            if (status.isOpen) {
                                var index = _.findIndex(tabsOnScope.projects, compareIds(status.id));
                                if (index > -1) {
                                    notebooksToOpen.push(index);
                                }
                            }
                        });
                        def.resolve(notebooksToOpen);
                    }

                    return def.promise;
                };

                var addNotebooksToBeOpened = function(notebooksToOpen, fun, tabsOnScope, needAll) {
                    var promises = [];
                    var defer = $q.defer();
                    _.each(notebooksToOpen, function(notebookId) {
                        var agent;
                        if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'NOTEBOOK_READER'])) {
                            agent = needAll ? AllNotebooks : Notebook;
                        }
                        promises.push(fun(agent, tabsOnScope.projects[notebookId]).$promise);
                    });
                    defer.resolve(promises);

                    return defer.promise;
                };

                var openNotebooks = function(notebooks) {
                    var defer = $q.defer();
                    $q.all(notebooks).then(function() {
                        defer.resolve();
                    }
                    );

                    return defer.promise;
                };

                $scope.restoreMyTabs = function(scope) {
                    var firstLevelFile;
                    var secondLevelFile;
                    var thirdLevelFile;
                    var firstLevelFun;
                    var secondLevelFun;
                    var thirdLevelFun;
                    var tabsOnScope;
                    var needAll;
                    if (scope === MYPROJECTS) {
                        firstLevelFile = SIDEBAR_MYPROJECTS;
                        secondLevelFile = SIDEBAR_MYPROJECTS_NOTEBOOKS;
                        thirdLevelFile = SIDEBAR_MYPROJECTS_EXPERIMENTS;
                        firstLevelFun = promiseToggleMyprojectsProject;
                        secondLevelFun = promiseToggleMyprojectsNotebook;
                        thirdLevelFun = promiseToggleMyprojectsExperiment;
                        tabsOnScope = $scope.allProjects;
                        needAll = true;
                    } else {
                        firstLevelFile = SIDEBAR_BOOKMARKS;
                        secondLevelFile = SIDEBAR_BOOKMARKS_NOTEBOOKS;
                        thirdLevelFile = SIDEBAR_BOOKMARKS_EXPERIMENTS;
                        firstLevelFun = promiseToggleBookmarksProject;
                        secondLevelFun = promiseToggleBookmarksNotebook;
                        thirdLevelFun = promiseToggleBookmarksExperiment;
                        tabsOnScope = $scope.myBookmarks;
                        needAll = false;
                    }

                    var firstLevelTab = JSON.parse(localStorageService.get(firstLevelFile));
                    if (firstLevelTab && firstLevelTab.isOpen) {
                        var agent = needAll ? AllProjects : Project;
                        firstLevelFun(agent, tabsOnScope, needAll).$promise
                            .then(function() {
                                return findNotebookIndexesToOpen(secondLevelFile, tabsOnScope);
                            })
                            .then(function(notebooksToOpen) {
                                return addNotebooksToBeOpened(notebooksToOpen, secondLevelFun, tabsOnScope, needAll);
                            })
                            .then(function(notebooks) {
                                return openNotebooks(notebooks);
                            })
                            .then(function() {
                                return openExperiments(thirdLevelFile, thirdLevelFun, tabsOnScope, needAll);
                            });
                    }
                };
                // restore MYPROJECTS tab
                $scope.restoreMyTabs(MYPROJECTS);
                // restore BOOKMARKS tab
                $scope.restoreMyTabs(BOOKMARKS);


                $scope.onExperimentClick = function(experiment, notebook, project) {
                    var experimentId = experiment.id;
                    var notebookId = notebook.id;
                    var projectId = project.id;
                    $state.go('entities.experiment-detail', {
                        experimentId: experimentId,
                        notebookId: notebookId,
                        projectId: projectId,
                        type: 'experiment'
                    });

                    // EntitiesBrowser.goToTab(fullId);
                };

                // check administration tab status from localstorage
                var admTabStatus = JSON.parse(localStorageService.get(SIDEBAR_ADMINISTRATION));
                if (admTabStatus) {
                    $scope.adminToggled = admTabStatus.isOpen;
                }


                $scope.toggleAdministration = function() {
                    $scope.adminToggled = !$scope.adminToggled;
                    var data = {
                        id: 'adm', isOpen: $scope.adminToggled
                    };
                    localStorageService.set(SIDEBAR_ADMINISTRATION, JSON.stringify(data));
                };

                $scope.toggleUsersAndRoles = function() {
                    $state.go('entities.user-management');
                };

                $scope.toggleAuthorities = function() {
                    $state.go('entities.role-management');
                };

                $scope.toggleTemplates = function() {
                    $state.go('entities.template');
                };

                $scope.toggleDictionaries = function() {
                    $state.go('entities.dictionary-management');
                };


                var extractParams = function(obj) {
                    return {
                        projectId: obj.projectId,
                        notebookId: obj.notebookId,
                        experimentId: obj.experimentId
                    };
                };


                function compactIds(params) {
                    params = extractParams(params);
                    var paramsArr = [];
                    if (params.projectId) {
                        paramsArr.push(params.projectId);
                    }
                    if (params.notebookId) {
                        paramsArr.push(params.notebookId);
                    }
                    if (params.experimentId) {
                        paramsArr.push(params.experimentId);
                    }

                    return paramsArr.join('-');
                }


                // if nothing was in localstorage
                var restoreTabsByDefault = function() {
                    var smthInStorage = JSON.parse(localStorageService.get(SIDEBAR_ADMINISTRATION)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_NOTEBOOKS)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_EXPERIMENTS)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_NOTEBOOKS)) ||
                            JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_EXPERIMENTS))
                        ;
                    if (!smthInStorage) {
                        promiseToggleBookmarksProject(AllProjects, $scope.myBookmarks);
                    }
                };
                restoreTabsByDefault();

                $scope.activeMenuItem = compactIds($state.params);

                $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams) {
                    $scope.activeMenuItem = compactIds(toParams);
                });
                console.warn($scope.ADMINISTRATION_AUTHORITIES);
            });
    });
