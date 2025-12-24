/* eslint-disable @typescript-eslint/no-empty-object-type */
import React, {useState} from 'react';
import {useDocumentsStore} from "../model/state.ts";
import {useAuth} from "react-oidc-context";
import {useNavigate} from "react-router";
import {server} from "../model/Server.ts";

interface DocumentListProps {
}

const DocumentList: React.FC<DocumentListProps> = () => {
    const auth = useAuth()
    const {documents, loadDocuments} = useDocumentsStore()
    const navigate = useNavigate()
    const [keystoreData, setKeystoreData] = useState({
        keyStore: null,
        keyStorePassword: null
    })
    console.log(documents)
    if (documents.loading) {
        return <div>Loading...</div>
    }
    if (documents.error) {
        console.log(documents.error)
        return <div>Error: {documents.error.toString()}</div>
    }
    if (!documents.started) {
        console.log(auth)
        loadDocuments(auth.user?.access_token as string)
        return
    }
    const reload = () => {
        loadDocuments(auth.user?.access_token as string)
    }
    const setKeyStore = (event) => {
        setKeystoreData({...keystoreData, keyStore: event.target.files[0]})
    }
    const setKeyStorePassword = (event) => {
        setKeystoreData({...keystoreData, keyStorePassword: event.target.value})
    }
    const signOrReject = (documentId: number, reject: boolean) => {
        server.signOrReject(documentId, reject, keystoreData.keyStore, keystoreData.keyStorePassword, auth.user?.access_token as string)
            .then(() => {reload()})
    }
    const download = async (url: string) => {
        const response = await fetch(url, {method: 'GET', headers: {
                'Authorization': `Bearer ${auth.user.access_token}`
        }});
        console.log(response)
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        console.log(response.headers.get('Content-Disposition'))
        a.download = response.headers.get('Content-Disposition')?.split('filename=')[1];
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
    }
    return (
        <>
            <form>
                <label>Keystore: <input type='file' onChange={setKeyStore}/></label>
                <label>Password: <input onChange={setKeyStorePassword}/></label>
            </form>
            <table className={'datatable'}>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Status</th>
                    <th>Author</th>
                    <th>Created</th>
                    <th>Modified</th>
                    <th>Signatures</th>
                </tr>
                </thead>
                <tbody>
                {documents.value?.map(document => (
                    <tr>
                        <td>{document.id}</td>
                        <td><a href='#' onClick={() => download(`/api/signature/documents/${document.id}/download`)}>{document.name}</a></td>
                        <td>{document.status}</td>
                        <td>{document.author}</td>
                        <td>{document.createdDate}</td>
                        <td>{document.lastModifiedDate}</td>
                        <td>
                            <table>
                            {document.signatureBlocks.map(block => (
                                <tr>
                                    <td>{block.user}</td>
                                    <td>{block.reason}</td>
                                    <td>{block.status}</td>
                                    <td>{block.actionDate}</td>
                                    <td>{block.canSignOrReject ? (<button onClick={() => signOrReject(document.id, false)}>Sign</button>) : ''}</td>
                                    <td>{block.canSignOrReject ? (<button onClick={() => signOrReject(document.id, true)}>Reject</button>) : ''}</td>
                                </tr>
                            ))}
                            </table>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
            <button onClick={reload}>Reload</button>
        </>
    )
};

export default DocumentList;
