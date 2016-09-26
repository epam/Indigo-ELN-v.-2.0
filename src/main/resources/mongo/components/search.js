/* jshint unused: false */
function searchComponents(filter) {

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


    return db.getCollection('component').aggregate([
        {
            $project: {
                'name': 1,
                'content': 1,
                'batch': createProjectCond('$content.batches'),
                'reactant': createProjectCond('$content.reactants'),
                'product': createProjectCond('$content.products')
            }
        },
        {$unwind: '$batch'},
        {
            $project: {
                'name': 1,
                'content': 1,
                'batch': 1,
                'reactant': 1,
                'product': 1,
                'batchPurity': createProjectCond('$batch.purity.data')
            }
        },
        {$unwind: '$batchPurity'},
        {$unwind: '$reactant'},
        {$unwind: '$product'},
        {
            $project: {
                'componentName': '$name',
                'therapeuticArea': '$content.therapeuticArea.name',
                'projectCode': '$content.codeAndName.name',
                'batchYield': '$batch.yield',
                'purity': '$batchPurity.value',
                'name': '$content.title',
                'description': '$content.description',
                'compoundId': {
                    $cond: {
                        if: {
                            $eq: ['$name', 'productBatchSummary']
                        },
                        then: '$batch.compoundId',
                        else: '$reactant.compoundId'
                    }
                },
                'references': '$content.literature',
                'keywords': '$content.keywords',
                'chemicalName': '$product.chemicalName',
                'batchStructureId': '$batch.structure.structureId',
                'reactionStructureId': '$content.structureId'
            }
        },
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}