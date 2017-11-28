function StoichField(value, unit, entered) {
    this.value = value || 0;
    this.unit = unit || '';
    this.entered = entered || false;
}

module.exports = StoichField;
