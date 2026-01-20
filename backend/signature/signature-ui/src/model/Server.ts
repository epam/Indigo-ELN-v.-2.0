import axios from 'axios';
import {Document, Template} from "./state.ts";

class SignatureServer {

    async getTemplates(accessToken: string): Promise<Template[]> {
        return this.call('GET', '/api/signature/templates', null, accessToken)
    }

    async getDocuments(accessToken: string): Promise<Document[]> {
        return this.call('GET', '/api/signature/documents', null, accessToken)
    }

    async uploadDocument(templateId: number, file: File, name: string, accessToken: string): Promise<void> {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('name', name)
        formData.append('templateId', templateId.toString())
        return this.call('POST', '/api/signature/documents/upload', formData, accessToken)
    }

    async signOrReject(documentId: number, reject: boolean, keyStore: File, keyStorePassword: string, accessToken: string): Promise<void> {
        const data = new FormData()
        data.append('keyStore', keyStore)
        data.append('keyStorePassword', keyStorePassword)
        return this.call('POST', `/api/signature/documents/${documentId}/${reject ? 'reject' : 'sign'}`, data, accessToken)
    }

    private async call<I, T>(method: string, url: string, data: I, access_token: string): Promise<T> {
        const config = {headers: {Authorization: `Bearer ${access_token}`}};
        const response = method == 'GET' ? axios.get(url, config)
            : method == 'POST' ? axios.post(url, data, config)
                : null
        if (response == null) {
            throw new Error("Invalid method: " + method)
        }
        try {
            return (await response).data
        } catch (e) {
            console.log("Server call failed: " + e.toString())
            alert("Server call failed: " + e.toString())
            throw e
        }
    }
}

export const server = new SignatureServer();
