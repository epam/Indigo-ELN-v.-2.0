(function() {
    angular
        .module('indigoeln')
        .factory('entityTreeService', entityTreeService);

    /* @ngInject */
    function entityTreeService(AllProjects, Project, AllNotebooks, Notebook, AllExperiments, Experiment, $q) {
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
            getProjects: getProjects,
            getNotebooks: getNotebooks,
            getExperiments: getExperiments,
            allNodesByFullId: allNodesByFullId
        };

        function getProjects(isAll) {
            var list = isAll ? allProjectsList : projectsList;
            var map = isAll ? allProjectsMap : projectsMap;

            if (_.isEmpty(list)) {
                return (isAll ? AllProjects : Project).query()
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
                service = AllNotebooks;
            } else {
                pMap = projectsMap;
                nMap = notebooksMap;
                service = Notebook;
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
                    isAll ? AllExperiments : Experiment,
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
            return {
                id: node.id,
                name: node.name,
                params: _.concat([], ((parent && parent.params) || []), node.id),
                parent: parent,
                children: undefined,
                isActive: false,
                isCollapsed: true,
                original: node
            };
        }

        function mergeNode(targetNode, fromNode) {
            targetNode.accessList = fromNode.accessList;
            targetNode.original = fromNode.original;
            targetNode.name = fromNode.name;
        }
    }
})();
