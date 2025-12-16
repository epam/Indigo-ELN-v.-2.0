import {create} from 'zustand'
import {server} from "./Server.ts";
import {devtools} from "zustand/middleware";

// import {devtools} from "zustand/middleware/devtools";

export enum Reason {
    AUTHOR = "AUTHOR",
    WITNESS = "WITNESS"
}

export interface TemplateSignatureBlock {
    username: string
    reason: Reason
}

export interface Template {
    id: number
    name: string
    author: string
    createdDate: string // TODO Date?
    signatureBlocks: TemplateSignatureBlock[]
}

export interface Document {
    id: number
    name: string
    status: string
    createdDate: string
    lastModifiedDate: string
    author: string
    signatureBlocks: DocumentSignatureBlock[]
}

export interface DocumentSignatureBlock {
    user: string
    reason: Reason
    actionDate: string
    status: string
    canSignOrReject: boolean
}

type FutureState<T> = {
    started: boolean
    loading: boolean
    error: any | null
    value: T | null
}

function notStarted<T>(): FutureState<T> {
    return {started: false, loading: false, error: null, value: null}
}
function loading<T>(): FutureState<T> {
    return {started: true, loading: true, error: null, value: null}
}
function loaded<T>(value: T): FutureState<T> {
    return {started: true, loading: false, error: null, value}
}
function error<T>(error: any): FutureState<T> {
    return {started: true, loading: false, error, value: null}
}

export interface TemplatesState {
    templates: FutureState<Template[]>
    loadTemplates: (access_token: string) => Promise<void>
}

export const useTemplateStore = create<TemplatesState>()(
    devtools(
        (set) => ({
            templates: notStarted(),
            loadTemplates: async (access_token) => {
                console.log("loadTemplates, access_token=", access_token)
                set({templates: loading()})
                try {
                    const templates = await server.getTemplates(access_token)
                    set({templates: loaded(templates)})
                } catch (e) {
                    set({templates: error(e)})
                }
            }
        }),
        {name: 'signatureStore'}
    )
)

export interface DocumentsState {
    documents: FutureState<Document[]>
    loadDocuments: (access_token: string) => Promise<void>
}

export const useDocumentsStore = create<DocumentsState>()(
    devtools(
        (set) => ({
            documents: notStarted(),
            loadDocuments: async (access_token) => {
                console.log("loadDocuments, access_token=", access_token)
                set({documents: loading()})
                try {
                    const documents = await server.getDocuments(access_token)
                    set({documents: loaded(documents)})
                } catch (e) {
                    set({documents: error(e)})
                }
            }
        }),
        {name: 'documentsStore'}
    )
)

export interface EditTemplateState {
    template: FutureState<Template>
    createNew: () => void
}

export const useEditTemplateStore = create<EditTemplateState>()(
    devtools(
        (set) => ({
            template: notStarted(),
            createNew: () => {
                set({template: loaded({id: 0, name: '', author: '', createdDate: '', signatureBlocks: []})})
            },
            addBlock: () => {
                set((state) => {
                    if (!state.template.value) {
                        return state
                    }
                    return {
                        template: {
                            ...state.template,
                            value: {
                                ...state.template.value,
                                signatureBlocks: [...state.template.value.signatureBlocks, {username: '', reason: Reason.AUTHOR}]
                            }
                        }
                    }
                })
            }
        }),
        {name: 'editTemplateStore'})
    )
