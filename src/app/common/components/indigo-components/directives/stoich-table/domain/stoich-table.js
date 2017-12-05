var calculationUtil = require('../calculation/calculation-util');

function stoichTable(table) {
    var stoichTable = table;

    return {
        addRow: addRow,
        onColumnValueChanged: onColumnValueChanged,
        setStoichTable: setStoichTable,
        getStoichTable: getStoichTable
    };

    function addRow(newRow) {
        if (newRow.isSolventRow()) {
            newRow.setReadonly(newRow.getResetFieldsForSolvent(), true);
        } else {
            isLimitingRowExist()
                ? newRow.updateMol(getLimitingRow().mol.value, onMolChanged)
                : newRow.limiting = true;
        }

        stoichTable.reactants.push(newRow);
    }

    function onColumnValueChanged(row, fieldId, previousValue) {
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
                onRxnRoleChanged(row, previousValue);
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
        }
    }

    function onMolWeightChanged(row) {
        if (row.molWeight.value) {
            if (row.isLimiting() && row.weight.value) {
                var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
                row.setComputedMol(mol, onMolChanged);
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
                updateRowsWithNewMol(row.mol.value);
            }

            row.updateVolume();
            row.updateEQ(getLimitingRow());

            return;
        }

        if (row.areValuesPresent(['weight', 'density'])) {
            row.updateVolume();

            return;
        }

        if (!row.isValuePresent('weight')) {
            row.setDefaultValues(['mol', 'eq']);
            return;
        }

        row.updateMolWeight();
    }

    function onMolChanged(row) {
        if (row.areValuesPresent(['molWeight', 'mol'])) {
            var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
            row.setComputedWeight(weight);
            if (row.isLimiting()) {
                updateRowsWithNewMol(row.mol.value);
            }
        }

        if (!row.isValuePresent('mol')) {
            row.resetFields(row.getResetFieldsForMol());
        }

        row.updateVolume();
        row.updateEQ(getLimitingRow());
        row.updateMolWeight();
    }

    function onEqChanged(row) {
        //TODO: remove this case in the future
        if (!row.eq.value) {
            row.eq.value = 1;
            return;
        }

        if (row.isLimiting() && row.isValuePresent('mol')) {
            var molByEq = calculationUtil.computeMolByEq(row.mol.value, row.eq.value);
            updateRowsWithNewMol(molByEq);
        }

        if (!row.isLimiting() && row.isValuePresent('weight')) {
            var weight = calculationUtil.multiply(row.weight.value, row.eq.value);
            row.setComputedWeight(weight);
        }

        if (!row.isLimiting() && row.isValuePresent('mol')) {
            var mol = calculationUtil.multiply(row.mol.value, row.eq.value);
            row.setComputedMol(mol);
        }
    }

    function onRxnRoleChanged(row, previousValue) {
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
        } else if (!row.isSolventRow() && previousValue === 'SOLVENT') {
            //TODO it doesn't work because previous value is always undefined
            row.resetFields(['volume']);
            row.setReadonly(fieldsToReset, false);

            if (isLimitingRowExist()) {
                row.setComputedMol(getLimitingRow().mol.value);
            }
        }
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
            var volume = calculationUtil.computeVolume(row.mol.value, row.molarity.value);
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
        if (row.stoicPurity.value) {
            if (row.isLimiting()) {
                var mol = calculationUtil.computeMolByPurity(row.stoicPurity.value, row.mol.value);
                row.setComputedMol(mol, onMolChanged);
            }

            if (!row.isLimiting()) {
                var weight = calculationUtil.computeWeightByPurity(row.stoicPurity.value, row.weight.value);
                row.setComputedWeight(weight, onWeightChanged);
            }
        }
    }

    function onSaltChanged(row) {
        if (!row.saltCode.weight && !row.saltEq.value && !row.molWeight.value && !row.weight.value) {
            return;
        }

        var molWeight = calculationUtil.computeMolWeightBySalt(row.molWeight.value, row.saltCode.weight, row.saltEq.value);
        row.setComputedMolWeight(molWeight, onMolWeightChanged);
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

    function updateRowsWithNewMol(mol) {
        if (isLimitingRowExist()) {
            _.forEach(stoichTable.reactants, function(row) {
                row.updateMol(mol, onMolChanged);
            });
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
