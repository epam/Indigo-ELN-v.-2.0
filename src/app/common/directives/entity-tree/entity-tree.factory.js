/* @ngInject */
function entityTreeFactory(apiUrl, $http) {
    return {
        getProjects: function(isAll) {
            var url = isAll ? 'tree/projects/all' : 'tree/projects';
            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getNotebooks: function(projectId, isAll) {
            var url = 'tree/projects/' + projectId + '/notebooks';
            if (isAll) {
                url += '/all';
            }
            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        },
        getExperiments: function(projectId, notebookId, isAll) {
            var url = 'tree/projects/' + projectId + '/notebooks/' + notebookId + '/experiments';
            if (isAll) {
                url += '/all';
            }
            return $http.get(apiUrl + url).then(function(response) {
                return response.data;
            });
        }
    };
}

module.exports = entityTreeFactory;
