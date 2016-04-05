'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, User, Project, Notebook, Experiment,
                                               AllProjects, AllNotebooks, AllExperiments, PermissionManagement,
                                               Principal, Alert, ExperimentStatus) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.USER_EDITOR = 'USER_EDITOR';
        $scope.ROLE_EDITOR = 'ROLE_EDITOR';
        $scope.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        $scope.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
        $scope.ADMINISTRATION_AUTHORITIES = [$scope.USER_EDITOR, $scope.ROLE_EDITOR,
            $scope.TEMPLATE_EDITOR, $scope.DICTIONARY_EDITOR].join(',');
        $scope.myBookmarks = {};
        $scope.allProjects = {};

        var updateProjectsStatuses = function(projects, statuses) {
            angular.forEach(projects, function(project) {
                angular.forEach(project.children, function(notebook) {
                    angular.forEach(notebook.children, function(experiment) {
                        var status = statuses[experiment.fullId];
                        if (status) {
                            experiment.status = status;
                        }
                    });
                });
            });
        };

        var updateStatuses = function(statuses) {
            updateProjectsStatuses($scope.myBookmarks.projects, statuses);
            updateProjectsStatuses($scope.allProjects.projects, statuses);
        };

        var onProjectCreatedEvent = $scope.$on('project-created', function () {
            Project.query(function (result) {
                $scope.projects = result;
                $scope.myBookmarks.projects = result;
            });
        });

        var onNotebookCreatedEvent = $scope.$on('notebook-created', function(event, data) {
            var project;
            if($scope.myBookmarks.projects &&
                    (project = $scope.getTreeItemById($scope.myBookmarks.projects, data.projectId))) { //find  existing project and update children
                $scope.projects = $scope.myBookmarks.projects;
                Notebook.query({projectId: project.id}, function (notebookResult) { //fetch children only
                    project.children = notebookResult;
                    project.isOpen = true;
                });
            } else { //otherwise fetch all projects
                Project.query(function (result) { //fetch all projects
                    $scope.projects = result;
                    $scope.myBookmarks.projects = result;

                    project = $scope.getTreeItemById($scope.projects, data.projectId);
                    if(project) {
                        project.isOpen = true;
                    }
                });
            }
        });

        var onExperimentCreatedEvent = $scope.$on('experiment-created', function(event, data) {
            updateTreeForExperiments(event, data);
        });

        var onExperimentStatusChangedEvent = $scope.$on('experiment-status-changed', function(event, data) {
            var statuses = {};
            statuses[data.id] = data.status;
            updateStatuses(statuses);
        });

        var updateTreeForExperiments = function (event, data) {
            var project = null, notebook = null;
            $scope.projects = $scope.myBookmarks.projects;

            if ($scope.projects && (project = $scope.getTreeItemById($scope.projects, data.projectId)) &&
                (notebook = $scope.getTreeItemById(project.children, data.notebookId)))
            { //find existing notebook and update children only
                Experiment.query({notebookId: notebook.id, projectId : project.id}, function (expResult) {
                    notebook.children = expResult;
                    project.isOpen = true;
                    notebook.isOpen = true;
                });
            } else { //find and update projects

                Project.query(function (result) {
                    $scope.projects = result;
                    $scope.myBookmarks.projects = result;

                    if((project = $scope.getTreeItemById($scope.projects, data.projectId)) &&
                        (notebook = $scope.getTreeItemById(project.children, data.notebookId))) {
                        project.isOpen = true;
                        notebook.isOpen = true;
                    }
                });
            }
        };

        $scope.$on('$destroy', function() {
            onExperimentCreatedEvent();
            onNotebookCreatedEvent();
            onProjectCreatedEvent();
            onExperimentStatusChangedEvent();
        });

        $scope.getTreeItemById = function(items, itemId) {
            var result = null;
            if(items) {
                items.some(function (projectItem) {
                    return projectItem.id === itemId ? ( (result = projectItem), true) : false;
                });
            }
            return result;
        };

        $scope.toggleProjects = function (parent, needAll) {
            if (!parent.projects) {
                var agent = needAll ? AllProjects : Project;
                agent.query({}, function (result) {
                    parent.projects = result;
                });
            } else {
                parent.projects = null;
            }
        };

        $scope.toggleNotebooks = function (project, needAll) {
            $state.go('entities.project-detail', {projectId: project.id});
            if (project.isOpen) {
                project.children = null;
                project.isOpen = false;
            } else {
                if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'NOTEBOOK_READER'])) {
                    var agent = needAll ? AllNotebooks : Notebook;
                    agent.query({projectId: project.id}, function (result) {
                        project.children = result;
                        project.isOpen = true;
                    });
                } else {
                    project.children = null;
                    project.isOpen = true;
                }
            }
        };

        $scope.toggleExperiments = function (notebook, project, needAll) {
            $state.go('entities.notebook-detail', {notebookId: notebook.id, projectId: project.id});
            if (notebook.isOpen) {
                notebook.children = null;
                notebook.isOpen = false;
            } else {
                if (Principal.hasAnyAuthority(['CONTENT_EDITOR', 'EXPERIMENT_READER'])) {
                    var agent = needAll ? AllExperiments : Experiment;
                    agent.query({notebookId: notebook.id, projectId: project.id}, function (result) {
                        notebook.children = result;
                        notebook.isOpen = true;
                    });
                } else {
                    notebook.children = null;
                    notebook.isOpen = true;
                }
            }
        };

        $scope.onExperimentClick = function (experiment, notebook, project) {
            $state.go('entities.experiment-detail', {
                experimentId: experiment.id,
                notebookId: notebook.id,
                projectId: project.id
            });
        };

        $scope.toggleAdministration = function () {
            $scope.adminToggled = !$scope.adminToggled;
        };

        $scope.toggleUsersAndRoles = function () {
            $state.go('user-management');
        };

        $scope.toggleAuthorities = function () {
            $state.go('role-management');
        };

        $scope.toggleTemplates = function () {
            $state.go('template');
        };

        $scope.toggleDictionaries = function () {
            $state.go('dictionary-management');
        };

        ExperimentStatus.connect();
        ExperimentStatus.subscribe();
        ExperimentStatus.receive().then(null, null, function(data) {
            updateStatuses(data);
        });

    });