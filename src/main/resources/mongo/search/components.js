/* jshint unused: false */
function searchComponents(filter) {

    return db.getCollection('component').aggregate([
        {$match: {"experiment":{$exists: true}}},
        {
            $project: {
                'experiment': 1,
                'name': 1,
                'content': 1,
                'batch': '$content.batches',
                'reactant': '$content.reactants',
                'product': '$content.products'
            }
        },
        {$unwind:{path:'$batch',preserveNullAndEmptyArrays:true}},
        {$unwind:{path:'$reactant',preserveNullAndEmptyArrays:true}},
        {$unwind:{path:'$product',preserveNullAndEmptyArrays:true}},
        {
            $project:{
                'experiment': 1,
                'name': 1,
                'content': 1,
                'batch': 1,
                'reactant': 1,
                'product': 1,
                'purity': '$batch.purity.data.value'
            }
        },
        {$unwind:{path:'$purity',preserveNullAndEmptyArrays:true}},
        {
            $group:{
                _id: "$experiment",
                'componentName': {$addToSet:'$name'},
                'therapeuticArea': {$addToSet:'$content.therapeuticArea.name'},
                'projectCode': {$addToSet:'$content.codeAndName.name'},
                'batchYield': {$addToSet:'$batch.yield'},
                'purity': {$addToSet:'$purity'},
                'name': {$addToSet:'$content.title'},
                'description': {$addToSet:'$content.description'},
                'compoundId': {
                    $addToSet:{
                        $cond: {
                            if: {
                                $eq: ['$name', 'productBatchSummary']
                            },
                            then: '$batch.compoundId',
                            else: '$reactant.compoundId'
                        }
                    }
                },
                'references': {$addToSet:'$content.literature'},
                'keywords': {$addToSet:'$content.keywords'},
                'chemicalName': {$addToSet:'$product.chemicalName'},
                'batchStructureId': {$addToSet:'$batch.structure.structureId'},
                'reactionStructureId': {$addToSet:'$content.structureId'}
            }
        },
        {$match: filter}
    ]).map(function (obj) {
        return obj._id;
    });
}