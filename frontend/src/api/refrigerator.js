import { apiRequest } from './client';

const REFRIGERATOR_ENDPOINT = import.meta.env.VITE_REFRIGERATOR_ENDPOINT ?? '/refrigerator/fruits';

export function fetchFruits({ token, keycloak }) {
  return apiRequest(REFRIGERATOR_ENDPOINT, { token, keycloak });
}

export function createFruit({ token, keycloak, fruit }) {
  return apiRequest(REFRIGERATOR_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: fruit,
  });
}
