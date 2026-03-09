// Config interface to adapt component to entity context (project, notebook, etc.)
export interface TeamComponentConfig {
    buildAccessEndpoint: (entityId: string) => string; // e.g. projects/{id}/access or notebooks/{id}/access
    buildNestedAccessEndpoint: (entityId: string) => string; // e.g. projects/{id}/nestedAccess
}
