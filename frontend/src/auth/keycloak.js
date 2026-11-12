import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL ?? 'http://keycloak.localhost:8080',
  realm: import.meta.env.VITE_KEYCLOAK_REALM ?? 'agriculture',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'react-client',
});

let initPromise;

export function initKeycloak() {
  if (!initPromise) {
    initPromise = keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false,
      redirectUri: `${window.location.origin}/welcome`,
    });
  }

  return initPromise;
}

export default keycloak;
