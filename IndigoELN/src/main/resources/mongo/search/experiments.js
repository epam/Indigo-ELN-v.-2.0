/* jshint unused: false */
function searchExperiments(filter) {

    var createProjectCond = function (path) {
        return {
            $cond: {
                if: {
                    $eq: [path, undefined]
                },
                then: [null],
                else: {
                    $cond: {
                        if: {
                            $eq: [path, []]
                        },
                        then: [null],
                        else: path
                    }
                }
            }
        };
    };

    return db.getCollection('experiment').aggregate([
        {
            $match: {
                'lastVersion': true
            }
        },
        {
            $project: {
                'components': createProjectCond('$components'),
                'status': 1,
                'author': 1,
                'kind': {'$substr': ['$_class', 30, -1]}
            }
        },
        {$unwind: '$components'},
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}