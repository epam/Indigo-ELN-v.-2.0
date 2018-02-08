/* @ngInject */
function entityTreeService(allProjectsService, projectService, allNotebooksService, notebookService,
                           allExperimentsService, experimentService, $q) {
    var allProjectsList = [];
    var projectsList = [];
    var allNotebooksList = [];
    var notebooksList = [];
    var allExperimentsList = [];
    var experimentsList = [];

    return {
        updateExperiment: updateExperiment,
        addExperiment: addExperiment,
        addNotebook: addNotebook,
        updateNotebook: updateNotebook,
        updateProject: updateProject,
        addProject: addProject,
        getProjects: getProjects,
        getNotebooks: getNotebooks,
        getExperiments: getExperiments,
        updateStatus: updateStatus,
        getFullIdFromParams: getFullIdFromParams,
        getProjectById: getProjectById,
        getNotebookByFullId: getNotebookByFullId,
        clearAll: clearAll
    };

    function clearAll() {
        allProjectsList.length = 0;
        projectsList.length = 0;
        allNotebooksList.length = 0;
        notebooksList.length = 0;
        allExperimentsList.length = 0;
        experimentsList.length = 0;
    }

    function updateStatus(fullId, status) {
        var experiment = _.find(allExperimentsList, {fullId: fullId});
        if (experiment) {
            experiment.original.status = status;
        }

        experiment = _.find(experimentsList, {fullId: fullId});
        if (experiment) {
            experiment.original.status = status;
        }
    }

    function updateExperiment(experiment) {
        var experimentNode = _.find(allExperimentsList, {fullId: experiment.fullId});
        if (experimentNode) {
            updateNode(experimentNode, experiment);
        }

        experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        if (experimentNode) {
            updateNode(experimentNode, experiment);
        }
    }

    function addExperiment(experiment) {
        var path = _.split(experiment.fullId, '-');
        var parentId = path[0] + '-' + path[1];

        var experimentNode = _.find(allExperimentsList, {fullId: experiment.fullId});
        var parentNode = _.find(allNotebooksList, {fullId: parentId});
        if (!experimentNode && parentNode && parentNode.hasLoadedChildren) {
            experimentNode = buildNode(experiment, parentNode);
            allExperimentsList.push(experimentNode);
            parentNode.children.push(experimentNode);
        }

        experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        parentNode = _.find(notebooksList, {fullId: parentId});
        if (!experimentNode && parentNode && parentNode.hasLoadedChildren) {
            experimentNode = buildNode(experiment, parentNode);
            experimentsList.push(experimentNode);
            parentNode.children.push(experimentNode);
        }
    }

    function updateProject(project) {
        var projectNode = _.find(allProjectsList, {id: project.id});
        if (projectNode) {
            updateNode(projectNode, project);
        }

        projectNode = _.find(projectsList, {id: project.id});
        if (projectNode) {
            updateNode(projectNode, project);
        }
    }

    function addProject(project) {
        var builtNode = buildNode(project, null);
        allProjectsList.push(builtNode);

        builtNode = buildNode(project, null);
        projectsList.push(builtNode);
    }

    function addNotebook(notebook, projectId) {
        var projectNode = _.find(allProjectsList, {id: projectId});
        var notebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            allNotebooksList.push(notebookNode);
        }

        projectNode = _.find(projectsList, {id: projectId});
        notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            notebooksList.push(notebookNode);
        }
    }

    function updateNotebook(notebook) {
        var notebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (notebookNode) {
            updateNode(notebookNode, notebook);
        }

        notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (notebookNode) {
            updateNode(notebookNode, notebook);
        }
    }

    function getProjects(isAll) {
        var list = isAll ? allProjectsList : projectsList;
        var service = isAll ? allProjectsService : projectService;

        if (!list.length) {
            return service.query()
                .$promise
                .then(function(projects) {
                    _.forEach(projects, function(project) {
                        var node = buildNode(project, null);
                        list.push(node);
                    });

                    return list;
                });
        }

        return $q.resolve(list);
    }

    function getNotebooks(projectId, isAll) {
        var projList = isAll ? allProjectsList : projectsList;
        var noteList = isAll ? allNotebooksList : notebooksList;
        var service = isAll ? allNotebooksService : notebookService;

        var project = _.find(projList, {id: projectId});

        if (!project) {
            return $q.resolve([]);
        }
        if (!project.hasLoadedChildren) {
            return service.query({projectId: projectId})
                .$promise
                .then(function(notebooks) {
                    _.forEach(notebooks, function(notebook) {
                        var node = buildNode(notebook, project);
                        project.children.push(node);
                        noteList.push(node);
                    });
                    project.hasLoadedChildren = true;

                    return project.children;
                });
        }

        return $q.resolve(project.children);
    }

    function getExperiments(projectId, notebookId, isAll) {
        var noteList = isAll ? allNotebooksList : notebooksList;
        var expList = isAll ? allExperimentsList : experimentsList;
        var service = isAll ? allExperimentsService : experimentService;
        var fullId = projectId + '-' + notebookId;

        var notebook = _.find(noteList, {fullId: fullId});

        if (!notebook) {
            return $q.resolve([]);
        }

        if (!notebook.hasLoadedChildren) {
            return service.query({
                projectId: projectId,
                notebookId: notebookId
            })
                .$promise
                .then(function(experiments) {
                    _.forEach(experiments, function(experiment) {
                        var node = buildNode(experiment, notebook);
                        notebook.children.push(node);
                        expList.push(node);
                    });
                    notebook.hasLoadedChildren = true;

                    return notebook.children;
                });
        }

        return $q.resolve(notebook.children);
    }

    function buildNode(node, parent) {
        var newNode = {
            id: node.id,
            fullId: node.fullId,
            name: node.name,
            params: _.concat([], ((parent && parent.params) || []), node.id),
            parent: parent,
            children: [],
            isActive: false,
            isCollapsed: true,
            original: node,
            accessList: node.accessList,
            hasLoadedChildren: false
        };

        return newNode;
    }

    function updateNode(targetNode, entity) {
        targetNode.accessList = entity.accessList;
        targetNode.original = entity;
        targetNode.name = entity.fullName || entity.name;
    }

    function getFullIdFromParams(toParams) {
        return _.compact([toParams.projectId, toParams.notebookId, toParams.experimentId])
            .join('-')
            .toString();
    }

    function getProjectById(projectId) {
        var node = _.find(allProjectsList, {id: projectId});
        if (node) {
            return node.original;
        }

        return null;
    }

    function getNotebookByFullId(fullId) {
        var node = _.find(allNotebooksList, {fullId: fullId});
        if (node) {
            return node.original;
        }

        return null;
    }
}

module.exports = entityTreeService;
