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
        switch (fieldId) {
            case 'molWeight':
                onMolWeightChanged(row);
                break;
            case 'weight':
                onWeightChanged(row);
                break;
            case 'mol':
                onMolChanged(row);
                break;
            case 'eq':
                onEqChanged(row);
                break;
            case 'rxnRole':
                onRxnRoleChanged(row, previousValue);
                break;
            case 'volume':
                onVolumeChanged(row);
                break;
            case 'molarity':
                onMolarityChanged(row);
                break;
            case 'stoicPurity':
                onPurityChanged(row);
                break;
            case 'saltCode':
                onSaltChanged(row);
                break;
            case 'saltEq':
                onSaltChanged(row);
                break;
            case 'density':
                onDensityChanged(row);
                break;
        }

        row.setEntered(fieldId);
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
        if (row.areFieldsPresent(['weight', 'molWeight'])) {
            var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
            row.setComputedMol(mol);

            if (row.isLimiting()) {
                updateRowsWithNewMol(row.mol.value);
            }

            row.updateVolume();
            row.updateEQ(getLimitingRow());

            return;
        }

        if (row.areFieldsPresent(['weight', 'density'])) {
            row.updateVolume();

            return;
        }

        if (!row.isFieldPresent('weight')) {
            row.setDefaultValues(['mol', 'eq']);
            return;
        }

        row.updateMolWeight();
    }

    function onMolChanged(row) {
        if (!row.mol.value) {
            row.setDefaultValues(['weight', 'eq']);
            return;
        }

        if (row.areFieldsPresent(['molWeight', 'mol'])) {
            var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
            row.setComputedWeight(weight);
            if (row.limiting) {
                updateRowsWithNewMol(row.mol.value);
            }

            row.updateVolume();
            row.updateEQ(getLimitingRow());

            return;
        }

        row.updateMolWeight();
    }

    function onEqChanged(row) {
        //TODO: remove this case in the future
        if (!row.eq.value) {
             row.eq.value = 1;
             return;
        }

        if (row.isLimiting() && row.isFieldPresent('mol')) {
            var molByEq = calculationUtil.computeMolByEq(row.mol.value, row.eq.value);
            updateRowsWithNewMol(molByEq);
        }

        if (!row.isLimiting() && row.isFieldPresent('weight')) {
            var weight = calculationUtil.multiply(row.weight.value, row.eq.value);
            row.setComputedWeight(weight);
        }

        if (!row.isLimiting() && row.isFieldPresent('mol')) {
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
        if (row.areFieldsPresent(['volume', 'mol', 'molarity'])) {
            var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);
            return;
        }

        if (row.areFieldsPresent(['volume', 'density'])) {
            var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);
            return;
        }

        if (row.isFieldPresent('volume') && !row.isSolventRow()) {
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

        if (!row.isFieldPresent('volume') && row.areFieldsPresent(['mol', 'molarity'])) {
            row.resetFields(['mol'], onMolChanged);
            return;
        }

        if (!row.isFieldPresent('volume') && row.isFieldPresent('density')) {
            row.resetFields(['weight'], onWeightChanged);
            return;
        }

        if (!row.isFieldPresent('volume') && !row.isSolventRow()) {
            var limitingRow = getLimitingRow();
            if (limitingRow) {
                row.setComputedMol(limitingRow.mol.value, onMolChanged);
            }
        }
    }

    function onMolarityChanged(row) {
        if (!row.molarity.value) {
            row.setDefaultValues(['volume']);
            return;
        }

        if (row.volume.value && row.molarity.value) {
            var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            if (!isLimitingRowExist()) {
                row.limiting = true;
            }

            return;
        }

        if (!row.volume.value && row.mol.value) {
            var volume = calculationUtil.computeVolume(row.mol.value, row.molarity.value);
            row.setComputedVolume(volume);
            return;
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
        if (row.areFieldsPresent(['density', 'volume'])) {
            var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);
            return;
        }

        if (row.areFieldsPresent(['density', 'weight'])) {
            var volume = calculationUtil.computeVolumeByDensity(row.weight.value, row.density.value);
            row.setComputedVolume(volume);
            return;
        }

        if (!row.isFieldPresent('density')) {
            var fieldToReset = row.getResetFieldForDensity();
            var callback = fieldToReset === 'weight' ? onWeightChanged: null;
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
