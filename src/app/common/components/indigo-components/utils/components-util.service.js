/* @ngInject */
function componentsUtil(principalService, typeOfComponents) {
    return {
        initComponents: initComponents,
        getComponentsFromTemplateContent: getComponentsFromTemplateContent
    };

    function initComponents(components, componentTemplates) {
        var templates = _.keyBy(getComponentsFromTemplateContent(componentTemplates), 'field');

        initExperimentDescription(components, templates[typeOfComponents.experimentDescription.field]);
        initConceptDetails(components, templates[typeOfComponents.conceptDetails.field]);
        initPreferredCompoundSummary(components, templates[typeOfComponents.preferredCompoundSummary.field]);
        initPreferredCompoundDetails(components, templates[typeOfComponents.preferredCompoundDetails.field]);
        initProductBatchDetails(components, templates[typeOfComponents.productBatchDetails.field]);
        initProductBatchSummary(components, _.find(templates, {isBatch: true}));
        initStoichTable(components, templates[typeOfComponents.stoichTable.field]);
        initReactionDetails(components, templates[typeOfComponents.reactionDetails.field]);
        initReaction(components, templates[typeOfComponents.reaction.field]);
    }

    function getComponentsFromTemplateContent(componentTemplates) {
        return _.chain(componentTemplates)
            .map('components')
            .flatten()
            .value();
    }

    function initExperimentDescription(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {
                experimentDescription: {description: ''}
            });
        }
    }

    function initConceptDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    conceptDetails: {experimentCreator: principalService.getIdentity().id}
                }
            );
        }
    }

    function initPreferredCompoundSummary(components, isExist) {
        if (!isExist) {
            return;
        }
        // migration, should be removed after dropped the database
        if (!_.isEmpty(components.preferredCompoundSummary && components.preferredCompoundSummary.compounds) &&
            _.isEmpty(components.productBatchSummary && components.productBatchSummary.batches)) {
            _.set(components, 'productBatchSummary.batches', components.preferredCompoundSummary.compounds);

            delete components.preferredCompoundSummary;
        }
    }

    function initPreferredCompoundDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {preferredCompoundDetails: null});
        }
    }

    function initProductBatchDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchDetails: null});
        }
    }

    function initProductBatchSummary(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {productBatchSummary: {batches: []}});
        }
    }

    function initStoichTable(components, isExist) {
        if (isExist) {
            _.defaultsDeep(components, {stoichTable: {products: [], reactants: []}});
        }
    }

    function initReactionDetails(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reactionDetails: {
                        batchOwner: [principalService.getIdentity().id],
                        coAuthors: [],
                        experimentCreator: principalService.getIdentity().id
                    }
                });
        }
    }

    function initReaction(components, isExist) {
        if (isExist) {
            _.defaultsDeep(
                components, {
                    reaction: {
                        molfile: null,
                        image: null
                    }
                });
        }
    }
}

module.exports = componentsUtil;
