function ReagentViewField(value, unit, isEntered, isReadonly) {
    this.value = value || 0;
    this.unit = unit || '';
    this.entered = isEntered || false;
    this.readonly = isReadonly || false;
}

module.exports = ReagentViewField;
