var advancedSearch = require('./advanced-search.json');

var searchReagentsService = function() {
    return {
        getRestrictions: function() {
            return {
                searchQuery: '',
                advancedSearch: angular.copy(advancedSearch),
                structure: {
                    name: 'Reaction Scheme',
                    similarityCriteria: {
                        name: 'equal'
                    },
                    similarityValue: null,
                    image: null
                }
            };
        }
    };
};

module.exports = searchReagentsService;
