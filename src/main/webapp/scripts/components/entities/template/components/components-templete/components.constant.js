(function() {
    angular
        .module('indigoeln')
        .constant('Components', {
            conceptDetails: {
                name: 'Concept Details',
                id: 'concept-details',
                desc: 'Allows user to specify Title, Therapeutic Area, Project Code, Co-authors, Designers, Keywords and Linked Experiments'
            },
            reactionDetails: {
                name: 'Reaction Details',
                id: 'reaction-details',
                desc: 'Allows user to specify Title, Therapeutic Area, Project Code, Co-authors, Literature References, Keywords, Link to Previous and Future Experiments and Link to any Experiment'
            },
            productBatchSummary: {
                name: 'Product Batch Summary',
                id: 'product-batch-summary',
                desc: 'Represents all batches in table format. Allows user to review, create and edit batch' +
                ' details:Total amount weight, Yield%, Stereoisomer code, Purity, Solubility, Hazards,ect. Batch Registration (if it is allowed) is also executed here',
                isBatch: true
            },
            productBatchDetails: {
                name: 'Product Batch Details',
                id: 'product-batch-details',
                desc: 'Provides details for the individual batch. Allows user to review, create and edit batch' +
                ' details:Total amount weight, Yield%, Stereoisomer code, Purity, Solubility, Hazards,ect. Batch Registration (if it is allowed) is also executed here',
                isBatch: true
            },
            reactionScheme: {
                name: 'Reaction Scheme',
                id: 'reaction-scheme',
                desc: 'Allows user to draw , import and export reaction schema'
            },
            stoichTable: {
                name: 'Stoich Table',
                id: 'stoich-table',
                desc: 'Allows user to specify Reactants, Reagents and Solvent using automatic reaction scheme analysis or manual search in database(s). Stoichiometry calculations of the Initial amounts and Theoretical amounts f the Intended Reaction Products are executed here'
            },
            batchStructure: {
                name: 'Batch Structure',
                id: 'batch-structure',
                desc: 'Allows user to draw , import and export batch structure',
                isBatch: true
            },
            experimentDescription: {
                name: 'Experiment Description',
                id: 'experiment-description',
                desc: 'Contains text editor with possibility to text formatting, insert pictures and table'
            },
            attachments: {
                name: 'Attachments',
                id: 'attachments',
                desc: 'Allows to manage attachment of any kind of file related to  this experiment'
            },
            preferredCompoundsSummary: {
                name: 'Preferred Compounds Summary',
                id: 'preferred-compounds-summary',
                desc: 'Allows user to review, create and edit compounds details: Stereoisomer code, Comments, ect.' +
                ' Virtual Compound Registration (if it is allowed) is also executed here.',
                isBatch: true
            },
            // TODO: fix wrong name, now we have differrent names into templateContent and components of experiment
            preferredCompoundSummary: {
                id: 'preferred-compounds-summary'
            },
            preferredCompoundDetails: {
                name: 'Preferred Compound  Details',
                id: 'preferred-compound-details',
                desc: 'Provides details for the individual compound. Allows user to review, create and edit batch' +
                ' details: Stereoisomer code, Comments, ect. Virtual Compound Registration (if it is allowed) is also executed here.',
                isBatch: true
            }
        });
})();
