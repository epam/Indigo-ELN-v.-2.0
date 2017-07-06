(function () {
    angular
        .module('indigoeln')
        .controller('SidebarController', SidebarController);

    function SidebarController($scope, $state, $q, localStorageService, Project, Notebook, Experiment,
                               AllProjects, AllNotebooks, AllExperiments, Principal, $rootScope, $timeout, user) {

        var vm = this;
        var etimeout;
        var popExperiment;
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

        // check administration tab status from localstorage
        var admTabStatus = JSON.parse(localStorageService.get(SIDEBAR_ADMINISTRATION));

        vm.CONTENT_EDITOR = 'CONTENT_EDITOR';
        vm.USER_EDITOR = 'USER_EDITOR';
        vm.ROLE_EDITOR = 'ROLE_EDITOR';
        vm.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        vm.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
        vm.POPOVER_TEMPLATE = 'scripts/components/sidebar/sidebar-popover-template.html';
        vm.ADMINISTRATION_AUTHORITIES = [vm.USER_EDITOR, vm.ROLE_EDITOR, vm.TEMPLATE_EDITOR, vm.DICTIONARY_EDITOR].join(',');
        vm.myBookmarks = {};
        vm.allProjects = {};
        vm.$state = $state;

        vm.experimentEnter = experimentEnter;
        vm.experimentLeave = experimentLeave;
        vm.isOpen = isOpen;
        vm.getTreeItemById = getTreeItemById;
        vm.toggleAdministration = toggleAdministration;
        vm.toggleUsersAndRoles = toggleUsersAndRoles;
        vm.toggleAuthorities = toggleAuthorities;
        vm.toggleTemplates = toggleTemplates;
        vm.toggleDictionaries = toggleDictionaries;
        vm.toggleMyprojects = toggleMyprojects;
        vm.toggleMyprojectsNotebooks = toggleMyprojectsNotebooks;
        vm.toggleMyprojectsExperiments = toggleMyprojectsExperiments;
        vm.toggleBookmarksProjects = toggleBookmarksProjects;
        vm.toggleBookmarksNotebooks = toggleBookmarksNotebooks;
        vm.toggleBookmarksExperiments = toggleBookmarksExperiments;
        vm.onExperimentClick = onExperimentClick;

        init();

        function init() {

            // restore MYPROJECTS tab
            restoreMyTabs(MYPROJECTS);
            // restore BOOKMARKS tab
            restoreMyTabs(BOOKMARKS);


            if (admTabStatus) {
                vm.adminToggled = admTabStatus.isOpen;
            }

            restoreTabsByDefault();

            vm.activeMenuItem = compactIds(vm.$state.params);

            bindEvents();


        }


        function experimentEnter(experiment, notebook, project) {
            if (etimeout) {
                $timeout.cancel(etimeout);
            }
            popExperiment = null;
            etimeout = $timeout(function () {
                if (experiment.components) {
                    popExperiment = experiment;
                    return;
                }
                Experiment.get({
                    experimentId: experiment.id,
                    notebookId: notebook.id,
                    projectId: project.id
                }).$promise.then(function (data) {
                    popExperiment = experiment;
                    _.extend(experiment, data);
                    wrapName(experiment);
                });
            }, 500);
        }


        function experimentLeave() {
            if (etimeout) {
                $timeout.cancel(etimeout);
            }
            popExperiment = null;
        }

        function isOpen(experiment) {
            //TODO: we cant compare objects like this
            return experiment === popExperiment;
        }


        function getTreeItemById(items, itemId) {
            return _.find(items, function (projectItem) {
                return projectItem.id === itemId;
            });
        }


        function toggleAdministration() {
            vm.adminToggled = !vm.adminToggled;
            var data = {
                id: 'adm', isOpen: vm.adminToggled
            };
            localStorageService.set(SIDEBAR_ADMINISTRATION, JSON.stringify(data));
        }

        function toggleUsersAndRoles() {
            vm.$state.go('entities.user-management');
        }

        function toggleAuthorities() {
            vm.$state.go('entities.role-management');
        }

        function toggleTemplates() {
            vm.$state.go('entities.template');
        }

        function toggleDictionaries() {
            vm.$state.go('entities.dictionary-management');
        }

        function toggleMyprojects(parent, needAll) {
            toggleProjects(parent, needAll, promiseToggleMyprojectsProject, SIDEBAR_MYPROJECTS);
        }

        function toggleMyprojectsNotebooks(project, needAll) {
            toggleNotebooks(project, needAll, promiseToggleMyprojectsNotebook, SIDEBAR_MYPROJECTS_NOTEBOOKS);
        }

        function toggleMyprojectsExperiments(notebook, project, needAll) {
            toggleExperiments(notebook, project, needAll, promiseToggleMyprojectsExperiment, SIDEBAR_MYPROJECTS_EXPERIMENTS);
        }

        function toggleBookmarksProjects(parent, needAll) {
            toggleProjects(parent, needAll, promiseToggleBookmarksProject, SIDEBAR_BOOKMARKS);
        }

        function toggleBookmarksNotebooks(project, needAll) {
            toggleNotebooks(project, needAll, promiseToggleBookmarksNotebook, SIDEBAR_BOOKMARKS_NOTEBOOKS);
        }

        function toggleBookmarksExperiments(notebook, project, needAll) {
            toggleExperiments(notebook, project, needAll, promiseToggleBookmarksExperiment, SIDEBAR_BOOKMARKS_EXPERIMENTS);
        }

        function onExperimentClick(experiment, notebook, project) {
            var experimentId = experiment.id;
            var notebookId = notebook.id;
            var projectId = project.id;
            $state.go('entities.experiment-detail', {
                experimentId: experimentId,
                notebookId: notebookId,
                projectId: projectId,
                type: 'experiment'
            });
        }


        function toggleProjects(parent, needAll, fun, file) {
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
        }

        function toggleNotebooks(project, needAll, fun, file) {
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
        }

        function toggleExperiments(notebook, project, needAll, fun, file) {
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
        }


        function promiseToggleMyprojectsProject(agent, parent) {
            return promiseToggleProject(agent, parent, SIDEBAR_MYPROJECTS);
        }

        function promiseToggleMyprojectsNotebook(agent, project) {
            return promiseToggleNotebook(agent, project, SIDEBAR_MYPROJECTS_NOTEBOOKS);
        }

        function promiseToggleMyprojectsExperiment(agent, notebook, project, save) {
            return promiseToggleExperiment(agent, notebook, project, SIDEBAR_MYPROJECTS_EXPERIMENTS, save);
        }

        function promiseToggleBookmarksProject(agent, parent) {
            return promiseToggleProject(agent, parent, SIDEBAR_BOOKMARKS);
        }

        function promiseToggleBookmarksNotebook(agent, project) {
            return promiseToggleNotebook(agent, project, SIDEBAR_BOOKMARKS_NOTEBOOKS);
        }

        function promiseToggleBookmarksExperiment(agent, notebook, project, save) {
            return promiseToggleExperiment(agent, notebook, project, SIDEBAR_BOOKMARKS_EXPERIMENTS, save);
        }

        function promiseToggleProject(agent, parent, file) {
            return agent.query({}, function (result) {
                if (result.length) {
                    parent.projects = result;
                    var data = {
                        id: result[0].id, isOpen: true
                    };
                    localStorageService.set(file, JSON.stringify(data));
                }
            });
        }

        function promiseToggleNotebook(agent, project, file) {
            return agent.query({
                projectId: project.id
            }, function (result) {
                project.children = result;
                project.isOpen = true;
                saveInStorage({
                    id: project.id, isOpen: true
                }, file);
            });
        }

        function promiseToggleExperiment(agent, notebook, project, file, save) {
            return agent.query({
                notebookId: notebook.id, projectId: project.id
            }, function (result) {
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
        }

        function saveInStorage(data, file, field) {
            if (angular.isUndefined(typeof (field))) {
                field = FIELD_ID;
            }
            var initialValue = JSON.parse(localStorageService.get(file));
            if (initialValue) {
                var index = _.findIndex(initialValue, function (item) {
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
        }

        function closeNotebooksAndExperimentInLocalStorage(file) {
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
            angular.forEach(notebooks, function (item) {
                item.isOpen = false;
            });
            angular.forEach(experiments, function (item) {
                item.isOpen = false;
            });
            if (ls === BOOKMARKS) {
                localStorageService.set(SIDEBAR_BOOKMARKS_EXPERIMENTS, JSON.stringify(experiments));
                localStorageService.set(SIDEBAR_BOOKMARKS_NOTEBOOKS, JSON.stringify(notebooks));
            } else {
                localStorageService.set(SIDEBAR_MYPROJECTS_EXPERIMENTS, JSON.stringify(experiments));
                localStorageService.set(SIDEBAR_MYPROJECTS_NOTEBOOKS, JSON.stringify(notebooks));
            }
        }

        function closeExperimentsInLocalStorage(file, id) {
            var ls = file.split('.')[2];
            var experiments;
            if (ls === BOOKMARKS) {
                experiments = JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_EXPERIMENTS));
            } else {
                experiments = JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_EXPERIMENTS));
            }
            angular.forEach(experiments, function (item) {
                if (item.projectId === id) {
                    item.isOpen = false;
                }
            });
            if (ls === BOOKMARKS) {
                localStorageService.set(SIDEBAR_BOOKMARKS_EXPERIMENTS, JSON.stringify(experiments));
            } else {
                localStorageService.set(SIDEBAR_MYPROJECTS_EXPERIMENTS, JSON.stringify(experiments));
            }
        }


        function updateTreeForExperiments(event, data) {
            var project = null,
                notebook = null;
            vm.projects = vm.myBookmarks.projects;
            project = getTreeItemById(vm.projects, data.projectId);
            notebook = getTreeItemById(project.children, data.notebookId);

            if (vm.projects && project && notebook) {
                // find existing notebook and update children only
                Experiment.query({
                    notebookId: notebook.id, projectId: project.id
                }, function (expResult) {
                    notebook.children = expResult;
                    project.isOpen = true;
                    notebook.isOpen = true;
                });
                return;
            }
            // find and update projects
            Project.query(function (result) {
                vm.projects = result;
                vm.myBookmarks.projects = result;

                if (project && notebook) {
                    project.isOpen = true;
                    notebook.isOpen = true;
                }
            });

        }


        function updateNotebookName (event, data) {
            vm.projects = vm.myBookmarks.projects;
            var project = getTreeItemById(vm.projects, data.projectId);
            var notebook = getTreeItemById(project.children, data.notebook.id);
            notebook.name = data.notebook.name;
        }

        function updateExperiments(projects, callback) {
            angular.forEach(projects, function (project) {
                angular.forEach(project.children, function (notebook) {
                    angular.forEach(notebook.children, callback);
                });
            });
        }

        function updateStatuses(statuses) {
            var updStatus = function (experiment) {
                var status = statuses[experiment.fullId];
                if (status) {
                    experiment.status = status;
                }
            };
            updateExperiments(vm.myBookmarks.projects, updStatus);
            updateExperiments(vm.allProjects.projects, updStatus);
        }


        function restoreMyTabs(scope) {
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
                tabsOnScope = vm.allProjects;
                needAll = true;
            } else {
                firstLevelFile = SIDEBAR_BOOKMARKS;
                secondLevelFile = SIDEBAR_BOOKMARKS_NOTEBOOKS;
                thirdLevelFile = SIDEBAR_BOOKMARKS_EXPERIMENTS;
                firstLevelFun = promiseToggleBookmarksProject;
                secondLevelFun = promiseToggleBookmarksNotebook;
                thirdLevelFun = promiseToggleBookmarksExperiment;
                tabsOnScope = vm.myBookmarks;
                needAll = false;
            }

            var firstLevelTab = JSON.parse(localStorageService.get(firstLevelFile));
            if (firstLevelTab && firstLevelTab.isOpen) {
                var agent = needAll ? AllProjects : Project;
                firstLevelFun(agent, tabsOnScope, needAll).$promise
                    .then(function () {
                        return findNotebookIndexesToOpen(secondLevelFile, tabsOnScope);
                    })
                    .then(function (notebooksToOpen) {
                        return addNotebooksToBeOpened(notebooksToOpen, secondLevelFun, tabsOnScope, needAll);
                    })
                    .then(function (notebooks) {
                        return openNotebooks(notebooks);
                    })
                    .then(function () {
                        return openExperiments(thirdLevelFile, thirdLevelFun, tabsOnScope, needAll);
                    });
            }
        }


        function findNotebookIndexesToOpen(file, tabsOnScope) {
            var def = $q.defer();
            var secondLevelTab = JSON.parse(localStorageService.get(file));
            if (secondLevelTab) {
                var notebooksToOpen = [];
                _.each(secondLevelTab, function (status) {
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
        }


        function addNotebooksToBeOpened(notebooksToOpen, fun, tabsOnScope, needAll) {
            var promises = [];
            var defer = $q.defer();
            _.each(notebooksToOpen, function (notebookId) {
                var agent;
                if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'NOTEBOOK_READER'])) {
                    agent = needAll ? AllNotebooks : Notebook;
                }
                promises.push(fun(agent, tabsOnScope.projects[notebookId]).$promise);
            });
            defer.resolve(promises);

            return defer.promise;
        }

        function openNotebooks(notebooks) {
            var defer = $q.defer();
            $q.all(notebooks).then(function () {
                    defer.resolve();
                }
            );
            return defer.promise;
        }

        function openExperiments(file, fun, tabsOnScope, needAll) {
            var experimentsMyprojectsTabStatus = JSON.parse(localStorageService.get(file));
            if (experimentsMyprojectsTabStatus) {
                _.each(experimentsMyprojectsTabStatus, function (status) {
                    if (status.isOpen) {
                        _.each(tabsOnScope.projects, function (project) {
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
        }

        function compareNames(notebookName) {
            return function (item) {
                return item.name === notebookName;
            };
        }

        function compareIds(notebookId) {
            return function (item) {
                return item.id === notebookId;
            };
        }


        function extractParams(obj) {
            return {
                projectId: obj.projectId,
                notebookId: obj.notebookId,
                experimentId: obj.experimentId
            };
        }


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

        function restoreTabsByDefault() {
            var smthInStorage = JSON.parse(localStorageService.get(SIDEBAR_ADMINISTRATION)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_NOTEBOOKS)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_BOOKMARKS_EXPERIMENTS)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_NOTEBOOKS)) ||
                    JSON.parse(localStorageService.get(SIDEBAR_MYPROJECTS_EXPERIMENTS))
                ;
            if (!smthInStorage) {
                promiseToggleBookmarksProject(AllProjects, vm.myBookmarks);
            }
        }

        function bindEvents() {
            $rootScope.$on('$stateChangeSuccess', function (event, toState, toParams) {
                vm.activeMenuItem = compactIds(toParams);
            });

            var events = [];
            events.push($scope.$on('project-created', function () {
                Project.query(function (result) {
                    vm.projects = result;
                    vm.myBookmarks.projects = result;
                });
            }));

            events.push($scope.$on('notebook-created', function (event, data) {
                var project;
                if (vm.myBookmarks.projects) {
                    // find  existing project and update children
                    project = getTreeItemById(vm.myBookmarks.projects, data.projectId);
                    vm.projects = vm.myBookmarks.projects;
                    Notebook.query({
                        projectId: project.id
                    }, function (notebookResult) {
                        // fetch children only
                        project.children = notebookResult;
                        project.isOpen = true;
                    });
                } else {
                    // otherwise fetch all projects
                    Project.query(function (result) {
                        vm.projects = result;
                        vm.myBookmarks.projects = result;

                        project = getTreeItemById(vm.projects, data.projectId);
                        if (project) {
                            project.isOpen = true;
                        }
                    });
                }
            }));

            $scope.$on('notebook-changed', updateNotebookName);

            events.push($scope.$on('experiment-created', function (event, data) {
                updateTreeForExperiments(event, data);
            }));

            events.push($scope.$on('experiment-status-changed', function (event, data) {
                updateStatuses(data);
            }));

            events.push($scope.$on('experiment-updated', function (event, experiment) {
                if (experiment) {
                    var updExp = function (exp) {
                        if (exp.fullId === experiment.fullId) {
                            _.extend(exp, experiment);
                            wrapName(exp);
                        }
                    };
                    updateExperiments(vm.myBookmarks.projects, updExp);
                    updateExperiments(vm.allProjects.projects, updExp);
                }
            }));

            $scope.$on('$destroy', function () {
                _.each(events, function (event) {
                    event();
                });
            });
        }

        function wrapName(exp) {
            if (!exp.lastVersion || (exp.lastVersion && exp.experimentVersion > 1)) {
                exp.name += ' v' + exp.experimentVersion;
            }
        }


    }
})();
