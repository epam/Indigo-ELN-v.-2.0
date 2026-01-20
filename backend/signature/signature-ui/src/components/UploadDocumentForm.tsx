import {useNavigate, useSearchParams} from "react-router";
import {server} from "../model/Server.ts";
import {useAuth} from "react-oidc-context";
import {useState} from "react";

interface UploadDocumentFormProps {
}

const UploadDocumentForm: React.FC<UploadDocumentFormProps> = () => {
    const auth = useAuth()
    const [searchParams, setSearchParams] = useSearchParams()
    const [formData, setFormData] = useState({
        file: null,
        name: null
    })
    const navigate = useNavigate()
    const handleChange = (event) => {
        console.log(event)
        setFormData({...formData, 'file': event.target.files[0], 'name': event.target.files[0].name})
    }
    const upload = function () {
        server.uploadDocument(parseInt(searchParams.get('templateId')), formData.file, formData.name, auth.user?.access_token)
            .then(() => {navigate('/documents')})
    }
    return (
        <div>
            <h1>Upload Document</h1>
            <form>
                <label>File: <input type="file" name="file" onChange={handleChange}/></label>
            </form>
            <button onClick={upload}>Upload</button>
        </div>
    )
}

export default UploadDocumentForm
