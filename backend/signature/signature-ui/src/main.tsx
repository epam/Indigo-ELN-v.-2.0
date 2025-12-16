import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import {AuthProvider, useAuth} from "react-oidc-context";
import {HashRouter, Route, Routes} from "react-router";
import TemplateList from "./components/TemplateList.tsx";
import AuthToolbar from "./components/AuthToolbar.tsx";
import NavigationBar from "./components/NavigationBar.tsx";
import UploadDocumentForm from "./components/UploadDocumentForm.tsx";
import DocumentList from "./components/DocumentList.tsx";

const cognitoAuthConfig = {
    authority: "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_GnMjXfy1G",
    client_id: "2a4sr216nlm9me536ev8tic5uk",
    redirect_uri: "http://localhost:5173/",
    response_type: "code",
    scope: "aws.cognito.signin.user.admin email openid phone profile",
    onSigninCallback: (/*_user: User | void*/): void => {
        window.history.replaceState({}, document.title, window.location.pathname)
    }
};

const localAuthConfig = {
    authority: "https://keycloak.indigoeln.local:8443/realms/indigo-eln",
    client_id: "indigo-eln-client",
    // client_secret: "secret",
    redirect_uri: `${window.location.protocol}//${window.location.host}/`,
    response_type: "code",
    scope: "openid profile email phone",
    onSigninCallback: (/*_user: User | void*/): void => {
        window.history.replaceState({}, document.title, window.location.pathname)
    }
}

// const authConfig = localAuthConfig
const authConfig = cognitoAuthConfig
// const authConfig = window.location.hostname == 'localhost' ? localAuthConfig : cognitoAuthConfig

function DeclareRoutes() {
    const auth = useAuth()
    if (!auth.isAuthenticated) {
        return
    }
    return (
        <>
            <NavigationBar/>
            <Routes>
                <Route path={"/"} element={<TemplateList/>}/>
                <Route path={"/templates"} element={<TemplateList/>}/>
                <Route path={"/documents"} element={<DocumentList/>}/>
                <Route path={"/documents/upload"} element={<UploadDocumentForm/>}/>
                {/*<App/>*/}
            </Routes>
        </>
    )
}

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <AuthProvider {...authConfig}>
            <AuthToolbar/>
            <HashRouter>
                <DeclareRoutes/>
            </HashRouter>
        </AuthProvider>
    </StrictMode>,
)
