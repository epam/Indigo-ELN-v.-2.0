var fieldTypes = require('../field-types');
var calculationUtil = require('../../calculation/calculation-util');

function stoichTable(config) {
    var table = config.table;
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
        } else if (isLimitingRowExist()) {
            newRow.updateMol(getLimitingRow().mol.value, onMolChanged);
        } else {
            newRow.limiting = true;
        }

        table.reactants.push(newRow);
    }

    function onFieldValueChanged(row, fieldId) {
        // TODO: refactor it
        switch (fieldId) {
            case fieldTypes.molWeight:
                row.setEntered(fieldId);
                onMolWeightChanged(row);
                break;
            case fieldTypes.weight:
                row.setEntered(fieldId);
                onWeightChanged(row);
                break;
            case fieldTypes.mol:
                row.setEntered(fieldId);
                onMolChanged(row);
                break;
            case fieldTypes.eq:
                row.setEntered(fieldId);
                onEqChanged(row);
                break;
            case fieldTypes.rxnRole:
                onRxnRoleChanged(row);
                break;
            case fieldTypes.volume:
                row.setEntered(fieldId);
                onVolumeChanged(row);
                break;
            case fieldTypes.molarity:
                row.setEntered(fieldId);
                onMolarityChanged(row);
                break;
            case fieldTypes.stoicPurity:
                row.setEntered(fieldId);
                onPurityChanged(row);
                break;
            case fieldTypes.saltCode:
                onSaltChanged(row);
                break;
            case fieldTypes.saltEq:
                onSaltChanged(row);
                break;
            case fieldTypes.density:
                row.setEntered(fieldId);
                onDensityChanged(row);
                break;
            case fieldTypes.compoundId:
                onCompoundIdChanged(row, row[fieldId]);
                break;
            default:
                break;
        }
    }

    function onMolWeightChanged(row) {
        row.molWeight.originalValue = row.molWeight.value;

        if (row.isMolWeightPresent()) {
            if (row.isLimiting() && row.isWeightPresent()) {
                var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
                updateMol(row, mol);

                return;
            }
            if (row.isMolPresent()) {
                var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
                row.setComputedWeight(weight, onWeightChanged);
            }
        }
    }

    function onWeightChanged(row) {
        if (row.areValuesPresent([fieldTypes.weight, fieldTypes.molWeight])) {
            var mol = calculationUtil.computePureMol(row.weight.value, row.molWeight.value);
            row.setComputedMol(mol);

            if (row.isLimiting()) {
                updateRows(row.mol.value);
            }
        }

        if (!row.isValuePresent(fieldTypes.weight)) {
            row.resetFields(row.getResetFieldsForWeight());
        }

        updateDependencies(row);
    }

    function onMolChanged(row) {
        if (row.stoicPurity.entered) {
            // TODO: discuss with Evgenia should be in calculation-util
            var k = calculationUtil.divide(row.stoicPurity.value, row.stoicPurity.prevValue);
            var divider = calculationUtil.multiply(k, 100);
            row.stoicPurity.prevValue = row.stoicPurity.value;
            var weightByPurity = calculationUtil.computeWeightByPurity(divider, row.weight.value);
            row.setComputedWeight(weightByPurity);
        }

        if (row.areValuesPresent([fieldTypes.molWeight, fieldTypes.mol]) && !row.stoicPurity.entered) {
            var weight = calculationUtil.computeWeight(row.mol.value, row.molWeight.value);
            row.setComputedWeight(weight);
        }

        if (row.isLimiting()) {
            updateRows(row.mol.value);
        }

        if (!row.isValuePresent(fieldTypes.mol)) {
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
        var shouldResetVolume = row.isVolumePresent() && !row.isDensityPresent() && !row.isMolarityPresent();
        row.eq.prevValue = row.eq.value;

        if (shouldResetVolume) {
            row.resetFields([fieldTypes.volume]);
        }

        if (row.isLimiting() && row.isMolPresent()) {
            var molByEq = calculationUtil.computeMolByEq(row.mol.value, row.eq.value);
            updateRows(molByEq);
        }

        if (!row.isLimiting()) {
            if (row.isWeightPresent()) {
                var weight = calculationUtil.multiply(row.weight.value, multiplier);
                row.setComputedWeight(weight);
            }

            if (row.isMolPresent()) {
                var mol = calculationUtil.multiply(row.mol.value, multiplier);
                row.setComputedMol(mol);
            }

            row.updateVolume();
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
            row.resetFields([fieldTypes.volume]);
            row.setReadonly(fieldsToReset, false);

            if (isLimitingRowExist()) {
                row.setComputedMol(getLimitingRow().mol.value, onMolChanged);
            }
        }

        row.prevRxnRole.name = row.rxnRole.name;
    }

    function onVolumeChanged(row) {
        if (row.isVolumePresent()) {
            if (row.isMolarityPresent()) {
                var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
                row.setComputedMol(mol, onMolChanged);

                return;
            }

            if (row.isDensityPresent()) {
                var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
                row.setComputedWeight(weight, onWeightChanged);

                return;
            }

            if (!row.isSolventRow()) {
                if (row.isLimiting()) {
                    var nextRow = getRowAfterLimiting();
                    if (nextRow) {
                        nextRow.limiting = true;
                    }

                    row.limiting = false;
                }

                row.resetFields([fieldTypes.weight, fieldTypes.mol, fieldTypes.eq]);

                return;
            }
        }

        if (!row.isVolumePresent()) {
            row.resetFields([fieldTypes.volume]);

            if (row.isMolarityPresent() && row.isMolPresent()) {
                row.resetFields([fieldTypes.mol], onMolChanged);

                return;
            }

            if (row.isDensityPresent()) {
                row.resetFields([fieldTypes.weight], onWeightChanged);

                return;
            }

            if (!row.isSolventRow()) {
                var limitingRow = getLimitingRow();

                if (limitingRow) {
                    row.setComputedMol(limitingRow.mol.value, onMolChanged);
                }
            }
        }
    }

    function onMolarityChanged(row) {
        if (!row.isValuePresent(fieldTypes.molarity)) {
            var fieldToReset = row.getResetFieldForMolarity();
            var callback = fieldToReset === fieldTypes.mol ? onMolChanged : null;
            row.resetFields([fieldToReset, fieldTypes.molarity], callback);
            setMolFromLimitingRow(row, row.isMolPresent());

            return;
        }

        if (row.isVolumePresent()) {
            var mol = calculationUtil.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            if (!isLimitingRowExist()) {
                row.limiting = true;
            }

            return;
        }

        if (!row.isVolumePresent() && row.isMolPresent()) {
            var volume = calculationUtil.computeVolumeByMolarity(row.mol.value, row.molarity.value);
            row.setComputedVolume(volume);
        }
    }

    function onPurityChanged(row) {
        if (!row.stoicPurity.value) {
            row.stoicPurity.value = row.stoicPurity.prevValue;

            return;
        }

        // TODO: discuss with Evgenia
        var k = calculationUtil.divide(row.stoicPurity.value, row.stoicPurity.prevValue);
        var divider = calculationUtil.multiply(k, 100);
        row.stoicPurity.prevValue = row.stoicPurity.value;

        if (row.isLimiting()) {
            var mol = calculationUtil.computeMolByPurity(divider, row.mol.value);
            updateMol(row, mol);
        }

        if (!row.isLimiting()) {
            if (row.isWeightManuallyEntered()) {
                row.setComputedMol(calculationUtil.computeMolByPurity(divider, row.mol.value), updateDependencies);
            } else {
                row.weight.value = calculationUtil.computeWeightByPurity(divider, row.weight.value);
            }
        }
    }

    function onSaltChanged(row) {
        // TODO just disable this field if molWeight doesn't exist
        if (!row.molWeight.originalValue) {
            return;
        }

        if (row.saltCode.weight === 0) {
            row.setDefaultValues([fieldTypes.saltEq]);
        }

        var molWeight = calculationUtil.computeMolWeightBySalt(
            row.molWeight.originalValue, row.saltCode.weight, row.saltEq.value
        );
        row.setComputedMolWeight(molWeight, row.molWeight.originalValue, onMolWeightChanged);
    }

    function onDensityChanged(row) {
        if (!row.isDensityPresent()) {
            var fieldToReset = row.getResetFieldForDensity();
            var callback = fieldToReset === fieldTypes.weight ? onWeightChanged : null;
            row.resetFields([fieldToReset, fieldTypes.density], callback);
            setMolFromLimitingRow(row, row.isWeightPresent());

            return;
        }

        if (row.isVolumePresent()) {
            var weight = calculationUtil.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);

            return;
        }

        if (row.isWeightPresent()) {
            var volume = calculationUtil.computeVolumeByDensity(row.weight.value, row.density.value);
            row.setComputedVolume(volume);
        }
    }

    function getRowAfterLimiting() {
        var indexOfLimitingRow = _.findIndex(table.reactants, {limiting: true});
        var indexOfNextRow = indexOfLimitingRow + 1;

        if (table.reactants.length > indexOfNextRow) {
            var rowsAfterLimiting = table.reactants.slice(indexOfNextRow, table.reactants.length);

            return _.find(rowsAfterLimiting, function(row) {
                return !row.isSolventRow() && row.eq.value === 1;
            });
        }
    }

    function updateRows(mol) {
        if (isLimitingRowExist()) {
            _.forEach(table.reactants, function(row) {
                var canUpdate = !row.isLimiting() && !row.isSolventRow();
                var isManuallyEnteredExist = row.isWeightManuallyEntered() || row.isVolumeManuallyEntered();
                var shouldUpdateRowWithNewMol = !isManuallyEnteredExist && canUpdate;
                var shouldUpdateOnlyEq = isManuallyEnteredExist && canUpdate;

                if (shouldUpdateRowWithNewMol) {
                    row.updateMol(mol, onMolChanged);
                }

                if (shouldUpdateOnlyEq) {
                    row.updateEqDependingOnLimitingEq(getLimitingRow());
                }
            });
        }
    }

    function setMolFromLimitingRow(row, isMolOrWeightPresent) {
        var shouldSetMolFromLimitingRow = !isMolOrWeightPresent
            && !row.isVolumePresent()
            && getLimitingRow().isMolPresent();

        if (shouldSetMolFromLimitingRow) {
            row.setComputedMol(getLimitingRow().mol.value, onMolChanged);
        }
    }

    function updateDependencies(row) {
        row.updateVolume();
        row.updateEq(getLimitingRow());
        row.updateMolWeight();
    }

    function updateMol(row, mol) {
        if (row.isWeightManuallyEntered()) {
            row.setComputedMol(mol, updateDependencies);
            updateRows(mol);
        } else {
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function isLimitingRowExist() {
        return _.some(table.reactants, {limiting: true});
    }

    function getLimitingRow() {
        return _.find(table.reactants, {limiting: true});
    }

    function setStoichTable(newTable) {
        table = newTable;
    }

    function getStoichTable() {
        return table;
    }
}

module.exports = stoichTable;
