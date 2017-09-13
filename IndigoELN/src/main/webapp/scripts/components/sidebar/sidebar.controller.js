(function() {
    angular
        .module('indigoeln')
        .controller('SidebarController', SidebarController);

    function SidebarController($scope, $state, localStorageService, Project, Notebook, Experiment,
                               AllProjects, AllNotebooks, AllExperiments, Principal, $rootScope, user) {
        var vm = this;
        var USER_PREFIX = user.id + '.';
        var FIELD_NAME = 'name';
        var FIELD_ID = 'id';

        var BOOKMARKS = 'bookmarks';
        var SIDEBAR_BOOKMARKS = USER_PREFIX + 'sidebar.bookmarks';
        var SIDEBAR_BOOKMARKS_NOTEBOOKS = USER_PREFIX + 'sidebar.bookmarks.notebooks';
        var SIDEBAR_BOOKMARKS_EXPERIMENTS = USER_PREFIX + 'sidebar.bookmarks.experiments';

        var SIDEBAR_MYPROJECTS = USER_PREFIX + 'sidebar.myprojects';
        var SIDEBAR_MYPROJECTS_NOTEBOOKS = USER_PREFIX + 'sidebar.myprojects.notebooks';
        var SIDEBAR_MYPROJECTS_EXPERIMENTS = USER_PREFIX + 'sidebar.myprojects.experiments';

        var SIDEBAR_ADMINISTRATION = USER_PREFIX + 'sidebar.administration';

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

        vm.getTreeItemById = getTreeItemById;
        vm.toggleAdministration = toggleAdministration;
        vm.toggleProjects = toggleProjects;
        vm.toggleMyProjects = toggleMyProjects;
        vm.onSelectNode = onSelectNode;

        init();

        function init() {
            vm.allProjectIsCollapsed = localStorageService.get(USER_PREFIX + 'allProjectIsCollapsed');
            vm.bookmarksIsCollapsed = localStorageService.get(USER_PREFIX + 'bookmarksIsCollapsed');

            vm.selectedFullId = localStorageService.get(USER_PREFIX + 'selectedFullId');
            vm.adminToggled = JSON.parse(localStorageService.get(SIDEBAR_ADMINISTRATION));

            bindEvents();
        }

        function onSelectNode(fullId) {
            vm.selectedFullId = fullId;
            localStorageService.set(USER_PREFIX + 'selectedFullId', fullId);
        }

        function getTreeItemById(items, itemId) {
            return _.find(items, function(projectItem) {
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

        function toggleProjects() {
            vm.allProjectIsCollapsed = !vm.allProjectIsCollapsed;
            localStorageService.set(USER_PREFIX + 'allProjectIsCollapsed', vm.allProjectIsCollapsed);
        }

        function toggleMyProjects() {
            vm.bookmarksIsCollapsed = !vm.bookmarksIsCollapsed;
            localStorageService.set(USER_PREFIX + 'bookmarksIsCollapsed', vm.bookmarksIsCollapsed);
        }

        // function toggleProjects(parent, needAll, fun, file) {
        //     var initialProject = JSON.parse(localStorageService.get(file));
        //     if (!parent.projects) {
        //         var agent = needAll ? AllProjects : Project;
        //         fun(agent, parent);
        //     } else {
        //         parent.projects = null;
        //         var data = {
        //             id: initialProject.id, isOpen: false
        //         };
        //         localStorageService.set(file, JSON.stringify(data));
        //         // close notebooks + experiments
        //         closeNotebooksAndExperimentInLocalStorage(file);
        //     }
        // }

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
            return agent.query({}, function(result) {
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
            }, function(result) {
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
        }

        function saveInStorage(data, file, field) {
            if (_.isUndefined(typeof (field))) {
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
            _.forEach(notebooks, function(item) {
                item.isOpen = false;
            });
            _.forEach(experiments, function(item) {
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
            _.forEach(experiments, function(item) {
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
            var myBookmarks = findProjectAndNotebookNodes(vm.myBookmarks.projects, data);
            var allProjects = findProjectAndNotebookNodes(vm.allProjects.projects, data);

            if (myBookmarks) {
                updateMyBookmarksTree(myBookmarks);
            }

            if (allProjects) {
                updateAllProjectsTree(allProjects);
            }
        }

        function findProjectAndNotebookNodes(tree, data) {
            var project = tree ? getTreeItemById(tree, data.projectId) : null;
            var notebook = project ? getTreeItemById(project.children, data.notebookId) : null;

            return (project && notebook) ? {project: project, notebook: notebook} : null;
        }

        function updateNotebookChildren(nodes, experiments) {
            nodes.notebook.children = experiments;
            nodes.notebook.isOpen = true;
            nodes.project.isOpen = true;
        }

        function updateMyBookmarksTree(myBookmarks) {
            Experiment.query({
                projectId: myBookmarks.project.id, notebookId: myBookmarks.notebook.id
            }, function(experiments) {
                updateNotebookChildren(myBookmarks, experiments);
            });
        }

        function updateAllProjectsTree(allProjects) {
            AllExperiments.query({
                projectId: allProjects.project.id, notebookId: allProjects.notebook.id
            }, function(experiments) {
                updateNotebookChildren(allProjects, experiments);
            });
        }

        function updateNotebookName(event, data) {
            var project = getTreeItemById(vm.myBookmarks.projects, data.projectId);
            var notebook = getTreeItemById(project.children, data.notebook.id);
            notebook.name = data.notebook.name;
        }

        function updateExperiments(projects, callback) {
            _.forEach(projects, function(project) {
                _.forEach(project.children, function(notebook) {
                    _.forEach(notebook.children, callback);
                });
            });
        }

        function updateStatuses(statuses) {
            var updStatus = function(experiment) {
                var status = statuses[experiment.fullId];
                if (status) {
                    experiment.status = status;
                }
            };
            updateExperiments(vm.myBookmarks.projects, updStatus);
            updateExperiments(vm.allProjects.projects, updStatus);
        }

        function bindEvents() {
            $scope.$on('$stateChangeSuccess', function(event, toState, toParams) {
                vm.onSelectNode(_.compact([toParams.projectId, toParams.notebookId, toParams.experimentId])
                    .join('-')
                    .toString());

            });

            $scope.$on('project-created', function($event, id) {
                // TODO: need to add created project to entities tree
                Project.query(function(result) {

                });
            });

            $scope.$on('notebook-created', function(event, data) {
                // TODO: need to add created notebook to entities tree

                var project;
                if (vm.myBookmarks.projects) {
                    // find  existing project and update children
                    project = getTreeItemById(vm.myBookmarks.projects, data.projectId);
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
                        vm.myBookmarks.projects = result;

                        project = getTreeItemById(vm.myBookmarks.projects, data.projectId);
                        if (project) {
                            project.isOpen = true;
                        }
                    });
                }
            });

            $scope.$on('notebook-changed', updateNotebookName);

            $scope.$on('experiment-created', function(event, data) {
                updateTreeForExperiments(event, data);
            });

            $scope.$on('experiment-status-changed', function(event, data) {
                updateStatuses(data);
            });

            $scope.$on('experiment-updated', function(event, experiment) {
                if (experiment) {
                    var updExp = function(exp) {
                        if (exp.fullId === experiment.fullId) {
                            _.extend(exp, experiment);
                            wrapName(exp);
                        }
                    };
                    updateExperiments(vm.myBookmarks.projects, updExp);
                    updateExperiments(vm.allProjects.projects, updExp);
                }
            });
        }

        function wrapName(exp) {
            if (!exp.lastVersion || (exp.lastVersion && exp.experimentVersion > 1)) {
                exp.name += ' v' + exp.experimentVersion;
            }
        }
    }
})();
