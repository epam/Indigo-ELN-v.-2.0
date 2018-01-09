var fieldTypes = require('../../domain/field-types');
var mathCalculation = require('../../../../services/calculation/math-calculation');

/* @ngInject */
function reagentsCalculation() {
    var rows;

    return {
        calculate: calculate
    };

    function calculate(reagentsData) {
        rows = _.cloneDeep(reagentsData.rows);

        var changedRow = _.find(rows, function(row) {
            return _.isEqual(row, reagentsData.changedRow);
        });

        recalculate(changedRow, reagentsData.changedField);

        return rows;
    }

    function recalculate(row, fieldId) {
        // Handle necessary recalculations here
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
                // Handle changes in the table caused by change in compoundId
                onNbkBatchOrCompoundIdChanged(row);
                break;
            case fieldTypes.fullNbkBatch:
                // Handle changes in the table caused by applying notebook batch
                onNbkBatchOrCompoundIdChanged(row);
                break;
            default:
                onAddNewRow(row);
                break;
        }
    }

    function onMolWeightChanged(row) {
        row.molWeight.originalValue = row.molWeight.value;

        if (row.isMolWeightPresent()) {
            if (row.isLimiting() && row.isWeightPresent()) {
                var mol = mathCalculation.computeMol(row.weight.value, row.molWeight.value, row.stoicPurity.value);
                updateMol(row, mol);

                return;
            }
            if (row.isMolPresent()) {
                var weight = mathCalculation.computeWeight(row.mol.value, row.molWeight.value, row.stoicPurity.value);
                row.setComputedWeight(weight, onWeightChanged);
            }
        }
    }

    function onWeightChanged(row) {
        if (row.isWeightPresent() && row.isMolWeightPresent()) {
            var mol = mathCalculation.computeMol(row.weight.value, row.molWeight.value, row.stoicPurity.value);
            row.setComputedMol(mol);

            if (row.isLimiting()) {
                updateRows();
            }
        }

        if (!row.isWeightPresent()) {
            row.resetFields(row.getResetFieldsForWeight());
        }

        updateDependencies(row);
    }

    function onMolChanged(row) {
        if (row.isMolWeightPresent() && row.isMolPresent()) {
            var weight = mathCalculation.computeWeight(row.mol.value, row.molWeight.value, row.stoicPurity.value);
            row.setComputedWeight(weight);
        }

        if (row.isLimiting()) {
            updateRows();
        }

        if (!row.isMolPresent()) {
            row.resetFields(row.getResetFieldsForMol());
        }

        updateDependencies(row);
    }

    function onEqChanged(row) {
        if (!row.eq.value) {
            row.eq.value = row.eq.prevValue;

            return;
        }

        var shouldResetVolume = row.isVolumePresent() && !row.isDensityPresent() && !row.isMolarityPresent();

        if (shouldResetVolume) {
            row.resetFields([fieldTypes.volume]);
            setMolDependingOfLimiting(row, getLimitingRow());

            return;
        }

        if (row.isLimiting()) {
            if (row.isMolManuallyEntered() && row.isWeightPresent()) {
                var limitingWeight = mathCalculation.computeWeightByEq(
                    row.weight.value, row.eq.value, row.eq.prevValue
                );
                row.setComputedWeight(limitingWeight);
            }

            if (row.isMolPresent()) {
                updateRows();
            }
        }

        if (!row.isLimiting()) {
            if (row.isWeightPresent()) {
                var weight = mathCalculation.computeWeightByEq(row.weight.value, row.eq.value, row.eq.prevValue);
                row.setComputedWeight(weight);
            }

            if (row.isMolPresent()) {
                var mol = mathCalculation.computeMolByEq(row.mol.value, row.eq.value, row.eq.prevValue);
                row.setComputedMol(mol);
            }

            row.updateVolume();
        }

        row.eq.prevValue = row.eq.value;
    }

    function onRxnRoleChanged(row) {
        var fieldsToReset = row.getResetFieldsForSolvent();

        if (row.isSolventRow()) {
            var nextRow = getRowAfterLimiting();
            var isLimiting = row.isLimiting();

            row.resetFields(fieldsToReset);
            row.setReadonly(fieldsToReset, true);

            if (isLimiting) {
                row.limiting.value = false;
            }

            if (isLimiting && nextRow) {
                nextRow.limiting.value = true;
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
                var mol = mathCalculation.computeDissolvedMol(row.molarity.value, row.volume.value);
                row.setComputedMol(mol, onMolChanged);

                return;
            }

            if (row.isDensityPresent()) {
                var weight = mathCalculation.computeWeightByDensity(row.volume.value, row.density.value);
                row.setComputedWeight(weight, onWeightChanged);

                return;
            }

            if (!row.isSolventRow()) {
                if (row.isLimiting()) {
                    var nextRow = getRowAfterLimiting();
                    if (nextRow) {
                        nextRow.limiting.value = true;
                    }

                    row.limiting.value = false;
                }

                row.resetFields([fieldTypes.weight, fieldTypes.mol, fieldTypes.eq]);

                return;
            }
        }

        if (!row.isVolumePresent()) {
            row.resetFields([fieldTypes.volume]);

            // TODO: problem with volume should implement test

            if (row.isMolarityPresent() && !row.isMolManuallyEntered()) {
                row.resetFields([fieldTypes.mol], onMolChanged);

                return;
            }

            if (row.isDensityPresent() && !row.isWeightManuallyEntered()) {
                row.resetFields([fieldTypes.weight], onWeightChanged);

                return;
            }

            if (!row.isSolventRow() && !row.isWeightManuallyEntered() && !row.isMolManuallyEntered()) {
                setMolDependingOfLimiting(row, getLimitingRow());
            }
        }
    }

    function onMolarityChanged(row) {
        if (!row.isMolarityPresent()) {
            var fieldToReset = row.getResetFieldForMolarity();
            var callback = fieldToReset === fieldTypes.mol ? onMolChanged : null;
            row.resetFields([fieldToReset, fieldTypes.molarity], callback);
            setMolFromLimitingRow(row, row.isMolPresent());

            return;
        }

        if (row.isVolumePresent()) {
            var mol = mathCalculation.computeDissolvedMol(row.molarity.value, row.volume.value);
            row.setComputedMol(mol, onMolChanged);

            if (!isLimitingRowExist()) {
                row.limiting.value = true;
            }

            return;
        }

        if (!row.isVolumePresent() && row.isMolPresent()) {
            var volume = mathCalculation.computeVolumeByMolarity(row.mol.value, row.molarity.value);
            row.setComputedVolume(volume);
        }
    }

    function onPurityChanged(row) {
        var prevPurity = row.stoicPurity.prevValue;
        var currentPurity = row.stoicPurity.value;
        var areMolAndWeightEmpty = !row.isMolPresent() && !row.isWeightPresent();
        var shouldUpdateMol = row.isWeightManuallyEntered() && row.isMolPresent();
        var shouldUpdateWeight = (row.isMolManuallyEntered() && row.isWeightPresent())
            || (!row.isWeightManuallyEntered() && !row.isMolManuallyEntered());

        if (!row.isPurityPresent()) {
            row.stoicPurity.value = row.stoicPurity.prevValue;

            return;
        }

        row.stoicPurity.prevValue = row.stoicPurity.value;

        if (areMolAndWeightEmpty) {
            return;
        }

        if (shouldUpdateMol) {
            var mol = mathCalculation.computeMolByPurity(row.mol.value, currentPurity, prevPurity);
            row.setComputedMol(mol, updateDependencies);

            if (row.isLimiting()) {
                updateRows();
            }
        } else if (shouldUpdateWeight) {
            var weight = mathCalculation.computeWeightByPurity(row.weight.value, currentPurity, prevPurity);
            row.setComputedWeight(weight, updateDependencies);
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

        var molWeight = mathCalculation.computeMolWeightBySalt(
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
            var weight = mathCalculation.computeWeightByDensity(row.volume.value, row.density.value);
            row.setComputedWeight(weight, onWeightChanged);

            return;
        }

        if (row.isWeightPresent()) {
            var volume = mathCalculation.computeVolumeByDensity(row.weight.value, row.density.value);
            row.setComputedVolume(volume);
        }
    }

    function onNbkBatchOrCompoundIdChanged(row) {
        // TODO: should implement test
        if (!isLimitingRowExist()) {
            row.limiting.value = true;

            return;
        }

        setMolDependingOfLimiting(row, getLimitingRow());
    }

    function onAddNewRow(newRow) {
        if (newRow.isSolventRow()) {
            newRow.prevRxnRole.name = 'SOLVENT';
            newRow.setReadonly(newRow.getResetFieldsForSolvent(), true);
        } else if (isLimitingRowExist()) {
            setMolDependingOfLimiting(newRow, getLimitingRow());
        } else {
            newRow.limiting.value = true;
        }
    }

    function getRowAfterLimiting() {
        var indexOfLimitingRow = _.findIndex(rows, {limiting: true});
        var indexOfNextRow = indexOfLimitingRow + 1;

        if (rows.length > indexOfNextRow) {
            var rowsAfterLimiting = rows.slice(indexOfNextRow, rows.length);

            return _.find(rowsAfterLimiting, function(row) {
                return !row.isSolventRow() && row.eq.value === 1;
            });
        }
    }

    function updateRows() {
        if (!isLimitingRowExist()) {
            return;
        }

        var limitingRow = getLimitingRow();
        _.forEach(rows, function(row) {
            var canUpdate = !row.isLimiting() && !row.isSolventRow();
            var isManuallyEnteredExist =
                row.isWeightManuallyEntered() || row.isVolumeManuallyEntered() || row.isMolManuallyEntered();
            var shouldUpdateRowWithNewMol = !isManuallyEnteredExist && canUpdate;
            var shouldUpdateOnlyEq = isManuallyEnteredExist && canUpdate;

            if (shouldUpdateRowWithNewMol) {
                setMolDependingOfLimiting(row, limitingRow);
            }

            if (shouldUpdateOnlyEq) {
                row.updateEq(limitingRow);
            }
        });
    }

    function setMolFromLimitingRow(row, isMolOrWeightPresent) {
        var shouldSetMolFromLimitingRow = !isMolOrWeightPresent && !row.isVolumePresent();

        if (shouldSetMolFromLimitingRow) {
            setMolDependingOfLimiting(row, getLimitingRow());
        }
    }

    function updateDependencies(row) {
        row.updateVolume();

        if (!row.isLimiting()) {
            row.updateEq(getLimitingRow());
        }
        row.updateMolWeight();
    }

    function updateMol(row, mol) {
        if (row.isWeightManuallyEntered()) {
            row.setComputedMol(mol, updateDependencies);
            updateRows();
        } else {
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function setMolDependingOfLimiting(row, limitingRow) {
        if (limitingRow && limitingRow.isMolPresent()) {
            var mol = mathCalculation.computeNonLimitingMolByEq(
                limitingRow.mol.value, row.eq.value, limitingRow.eq.value
            );
            row.setComputedMol(mol, onMolChanged);
        }
    }

    function isLimitingRowExist() {
        return _.some(rows, ['limiting.value', true]);
    }

    function getLimitingRow() {
        return _.find(rows, ['limiting.value', true]);
    }
}

module.exports = reagentsCalculation;
