run.$inject = ['$templateCache', 'typeOfComponents'];

function run($templateCache, typeOfComponents) {
    var defaultAttributes = ' model="vm.model"' +
        ' reactants="vm.reactants"' +
        ' reactants-trigger="vm.reactantsTrigger"' +
        ' experiment="vm.experiment"' +
        ' is-readonly="vm.isReadonly"' +
        ' on-changed="vm.onChangedComponent({componentId: component.id})"';

    var batchAttributes = defaultAttributes +
        ' batches="vm.batches"' +
        ' on-added-batch="vm.onAddedBatch(batch)"' +
        ' batches-trigger="vm.batchesTrigger"' +
        ' selected-batch="vm.selectedBatch"' +
        ' is-exist-stoich-table="::!!vm.model.stoichTable"' +
        ' selected-batch-trigger="vm.selectedBatchTrigger"' +
        ' on-select-batch="vm.onSelectBatch(batch)"' +
        ' on-remove-batches="vm.onRemoveBatches(batches)"' +
        ' batch-operation="vm.batchOperation"' +
        ' save-experiment-fn="vm.saveExperimentFn()"';

    var stoichTableAttributes = defaultAttributes +
        ' on-precursors-changed="vm.onPrecursorsChanged(precursors)"' +
        ' info-reactants="vm.model.reaction.infoReactants"' +
        ' info-products="vm.model.reaction.infoProducts"';

    _.forEach(typeOfComponents, function(component) {
        $templateCache.put(component.id, getTemplate(component));
    });

    function getTemplate(component) {
        var directiveName = 'indigo-' + component.id;

        return '<' + directiveName + getComponentAttributes(component.id) +
            'component-data="vm.model.' + component.field + '"></' + directiveName + '>';
    }

    function getComponentAttributes(id) {
        var component = _.find(typeOfComponents, {id: id});
        if (component.isBatch) {
            return batchAttributes;
        }
        if (id === 'stoich-table') {
            return stoichTableAttributes;
        }

        return defaultAttributes;
    }
}

module.exports = run;
