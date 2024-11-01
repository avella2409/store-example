import Keycloak from "keycloak-js";
import { Product, ProductGateway } from "../ProductGateway";

export class RemoteProductGateway implements ProductGateway {

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

    publish(productId: string): Promise<void> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/publish`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        productId
                    })
                })
            })
            .then(_ => { })
    }
    archive(productId: string): Promise<void> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/archive`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        productId
                    })
                })
            })
            .then(_ => { })
    }

    create(): Promise<string> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/create`, {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({})
                })
            })
            .then(response => response.text())
    }

    findAll(): Promise<Product[]> {
        return this.token()
            .then(token => {
                return fetch(`${this.baseUrl}/product/findAll`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })
            })
            .then(response => response.json())
    }

}