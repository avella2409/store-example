import Keycloak from "keycloak-js";
import { ProductInfo, ProductInfoGateway } from "../ProductInfoGateway";

export class RemoteProductInfoGateway implements ProductInfoGateway {

    private readonly keycloak: Keycloak;
    private readonly baseUrl: string;

    constructor(keycloak: Keycloak, baseUrl: string) {
        this.keycloak = keycloak;
        this.baseUrl = baseUrl;
    }

    token(): Promise<string> {
        return this.keycloak.updateToken(10)
            .then(_ => this.keycloak.token ?? "");
    }

    findInfo(id: string): Promise<ProductInfo> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/info/${id}`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })
            })
            .then(response => response.json())
    }

    update(id: string, info: ProductInfo): Promise<void> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/update/${id}`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        name: info.name,
                        description: info.description
                    })
                })
            })
            .then(_ => { })
    }
}