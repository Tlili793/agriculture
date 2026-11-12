import { apiRequest } from './client';

const RECLAMATIONS_ENDPOINT = import.meta.env.VITE_RECLAMATIONS_ENDPOINT ?? '/api/reclamations';

export function createReclamation({ token, keycloak, reclamation }) {
  return apiRequest(RECLAMATIONS_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: reclamation,
  });
}

export function fetchUserReclamations({ token, keycloak, userId }) {
  return apiRequest(`${RECLAMATIONS_ENDPOINT}/user/${userId}`, { token, keycloak });
}

export function fetchAllReclamations({ token, keycloak }) {
  return apiRequest(RECLAMATIONS_ENDPOINT, { token, keycloak });
}

export function fetchReclamation({ token, keycloak, id }) {
  return apiRequest(`${RECLAMATIONS_ENDPOINT}/${id}`, { token, keycloak });
}

export function updateReclamationStatus({ token, keycloak, id, status, adminResponse }) {
  return apiRequest(`${RECLAMATIONS_ENDPOINT}/${id}/status`, {
    token,
    keycloak,
    method: 'PUT',
    body: { status, adminResponse },
  });
}

export function deleteReclamation({ token, keycloak, id }) {
  return apiRequest(`${RECLAMATIONS_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}
