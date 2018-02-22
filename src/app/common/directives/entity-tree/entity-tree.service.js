/* @ngInject */
function entityTreeService($q, entityTreeFactory) {
    var allProjectsList = [];
    var projectsList = [];
    var allNotebooksList = [];
    var notebooksList = [];
    var allExperimentsList = [];
    var experimentsList = [];

    return {
        updateExperiment: updateExperiment,
        updateExperimentByEntity: updateExperimentByEntity,
        addExperimentByEntity: addExperimentByEntity,
        addNotebookByEntity: addNotebookByEntity,
        updateNotebook: updateNotebook,
        updateNotebookByEntity: updateNotebookByEntity,
        refreshNotebooks: refreshNotebooks,
        updateProject: updateProject,
        updateProjectByEntity: updateProjectByEntity,
        addProject: addProject,
        getProjects: getProjects,
        refreshProjects: refreshProjects,
        getNotebooks: getNotebooks,
        getExperiments: getExperiments,
        refreshExperiments: refreshExperiments,
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

    function updateExperimentByEntity(experiment) {
        var allExperimentNode = _.find(allExperimentsList, {fullId: experiment.fullId});
        if (allExperimentNode && allExperimentNode.version < experiment.version) {
            updateExperimentNodeByEntity(allExperimentNode, experiment);
        }

        var experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        if (experimentNode && experimentNode.version < experiment.version) {
            updateExperimentNodeByEntity(experimentNode, experiment);
        }
    }

    function updateExperiment(fullId, newVersion) {
        var allExperimentNode = _.find(allExperimentsList, {fullId: fullId});
        var experimentNode = _.find(experimentsList, {fullId: fullId});
        if ((experimentNode && experimentNode.version < newVersion)
            || (allExperimentNode && allExperimentNode.version < newVersion)) {
            var path = fullId.split('-');
            entityTreeFactory.getExperiment(path[0], path[1], path[2]).then(function(experiment) {
                if (allExperimentNode) {
                    updateExperimentNode(allExperimentNode, experiment);
                    allExperimentNode.parent.children.sort(sortByName);
                }
                if (experimentNode) {
                    updateExperimentNode(experimentNode, experiment);
                    experimentNode.parent.children.sort(sortByName);
                }
            });
        }
    }

    function addExperimentByEntity(experiment) {
        var path = _.split(experiment.fullId, '-');
        var parentId = path[0] + '-' + path[1];

        var experimentNode = _.find(allExperimentsList, {fullId: experiment.fullId});
        var parentNode = _.find(allNotebooksList, {fullId: parentId});
        if (!experimentNode && parentNode && parentNode.hasLoadedChildren) {
            experimentNode = buildExperimentNodeFromEntity(experiment, parentNode);
            allExperimentsList.push(experimentNode);
            parentNode.children.push(experimentNode);
            parentNode.children.sort(sortByName);
        }

        experimentNode = _.find(experimentsList, {fullId: experiment.fullId});
        parentNode = _.find(notebooksList, {fullId: parentId});
        if (!experimentNode && parentNode && parentNode.hasLoadedChildren) {
            experimentNode = buildExperimentNodeFromEntity(experiment, parentNode);
            experimentsList.push(experimentNode);
            parentNode.children.push(experimentNode);
            parentNode.children.sort(sortByName);
        }
    }

    function updateProject(projectId, newVersion) {
        var allProjectNode = _.find(allProjectsList, {id: projectId});
        var projectNode = _.find(projectsList, {id: projectId});
        if ((allProjectNode && allProjectNode.version < newVersion)
            || (projectNode && projectNode.version < newVersion)) {
            entityTreeFactory.getProject(projectId).then(function(project) {
                if (allProjectNode) {
                    allProjectNode.name = project.name;
                    allProjectNode.version = project.version;
                    allProjectsList.sort(sortByName);
                }
                if (projectNode) {
                    projectNode.name = project.name;
                    projectNode.version = project.version;
                    projectsList.sort(sortByName);
                }
            });
        }
    }

    function updateProjectByEntity(project) {
        var allProjectNode = _.find(allProjectsList, {id: project.id});
        if (allProjectNode && allProjectNode.version < project.version) {
            allProjectNode.name = project.name;
            allProjectNode.version = project.version;
            allProjectsList.sort(sortByName);
        }

        var projectNode = _.find(projectsList, {id: project.id});
        if (projectNode && projectNode.version < project.version) {
            projectNode.name = project.name;
            projectNode.version = project.version;
            projectsList.sort(sortByName);
        }
    }

    function addProject(project) {
        if (!project.fullId) {
            return;
        }
        var builtNode = buildProjectNode(project, null);
        allProjectsList.push(builtNode);
        allProjectsList.sort(sortByName);

        builtNode = buildProjectNode(project, null);
        projectsList.push(builtNode);
        projectsList.sort(sortByName);
    }

    function addNotebookByEntity(notebook) {
        var projectNode = _.find(allProjectsList, {id: notebook.parentId});
        var notebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNotebookNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            projectNode.children.sort(sortByName);
            allNotebooksList.push(notebookNode);
        }

        projectNode = _.find(projectsList, {id: notebook.parentId});
        notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (!notebookNode && projectNode && projectNode.hasLoadedChildren) {
            notebookNode = buildNotebookNode(notebook, projectNode);
            projectNode.children.push(notebookNode);
            projectNode.children.sort(sortByName);
            notebooksList.push(notebookNode);
        }
    }

    function updateNotebook(fullId, newVersion) {
        var path = fullId.split('-');
        var allNotebookNode = _.find(allNotebooksList, {fullId: fullId});
        var notebookNode = _.find(notebooksList, {fullId: fullId});
        if ((allNotebookNode && allNotebookNode.version < newVersion)
            || (notebookNode && notebookNode.version < newVersion)) {
            entityTreeFactory.getNotebook(path[0], path[1]).then(function(notebook) {
                if (allNotebookNode) {
                    allNotebookNode.name = notebook.name;
                    allNotebookNode.version = notebook.version;
                    allNotebookNode.parent.children.sort(sortByName);
                }
                if (notebookNode) {
                    notebookNode.name = notebook.name;
                    notebookNode.version = notebook.version;
                    notebookNode.parent.children.sort(sortByName);
                }
            });
        }
    }

    function updateNotebookByEntity(notebook) {
        var allNotebookNode = _.find(allNotebooksList, {fullId: notebook.fullId});
        if (allNotebookNode && allNotebookNode.version < notebook.version) {
            allNotebookNode.name = notebook.name;
            allNotebookNode.version = notebook.version;
            allNotebookNode.parent.children.sort(sortByName);
        }

        var notebookNode = _.find(notebooksList, {fullId: notebook.fullId});
        if (notebookNode && notebookNode.version < notebook.version) {
            notebookNode.name = notebook.name;
            notebookNode.version = notebook.version;
            notebookNode.parent.children.sort(sortByName);
        }
    }

    function getProjects(isAll) {
        var list = isAll ? allProjectsList : projectsList;

        if (!list.length) {
            return entityTreeFactory.getProjects(isAll)
                .then(function(projects) {
                    _.forEach(projects, function(project) {
                        var node = buildProjectNode(project, null);
                        list.push(node);
                    });
                    list.sort(sortByName);

                    return list;
                });
        }

        return $q.resolve(list);
    }

    function refreshProjects(isAll) {
        var list = isAll ? allProjectsList : projectsList;

        entityTreeFactory.getProjects(isAll)
            .then(function(projects) {
                _.forEach(projects, function(project) {
                    if (!_.find(list, {id: project.id})) {
                        var node = buildProjectNode(project, null);
                        list.push(node);
                    }
                });
                _.remove(list, function(project) {
                    return !_.some(projects, {id: project.id});
                });
                list.sort(sortByName);
            });
    }

    function getNotebooks(projectId, isAll) {
        var projList = isAll ? allProjectsList : projectsList;
        var noteList = isAll ? allNotebooksList : notebooksList;

        var project = _.find(projList, {id: projectId});

        if (!project) {
            return $q.resolve([]);
        }
        if (!project.hasLoadedChildren) {
            return entityTreeFactory.getNotebooks(projectId, isAll)
                .then(function(notebooks) {
                    _.forEach(notebooks, function(notebook) {
                        var node = buildNotebookNode(notebook, project);
                        project.children.push(node);
                        noteList.push(node);
                    });
                    project.children.sort(sortByName);
                    project.hasLoadedChildren = true;
                });
        }

        return $q.resolve(project.children);
    }

    function refreshNotebooks(projectId, isAll) {
        var projList = isAll ? allProjectsList : projectsList;
        var noteList = isAll ? allNotebooksList : notebooksList;

        var project = _.find(projList, {id: projectId});

        if (!project) {
            return;
        }
        if (!project.hasLoadedChildren) {
            return;
        }
        entityTreeFactory.getNotebooks(projectId, isAll)
            .then(function(notebooks) {
                _.forEach(notebooks, function(notebook) {
                    if (!_.find(project.children, {fullId: notebook.fullId})) {
                        var node = buildNotebookNode(notebook, project);
                        project.children.push(node);
                        noteList.push(node);
                    }
                });
                _.remove(project.children, function(notebook) {
                    return !_.some(notebooks, {fullId: notebook.fullId});
                });
                _.remove(noteList, function(notebook) {
                    return !_.some(notebooks, {fullId: notebook.fullId});
                });
                project.children.sort(sortByName);
            });
    }

    function getExperiments(projectId, notebookId, isAll) {
        var noteList = isAll ? allNotebooksList : notebooksList;
        var expList = isAll ? allExperimentsList : experimentsList;
        var fullId = projectId + '-' + notebookId;

        var notebook = _.find(noteList, {fullId: fullId});

        if (!notebook) {
            return $q.resolve([]);
        }

        if (!notebook.hasLoadedChildren) {
            return entityTreeFactory.getExperiments(projectId, notebookId, isAll)
                .then(function(experiments) {
                    _.forEach(experiments, function(experiment) {
                        var node = buildExperimentNode(experiment, notebook);
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

    function refreshExperiments(projectId, notebookId, isAll) {
        var noteList = isAll ? allNotebooksList : notebooksList;
        var expList = isAll ? allExperimentsList : experimentsList;
        var fullId = projectId + '-' + notebookId;

        var notebook = _.find(noteList, {fullId: fullId});

        if (!notebook) {
            return;
        }

        if (!notebook.hasLoadedChildren) {
            return;
        }
        entityTreeFactory.getExperiments(projectId, notebookId, isAll)
            .then(function(experiments) {
                _.forEach(experiments, function(experiment) {
                    if (!_.find(notebook.children, {fullId: experiment.fullId})) {
                        var node = buildExperimentNode(experiment, notebook);
                        notebook.children.push(node);
                        expList.push(node);
                    }
                });
                _.remove(notebook.children, function(experiment) {
                    return !_.some(experiments, {fullId: experiment.fullId});
                });
                _.remove(expList, function(experiment) {
                    return !_.some(experiments, {fullId: experiment.fullId});
                });
                notebook.children.sort(sortByName);
            });
    }

    function buildExperimentNode(entity, parent) {
        var newNode = {
            id: entity.id,
            fullId: entity.fullId,
            name: entity.fullName || entity.name,
            authorFullName: entity.authorFullName,
            creationDate: entity.creationDate,
            status: entity.status,
            reactionImage: entity.reactionImage,
            title: entity.title,
            therapeuticArea: entity.therapeuticArea,
            version: entity.version,
            params: [parent.parent.id, parent.id, entity.id],
            parent: parent,
            isActive: false,
            isCollapsed: true
        };

        return newNode;
    }

    function buildExperimentNodeFromEntity(entity, parent) {
        var node = buildExperimentNode(entity, parent);
        updateExperimentNodeByEntity(node, entity);
        node.authorFullName = _.get(entity, 'author.fullName', null);

        return node;
    }

    function updateExperimentNodeByEntity(source, entity) {
        source.status = entity.status;
        source.version = entity.version;
        source.reactionImage = _.get(entity, 'components.reaction.image', null);
        source.title = _.get(entity, 'components.reactionDetails.title', null);
        source.therapeuticArea = _.get(entity, 'components.reactionDetails.therapeuticArea.name', null);
    }

    function updateExperimentNode(source, newNode) {
        source.status = newNode.status;
        source.version = newNode.version;
        source.reactionImage = newNode.reactionImage;
        source.title = newNode.title;
        source.therapeuticArea = newNode.therapeuticArea;
    }

    function buildNotebookNode(entity, parent) {
        var newNode = {
            id: entity.id,
            fullId: entity.fullId,
            name: entity.name,
            version: entity.version,
            params: [parent.id, entity.id],
            parent: parent,
            children: [],
            isActive: false,
            isCollapsed: true,
            hasLoadedChildren: false
        };

        return newNode;
    }

    function buildProjectNode(entity) {
        var newNode = {
            id: entity.id,
            fullId: entity.fullId,
            name: entity.name,
            version: entity.version,
            params: [entity.id],
            children: [],
            isActive: false,
            isCollapsed: true,
            hasLoadedChildren: false
        };

        return newNode;
    }

    function getProjectById(projectId) {
        var node = _.find(projectsList, {id: projectId});
        if (node) {
            return node;
        }
        node = _.find(allProjectsList, {id: projectId});

        return node;
    }

    function getNotebookByFullId(fullId) {
        var node = _.find(notebooksList, {fullId: fullId});
        if (node) {
            return node;
        }
        node = _.find(allNotebooksList, {fullId: fullId});

        return node;
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
