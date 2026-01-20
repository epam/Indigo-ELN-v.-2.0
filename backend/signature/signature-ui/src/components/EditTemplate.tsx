import {useEditTemplateStore} from "../model/state.ts";

interface EditTemplateProps {
    isNew: boolean
    id: number
}

const EditTemplate: React.FC<EditTemplateProps> = ({isNew, id}) => {
    const {template, createNew} = useEditTemplateStore()
    if (!template.started) {
        if (isNew) {
            createNew()
        } else {
            // loadTemplate(id)
        }
        return <div>Loading...</div>
    }
    const value = template.value
    if (!value) {
        return
    }
    const blockRows = value.signatureBlocks.map((block, i) => {
        return (
            <>
                <label>Signer:<input value={block.username}/></label>
                <label>Reason:<select value={block.reason}>
                    <option>AUTHOR</option>
                    <option>WITNESS</option>
                </select></label>
                <br/>
            </>)
    })
    return (
        <div>
            <h1>Edit Template</h1>
            <form>
                <label>Name:<input type="text" name="name" value={value.name}/></label>
                <br/>
                {blockRows}
            </form>
        </div>
    )
}
