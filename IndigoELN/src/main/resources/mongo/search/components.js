/* jshint unused: false */
function searchComponents(filter) {

    return db.getCollection('component').aggregate([
        {$match: {'experiment':{$exists: true}}},
        {
            $group:{
                _id: '$experiment',
                'therapeuticArea': {$max:'$content.therapeuticArea.name'},
                'projectCode': {$max:'$content.codeAndName.name'},
                'batchYield': {$max:'$content.batches.yield'},
                'purity': {$max:'$content.batches.purity.data'},
                'name': {$max:'$content.title'},
                'description': {$max:'$content.description'},
                'compoundId': {
                    $max:{
                        $cond: {
                            if: {
                                $eq: ['$name', 'productBatchSummary']
                            },
                            then: '$content.batches.compoundId',
                            else: '$content.reactants.compoundId'
                        }
                    }
                },
                'references': {$max:'$content.literature'},
                'keywords': {$max:'$content.keywords'},
                'chemicalName': {$max:'$content.products.chemicalName'},
                'batchStructureId': {$max:'$content.batches.structure.structureId'},
                'reactionStructureId': {$max:'$content.structureId'}
            }
        },
        {$unwind: {path:'$purity',preserveNullAndEmptyArrays:true}},
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}