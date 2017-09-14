(function() {
    angular
        .module('indigoeln')
        .controller('SidebarController', SidebarController);

    function SidebarController($scope, $state, sidebarCache, Project, Notebook, Experiment, AllExperiments) {
        var vm = this;

        vm.CONTENT_EDITOR = 'CONTENT_EDITOR';
        vm.USER_EDITOR = 'USER_EDITOR';
        vm.ROLE_EDITOR = 'ROLE_EDITOR';
        vm.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        vm.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
        vm.POPOVER_TEMPLATE = 'scripts/components/sidebar/sidebar-popover-template.html';
        vm.ADMINISTRATION_AUTHORITIES = [vm.USER_EDITOR, vm.ROLE_EDITOR, vm.TEMPLATE_EDITOR, vm.DICTIONARY_EDITOR].join(',');
        vm.$state = $state;

        vm.getTreeItemById = getTreeItemById;
        vm.toggleAdministration = toggleAdministration;
        vm.toggleProjects = toggleProjects;
        vm.toggleMyProjects = toggleMyProjects;
        vm.onSelectNode = onSelectNode;

        init();

        function init() {
            vm.allProjectIsCollapsed = sidebarCache.get('allProjectIsCollapsed');
            vm.bookmarksIsCollapsed = sidebarCache.get('bookmarksIsCollapsed');

            vm.selectedFullId = sidebarCache.get('selectedFullId');
            vm.adminToggled = sidebarCache.get('adminToggled');

            bindEvents();
        }

        function onSelectNode(fullId) {
            if (vm.selectedFullId !== fullId) {
                vm.selectedFullId = fullId;
                sidebarCache.put('selectedFullId', fullId);
            }
        }

        function getTreeItemById(items, itemId) {
            return _.find(items, function(projectItem) {
                return projectItem.id === itemId;
            });
        }

        function toggleAdministration() {
            vm.adminToggled = !vm.adminToggled;
            sidebarCache.put('adminToggled', vm.adminToggled);
        }

        function toggleProjects() {
            vm.allProjectIsCollapsed = !vm.allProjectIsCollapsed;
            sidebarCache.put('allProjectIsCollapsed', vm.allProjectIsCollapsed);
        }

        function toggleMyProjects() {
            vm.bookmarksIsCollapsed = !vm.bookmarksIsCollapsed;
            sidebarCache.put('bookmarksIsCollapsed', vm.bookmarksIsCollapsed);
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

        function getFullIdFromParams(toParams) {
            return _.compact([toParams.projectId, toParams.notebookId, toParams.experimentId])
                .join('-')
                .toString();
        }

        function bindEvents() {
            $scope.$on('$stateChangeSuccess', function(event, toState, toParams) {
                vm.onSelectNode(getFullIdFromParams(toParams));
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
