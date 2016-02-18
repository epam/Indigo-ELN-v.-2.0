'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', function ($scope, $state, User, Project, Notebook, Experiment, AllProjects, AllNotebooks, AllExperiments,
                                               ExperimentBrowser) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.USER_EDITOR = 'USER_EDITOR';
        $scope.ROLE_EDITOR = 'ROLE_EDITOR';
        $scope.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        $scope.ADMINISTRATION_AUTHORITIES = [$scope.USER_EDITOR, $scope.ROLE_EDITOR,
            $scope.TEMPLATE_EDITOR].join(',');
        $scope.myBookmarks = {};
        $scope.allProjects = {};
        $scope.$on('project-created', function (event, data) {
            Project.query(function (result) {
                $scope.projects = result;
                $scope.myBookmarks.projects = result;
            });
        });

        $scope.$on('notebook-created', function(event, data) {
            var project = null;
            Project.query(function (result) {
                $scope.projects = result;
                $scope.projects.some(function(projectItem, i) {
                    return projectItem.node.id == data.projectId ? ( (project = projectItem), true) : false;
                });

                if(project) {
                    Notebook.query({projectId: project.node.id}, function (notebookResult) {
                        project.notebooks = notebookResult;
                        $scope.myBookmarks.projects = $scope.projects;
                    });
                }
            });
        });

        $scope.$on('experiment-created', function(event, data) {
            var project = null, notebook = null;
            Project.query(function (result) {
                $scope.projects = result;
                $scope.projects.some(function (projectItem, i) {
                    return projectItem.node.id == data.projectId ? ( (project = projectItem), true) : false;
                });

                if(project) {
                    Notebook.query({projectId: project.node.id}, function (notebookResult) {
                        notebookResult.some(function (notebookItem, i){
                            return notebookItem.node.id == data.notebookId ? ( (notebook = notebookItem), true) : false;
                        });

                        project.notebooks = notebookResult;
                        $scope.myBookmarks.projects = $scope.projects;

                        if(notebook) {
                            Experiment.query({notebookId: notebook.node.id, projectId : project.node.id}, function (expResult) {
                                notebook.experiments = expResult;
                            });
                        }
                    });
                }
            });
        });

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
            $state.go('project', {id: project.node.id});
            if (!project.notebooks) {
                var agent = needAll ? AllNotebooks : Notebook;
                agent.query({projectId: project.node.id}, function (result) {
                    project.notebooks = result;
                });
            } else {
                project.notebooks = null;
            }
        };

        $scope.toggleExperiments = function (notebook, project, needAll) {
            $state.go('notebook', {id: notebook.node.id, projectId: project.node.id});
            if (!notebook.experiments) {
                var agent = needAll ? AllExperiments : Experiment;
                agent.query({notebookId: notebook.node.id, projectId: project.node.id}, function (result) {
                    notebook.experiments = result;
                });
            } else {
                notebook.experiments = null;
            }
        };

        $scope.onExperimentClick = function (experiment, notebook, project) {
            $state.go('experiment.detail', {
                experimentId: ExperimentBrowser.compactIds({
                    experimentId: experiment.node.id,
                    notebookId: notebook.node.id,
                    projectId: project.node.id
                })
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

        };
    });