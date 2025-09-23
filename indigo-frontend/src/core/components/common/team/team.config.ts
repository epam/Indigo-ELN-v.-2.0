// Config interface to adapt component to entity context (project, notebook, etc.)
export interface TeamComponentConfig {
    title?: string; // e.g. 'Team' or 'Notebook Team'
    buildAccessEndpoint: (entityId: string) => string; // e.g. projects/{id}/access or notebooks/{id}/access
}
