/* jshint unused: false */
function searchProjects(filter) {

    return db.getCollection('project').aggregate([
        {
            $project: {
                'description': 1,
                'name': 1,
                'keywords': 1,
                'references': 1,
                'author': 1,
                'kind': {'$substr': ['$_class', 30, -1]}
            }
        },
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}