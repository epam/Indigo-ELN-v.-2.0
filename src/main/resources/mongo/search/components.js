/* jshint unused: false */
function searchComponents(filter) {

    return db.getCollection('component').aggregate([
        {
            $group:{
                _id: "$experiment",
                'componentName': {$addToSet:'$name'},
                'therapeuticArea': {$addToSet:'$content.therapeuticArea.name'},
                'projectCode': {$addToSet:'$content.codeAndName.name'},
                'batchYield': {$addToSet:'$content.batches.yield'},
                'purity': {$addToSet:'$content.batches.purity.data.value'},
                'name': {$addToSet:'$content.title'},
                'description': {$addToSet:'$content.description'},
                'compoundId': {
                    $addToSet:{
                        $cond: {
                            if: {
                                $eq: ['$name', 'productBatchSummary']
                            },
                            then: '$content.batches.compoundId',
                            else: '$content.reactants.compoundId'
                        }
                    }
                },
                'references': {$addToSet:'$content.literature'},
                'keywords': {$addToSet:'$content.keywords'},
                'chemicalName': {$addToSet:'$content.products.chemicalName'},
                'batchStructureId': {$addToSet:'$content.batches.structure.structureId'},
                'reactionStructureId': {$addToSet:'$content.structureId'}
            }
        },
        {$unwind:'$batchYield'},
        {$unwind:'$purity'},
        {$unwind:'$compoundId'},
        {$unwind:'$batchStructureId'},
        {$unwind:'$batchStructureId'},
        {$match: filter}
    ], {allowDiskUse: true}
    ).map(function (obj) {
        return obj._id;
    });
}