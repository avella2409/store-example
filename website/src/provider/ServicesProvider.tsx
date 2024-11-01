import React from "react";
import keycloak from "../config/keycloak-config";
import { RemoteProductGateway } from "../service/impl/RemoteProductGateway";
import { RemoteProductInfoGateway } from "../service/impl/RemoteProductInfoGateway";
import { ProductGateway } from "../service/ProductGateway";
import { ProductInfoGateway } from "../service/ProductInfoGateway";

type ServicesProps = {
    productGateway: ProductGateway,
    productInfoGateway: ProductInfoGateway
}

export const ServicesContext = React.createContext<ServicesProps | undefined>(undefined);

export const useServices = () => {
    const props = React.useContext(ServicesContext);

    if (props === undefined) throw new Error("useServices must be used within a provider");

    return props;
}

export function ServicesProvider({ children }: { children: React.ReactNode }) {
    const kc = keycloak;
    const env = "customdevenv";
    const baseUrl = `${import.meta.env.VITE_GATEWAY_URL}/${env}/`;
    const productGateway = new RemoteProductGateway(kc, baseUrl + "merchant"); //  new FakeProductGateway;
    const productInfoGateway = new RemoteProductInfoGateway(kc, baseUrl + "productinfo"); // new FakeProductInfoGateway;

    const services: ServicesProps = {
        productGateway: productGateway,
        productInfoGateway: productInfoGateway
    }

    return (
        <ServicesContext.Provider value={services}>
            {children}
        </ServicesContext.Provider>
    )
}