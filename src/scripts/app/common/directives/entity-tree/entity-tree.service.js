/* @ngInject */
function entityTreeService(allProjects, $injector, allNotebooks, allExperiments, $q) {
    var allProjectsList = {};
    var allProjectsMap = {};
    var allNodesByFullId = {};
    var projectsList = {};
    var projectsMap = {};
    var allNotebooksMap = {};
    var notebooksMap = {};
    var allExperimentsMap = {};
    var experimentsMap = {};

    return {
        updateExperiment: updateExperiment,
        addNotebook: addNotebook,
        updateNotebook: updateNotebook,
        updateProject: updateProject,
        addProject: addProject,
        getProjects: getProjects,
        getNotebooks: getNotebooks,
        getExperiments: getExperiments,
        updateStatus: updateStatus,
        allNodesByFullId: allNodesByFullId,
        getFullIdFromParams: getFullIdFromParams,
        clearAll: clearAll
    };

    function clearAll() {
        allProjectsList = {};
        allProjectsMap = {};
        allNodesByFullId = {};
        projectsList = {};
        projectsMap = {};
        allNotebooksMap = {};
        notebooksMap = {};
        allExperimentsMap = {};
        experimentsMap = {};
    }

    function updateStatus(fullId, status) {
        var node = allNodesByFullId[fullId];
        if (node) {
            node.original.status = status;
        }
    }

    function updateExperiment(experiment) {
        var nodeExperiment = allNodesByFullId[experiment.fullId];
        var path = _.split(experiment.fullId, '-');
        var parent = allNodesByFullId[path[0] + '-' + path[1]];

        if (!nodeExperiment && !parent) {
            return;
        }

        var builtNode = buildNode(experiment, parent);

        if (nodeExperiment) {
            mergeNode(allNodesByFullId[builtNode.original.fullId], builtNode);
        } else {
            addEntity(builtNode, parent, allExperimentsMap, experimentsMap);
        }
    }

    function updateProject(project) {
        updateEntity(buildNode(project, undefined), project.id, allProjectsMap, projectsMap);
    }

    function addNotebook(notebook) {
        var project = allProjectsList[notebook.parentId];
        if (project) {
            var builtNode = buildNode(notebook, project);
            addEntity(builtNode, notebook.parentId, allNotebooksMap, notebooksMap);
        }
    }

    function updateNotebook(notebook) {
        var project = allNodesByFullId[notebook.parentId];
        var builtNode = buildNode(notebook, project);
        mergeNode(allNodesByFullId[builtNode.original.fullId], builtNode);
    }

    function addProject(project) {
        var builtNode = buildNode(project, undefined);
        addEntity(builtNode, builtNode.id, allProjectsMap, projectsMap, allProjectsList, projectsList);
    }

    function updateEntity(node, id, allMap, map) {
        mergeNode(allMap[id], node);
        mergeNode(map[id], node);
    }

    function addEntity(node, id, allMap, map, allList, list) {
        allMap[id] = node;
        map[id] = node;

        if (allList && list) {
            allList.push(node);
            list.push(node);
        }
    }

    function getProjects(isAll) {
        var list = isAll ? allProjectsList : projectsList;
        var map = isAll ? allProjectsMap : projectsMap;

        if (_.isEmpty(list)) {
            return (isAll ? allProjects : $injector.get('projectService')).query()
                .$promise
                .then(function(projects) {
                    list = updateTree(projects, map);

                    if (isAll) {
                        allProjectsList = list;
                    } else {
                        projectsList = list;
                    }

                    return list;
                });
        }

        return $q.resolve(list);
    }

    function getNotebooks(projectId, isAll) {
        var pMap;
        var nMap;
        var service;

        if (isAll) {
            pMap = allProjectsMap;
            nMap = allNotebooksMap;
            service = allNotebooks;
        } else {
            pMap = projectsMap;
            nMap = notebooksMap;
            service = $injector.get('notebookService');
        }

        if (_.isEmpty(nMap[projectId])) {
            return getEntities(
                service,
                {projectId: projectId},
                pMap[projectId]
            ).then(function(notebooks) {
                nMap[projectId] = notebooks;

                return notebooks;
            });
        }

        return $q.resolve(nMap[projectId]);
    }

    function getExperiments(projectId, notebookId, isAll) {
        var nMap = isAll ? allNotebooksMap : notebooksMap;
        var eMap = isAll ? allExperimentsMap : experimentsMap;
        var path = projectId + '_' + notebookId;
        if (_.isEmpty(eMap[path])) {
            return getEntities(
                isAll ? allExperiments : $injector.get('experimentService'),
                {
                    projectId: projectId,
                    notebookId: notebookId
                },
                _.find(nMap[projectId], {id: notebookId})
            ).then(function(experiments) {
                eMap[path] = experiments;

                return experiments;
            });
        }

        return $q.resolve(eMap[path]);
    }

    function getEntities(service, query, current) {
        return service.query(query)
            .$promise
            .then(function(notebooks) {
                return _.map(notebooks, function(node) {
                    return buildNode(node, current);
                });
            });
    }

    function updateTree(model, collection) {
        if (!model) {
            return [];
        }

        return _.map(model, function(node) {
            if (collection[node.id]) {
                collection[node.id].name = node.name;
            } else {
                collection[node.id] = buildNode(node, undefined);
            }

            return collection[node.id];
        });
    }

    function buildNode(node, parent) {
        var builtNode = {
            id: node.id,
            name: node.name,
            params: _.concat([], ((parent && parent.params) || []), node.id),
            parent: parent,
            children: undefined,
            isActive: false,
            isCollapsed: true,
            original: node
        };

        if (allNodesByFullId[node.fullId]) {
            mergeNode(allNodesByFullId[node.fullId], builtNode);
        } else {
            allNodesByFullId[node.fullId] = builtNode;
        }

        return allNodesByFullId[node.fullId];
    }

    function mergeNode(targetNode, fromNode) {
        targetNode.accessList = fromNode.accessList;
        targetNode.original = fromNode.original;
        targetNode.name = fromNode.original.fullName || fromNode.name;
    }

    function getFullIdFromParams(toParams) {
        return _.compact([toParams.projectId, toParams.notebookId, toParams.experimentId])
            .join('-')
            .toString();
    }
}

module.exports = entityTreeService;
