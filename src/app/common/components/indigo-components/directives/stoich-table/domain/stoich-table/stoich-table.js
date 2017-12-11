var calculationUtil = require('../../calculation/calculation-util');

function stoichTable(config) {
    var stoichTable = config.table;
    var onCompoundIdChanged = config.onCompoundIdChanged;

    return {
        addRow: addRow,
        onFieldValueChanged: onFieldValueChanged,
        setStoichTable: setStoichTable,
        getStoichTable: getStoichTable
    };

    function addRow(newRow) {
        if (newRow.isSolventRow()) {
            newRow.prevRxnRole.name = 'SOLVENT';
            newRow.setReadonly(newRow.getResetFieldsForSolvent(), true);
        } else {
            isLimitingRowExist()
                ? newRow.updateMol(getLimitingRow().mol.value, onMolChanged)
                : newRow.limiting = true;
        }

        stoichTable.reactants.push(newRow);
    }

    function onFieldValueChanged(row, fieldId) {
        //TODO: refactor it
        switch (fieldId) {
            case 'molWeight':
                row.setEntered(fieldId);
                onMolWeightChanged(row);
                break;
            case 'weight':
                row.setEntered(fieldId);
                onWeightChanged(row);
                break;
            case 'mol':
                row.setEntered(fieldId);
                onMolChanged(row);
                break;
            case 'eq':
                row.setEntered(fieldId);
                onEqChanged(row);
                break;
            case 'rxnRole':
                onRxnRoleChanged(row);
                break;
            case 'volume':
                row.setEntered(fieldId);
                onVolumeChanged(row);
                break;
            case 'molarity':
                row.setEntered(fieldId);
                onMolarityChanged(row);
                break;
            case 'stoicPurity':
                row.setEntered(fieldId);
                onPurityChanged(row);
                break;
            case 'saltCode':
                onSaltChanged(row);
                break;
            case 'saltEq':
                onSaltChanged(row);
                break;
            case 'density':
                row.setEntered(fieldId);
                onDensityChanged(row);
                break;
            case 'compoundId':
                onCompoundIdChanged(row, row[fieldId]);
                break;
        }
    }

    function onMolWeightChanged(row) {
        row.molWeight.originalValue = row.molWeight.value;

        if (row.molWeight.value) {
            if (row.isLimiting() && row.weight.value) {
                var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
                updateMol(row, mol);
                return;
            }
            if (row.mol.value) {
                var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
                row.setComputedWeight(weight, onWeightChanged);
            }
        }
    }

    function onWeightChanged(row) {
        if (row.areValuesPresent(['weight', 'molWeight'])) {
            var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
            row.setComputedMol(mol);

            if (row.isLimiting()) {
                updateRows(row.mol.value);
            }
        }

        if (!row.isValuePresent('weight')) {
            row.resetFields(row.getResetFieldsForWeight());
        }

        updateDependencies(row);
    }

    function onMolChanged(row) {
        if (row.stoicPurity.entered) {
            var k = calculationUtil.divide(row.stoicPurity.value, row.stoicPurity.prevValue);
            var divider = calculationUtil.multiply(k, 100);
            row.stoicPurity.prevValue = row.stoicPurity.value;
            var weight = calculationUtil.computeWeightByPurity(divider, row.weight.value);
            row.setComputedWeight(weight);
        }

        if (row.areValuesPresent(['molWeight', 'mol']) && !row.stoicPurity.entered) {
            var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
            row.setComputedWeight(weight);
        }

        if (row.isLimiting()) {
            updateRows(row.mol.value);
        }

        if (!row.isValuePresent('mol')) {
            row.resetFields(row.getResetFieldsForMol());
        }

        updateDependencies(row);
    }

    function onEqChanged(row) {
        if (!row.eq.value) {
            row.eq.value = row.eq.prevValue;
            return;
        }

        var multiplier = calculationUtil.divide(row.eq.value, row.eq.prevValue);
        row.eq.prevValue = row.eq.value;

        if (row.isLimiting() && row.isValuePresent('mol')) {
            var molByEq = calculationUtil.computeMolByEq(row.mol.value, row.eq.value);
            updateRows(molByEq);
        }

        if (!row.isLimiting() && row.isValuePresent('weight')) {
            var weight = calculationUtil.multiply(row.weight.value, multiplier);
            row.setComputedWeight(weight);
        }

        if (!row.isLimiting() && row.isValuePresent('mol')) {
            var mol = calculationUtil.multiply(row.mol.value, multiplier);
            row.setComputedMol(mol);
        }
    }

    function onRxnRoleChanged(row) {
        var fieldsToReset = row.getResetFieldsForSolvent();

        if (row.isSolventRow()) {
            var isLimiting = row.isLimiting();
            var nextRow = getRowAfterLimiting();

            row.resetFields(fieldsToReset);
            row.setReadonly(fieldsToReset, true);

            if (isLimiting && nextRow) {
                row.limiting = false;
                nextRow.limiting = true;
            }
        } else if (!row.isSolventRow() && row.prevRxnRole.name === 'SOLVENT') {
            row.resetFields(['volume']);
            row.setReadonly(fieldsToReset, false);

            if (isLimitingRowExist()) {
                row.setComputedMol(getLimitingRow().mol.value, onMolChanged);
            }
        }

        row.prevRxnRole.name = row.rxnRole.name;
    }

    function onVolumeChanged(row) {
        if (row.areValuesPresent(['volume', 'molarity'])) {
            var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);
            return;
        }

        if (row.areValuesPresent(['volume', 'density'])) {
            var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);
            return;
        }

        if (row.isValuePresent('volume') && !row.isSolventRow()) {
            if (row.isLimiting()) {
                var nextRow = getRowAfterLimiting();
                if (nextRow) {
                    nextRow.limiting = true;
                }

                row.limiting = false;
            }

            row.resetFields(['weight', 'mol']);

            return;
        }

        if (!row.isValuePresent('volume') && row.areValuesPresent(['mol', 'molarity'])) {
            row.resetFields(['mol'], onMolChanged);
            return;
        }

        if (!row.isValuePresent('volume') && row.isValuePresent('density')) {
            row.resetFields(['weight'], onWeightChanged);
            return;
        }

        if (!row.isValuePresent('volume') && !row.isSolventRow()) {
            var limitingRow = getLimitingRow();
            if (limitingRow) {
                row.setComputedMol(limitingRow.mol.value, onMolChanged);
            }
        }
    }

    function onMolarityChanged(row) {
        if (row.areValuesPresent(['volume', 'molarity'])) {
            var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            if (!isLimitingRowExist()) {
                row.limiting = true;
            }

            return;
        }

        if (!row.isValuePresent('volume') && row.isValuePresent('mol')) {
            var volume = calculationUtil.computeVolumeByMolarity(row.mol.value, row.molarity.value);
            row.setComputedVolume(volume);
            return;
        }

        if (!row.isValuePresent('molarity')) {
            var fieldToReset = row.getResetFieldForMolarity();
            var callback = fieldToReset === 'mol' ? onMolChanged : null;
            row.resetFields([fieldToReset], callback);
        }
    }

    function onPurityChanged(row) {
        if (!row.stoicPurity.value) {
            row.stoicPurity.value = row.stoicPurity.prevValue;
            return;
        }

        //TODO: discuss with Evgenia
        var k = calculationUtil.divide(row.stoicPurity.value, row.stoicPurity.prevValue);
        var divider = calculationUtil.multiply(k, 100);
        row.stoicPurity.prevValue = row.stoicPurity.value;

        if (row.isLimiting()) {
            var mol = calculationUtil.computeMolByPurity(divider, row.mol.value);
            updateMol(row, mol);
        }

        if (!row.isLimiting()) {
            row.weight.entered
                ? row.setComputedMol(calculationUtil.computeMolByPurity(divider, row.mol.value), updateDependencies)
                : row.weight.value = calculationUtil.computeWeightByPurity(divider, row.weight.value);
        }
    }

    function onSaltChanged(row) {
        //TODO just disable this field if molWeight doesn't exist
        if (!row.molWeight.originalValue) {
            return;
        }

        if (row.saltCode.weight === 0) {
            row.setDefaultValues(['saltEq']);
        }

        var molWeight = calculationUtil.computeMolWeightBySalt(row.molWeight.originalValue, row.saltCode.weight, row.saltEq.value);
        row.setComputedMolWeight(molWeight, row.molWeight.originalValue, onMolWeightChanged);
    }

    function onDensityChanged(row) {
        if (row.areValuesPresent(['density', 'volume'])) {
            var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);
            return;
        }

        if (row.areValuesPresent(['density', 'weight'])) {
            var volume = calculationUtil.computeVolumeByDensity(row.weight.value, row.density.value);
            row.setComputedVolume(volume);
            return;
        }

        if (!row.isValuePresent('density')) {
            var fieldToReset = row.getResetFieldForDensity();
            var callback = fieldToReset === 'weight' ? onWeightChanged : null;
            row.resetFields([fieldToReset], callback);
        }
    }

    function getRowAfterLimiting() {
        var indexOfLimitingRow = _.findIndex(stoichTable.reactants, {limiting: true});
        var indexOfNextRow = indexOfLimitingRow + 1;

        if (stoichTable.reactants.length > indexOfNextRow) {
            var rowsAfterLimiting = stoichTable.reactants.slice(indexOfNextRow, stoichTable.reactants.length);

            return _.find(rowsAfterLimiting, function(row) {
                return !row.isSolventRow() && row.eq.value === 1;
            });
        }
    }

    function updateRows(mol) {
        if (isLimitingRowExist()) {
            _.forEach(stoichTable.reactants, function(row) {
                var canUpdateMol = !row.isLimiting() && !row.weight.entered;
                var canUpdateEq = !row.isLimiting() && row.weight.entered;

                if (canUpdateMol) {
                    row.updateMol(mol, onMolChanged);
                }

                if (canUpdateEq) {
                    row.updateEqDependingOnLimitingEq(getLimitingRow());
                }
            });
        }
    }

    function updateDependencies(row) {
        row.updateVolume();
        row.updateEq(getLimitingRow());
        row.updateMolWeight();
    }

    function updateMol(row, mol) {
        if (row.weight.entered) {
            row.setComputedMol(mol, updateDependencies);
            updateRows(mol);
        } else {
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function isLimitingRowExist() {
        return _.some(stoichTable.reactants, {limiting: true});
    }

    function getLimitingRow() {
        return _.find(stoichTable.reactants, {limiting: true});
    }

    function setStoichTable(table) {
        stoichTable = table;
    }

    function getStoichTable() {
        return stoichTable;
    }
}

module.exports = stoichTable;
