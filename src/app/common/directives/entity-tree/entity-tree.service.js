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
            experimentNode.parent.children.sort(sortByName);
        }

        experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        if (experimentNode) {
            updateNode(experimentNode, experiment);
            experimentNode.parent.children.sort(sortByName);
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
            parentNode.children.sort(sortByName);
        }

        experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        parentNode = _.find(notebooksList, {fullId: parentId});
        if (!experimentNode && parentNode && parentNode.hasLoadedChildren) {
            experimentNode = buildNode(experiment, parentNode);
            experimentsList.push(experimentNode);
            parentNode.children.push(experimentNode);
            parentNode.children.sort(sortByName);
        }
    }

    function updateProject(project) {
        var projectNode = _.find(allProjectsList, {id: project.id});
        if (projectNode) {
            updateNode(projectNode, project);
            allProjectsList.sort(sortByName);
        }

        projectNode = _.find(projectsList, {id: project.id});
        if (projectNode) {
            updateNode(projectNode, project);
            projectsList.sort(sortByName);
        }
    }

    function addProject(project) {
        var builtNode = buildNode(project, null);
        allProjectsList.push(builtNode);
        allProjectsList.sort(sortByName);

        builtNode = buildNode(project, null);
        projectsList.push(builtNode);
        projectsList.sort(sortByName);
    }

    function addNotebook(notebook, projectId) {
        var projectNode = _.find(allProjectsList, {id: projectId});
        var notebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            projectNode.children.sort(sortByName);
            allNotebooksList.push(notebookNode);
        }

        projectNode = _.find(projectsList, {id: projectId});
        notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            projectNode.children.sort(sortByName);
            notebooksList.push(notebookNode);
        }
    }

    function updateNotebook(notebook) {
        var notebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (notebookNode) {
            updateNode(notebookNode, notebook);
            notebookNode.parent.children.sort(sortByName);
        }

        notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (notebookNode) {
            updateNode(notebookNode, notebook);
            notebookNode.parent.children.sort(sortByName);
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
                    list.sort(sortByName);

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
                    project.children.sort(sortByName);
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
                    notebook.children.sort(sortByName);
                    notebook.hasLoadedChildren = true;

                    return notebook.children;
                });
        }

        return $q.resolve(notebook.children);
    }

    function buildNode(entity, parent) {
        var newNode = {
            id: entity.id,
            fullId: entity.fullId,
            name: entity.fullName || entity.name,
            params: _.concat([], ((parent && parent.params) || []), entity.id),
            parent: parent,
            children: [],
            isActive: false,
            isCollapsed: true,
            original: entity,
            accessList: entity.accessList,
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
        var node = _.find(projectsList, {id: projectId});
        if (node) {
            return node.original;
        }

        return null;
    }

    function getNotebookByFullId(fullId) {
        var node = _.find(notebooksList, {fullId: fullId});
        if (node) {
            return node.original;
        }

        return null;
    }

    function sortByName(a, b) {
        var aName = a.name ? a.name.toLowerCase() : '';
        var bName = b.name ? b.name.toLowerCase() : '';
        if (aName > bName) {
            return 1;
        }
        if (aName < bName) {
            return -1;
        }

        return 0;
    }
}

module.exports = entityTreeService;
