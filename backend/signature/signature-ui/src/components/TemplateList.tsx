/* eslint-disable @typescript-eslint/no-empty-object-type */
import React from 'react';
import {useTemplateStore} from "../model/state.ts";
import {useAuth} from "react-oidc-context";
import {useNavigate} from "react-router";

interface TemplateListProps {
}

const TemplateList: React.FC<TemplateListProps> = () => {
    const auth = useAuth()
    const {templates, loadTemplates} = useTemplateStore()
    const navigate = useNavigate()
    console.log(templates)
    if (templates.loading) {
        return <div>Loading...</div>
    }
    if (templates.error) {
        console.log(templates.error)
        return <div>Error: {templates.error.toString()}</div>
    }
    if (!templates.started) {
        console.log(auth)
        loadTemplates(auth.user?.access_token as string)
        return
    }
    return (
        <table className={'datatable'}>
            <thead>
            <tr>
                <th>Name</th>
                <th>Author</th>
                <th>Created</th>
            </tr>
            </thead>
            <tbody>
            {templates.value?.map(template => (
                <tr>
                    <td>{template.name}</td>
                    <td>{template.author}</td>
                    <td>{template.createdDate}</td>
                    <td><button onClick={() => navigate(`/documents/upload?templateId=${template.id}`)}>Upload document</button></td>
                </tr>
            ))}
            </tbody>
        </table>
    );
};

export default TemplateList;
