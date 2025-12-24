import {useAuth} from "react-oidc-context";

export default function AuthToolbar() {
    const auth = useAuth();

    switch (auth.activeNavigator) {
        case "signinSilent":
            return <div>Signing you in...</div>;
        case "signoutRedirect":
            return <div>Signing you out...</div>;
    }

    if (auth.isLoading) {
        return <div>Logging in...</div>;
    }

    if (auth.error) {
        console.log(auth)
        return <div>Oops... {auth.error.message}</div>;
    }

    if (auth.isAuthenticated) {
        return (
            <div>
                Hello {auth.user?.profile.given_name} {auth.user?.profile.family_name}
                <button onClick={() => void auth.removeUser()}>Log out</button>
            </div>
        );
    }

    return <button onClick={() => void auth.signinRedirect()}>Log in</button>;
}
