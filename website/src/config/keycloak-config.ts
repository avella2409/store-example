import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
    url: `${import.meta.env.VITE_GATEWAY_URL}/auth`,
    realm: "avella",
    clientId: "store-client"
})

export default keycloak;