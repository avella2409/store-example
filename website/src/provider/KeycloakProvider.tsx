import Keycloak from "keycloak-js";
import React, { useEffect } from "react";
import keycloak from "../config/keycloak-config";

type KeycloakProps = {
    authenticated: boolean;
    keycloak: Keycloak;
}

export const KeycloakContext = React.createContext<KeycloakProps | undefined>(undefined);

export const useKeycloak = () => {
    const keycloakProps = React.useContext(KeycloakContext);

    if (keycloakProps === undefined) throw new Error("useKeycloak must be used with a provider");

    return keycloakProps;
};

export function KeycloakProvider({ children }: { children: React.ReactNode }) {
    const [props, setProps] = React.useState({
        authenticated: false,
        keycloak: keycloak
    })

    useEffect(() => {
        if (keycloak.didInitialize) return;

        keycloak.onAuthSuccess = () => {
            console.log("Auth success")
            setProps(old => ({ ...old, authenticated: true }));
        }
        keycloak.onAuthLogout = () => {
            console.log("Logout")
            setProps(old => ({ ...old, authenticated: false }));
        }
        keycloak.onAuthRefreshSuccess = () => {
            console.log("Auth refresh success")
        }

        keycloak.init({
            onLoad: 'login-required',
            redirectUri: 'http://localhost:5173/dashboard'
        })
            .catch(error => console.log(error));

    }, []);

    return (
        <KeycloakContext.Provider value={props}>
            {children}
        </KeycloakContext.Provider>
    )
}