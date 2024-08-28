/* jshint unused: false */
function searchNotebooks(filter) {

    return db.getCollection('notebook').aggregate([
        {
            $project: {
                'description': 1,
                'name': 1,
                'author': 1,
                'kind': {'$substr': ['$_class', 30, -1]}
            }
        },
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}