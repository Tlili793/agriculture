import { apiRequest } from './client';

const MARKETPLACE_ENDPOINT =
  import.meta.env.VITE_MARKETPLACE_ENDPOINT ?? '/api/marketplace/listings';

export function fetchListings({ token, keycloak } = {}) {
  return apiRequest(MARKETPLACE_ENDPOINT, { token, keycloak });
}

export function createListing({ token, keycloak, listing }) {
  return apiRequest(MARKETPLACE_ENDPOINT, {
    token,
    keycloak,
    method: 'POST',
    body: listing,
  });
}

export function updateListing({ token, keycloak, id, listing }) {
  return apiRequest(`${MARKETPLACE_ENDPOINT}/${id}`, {
    token,
    keycloak,
    method: 'PUT',
    body: listing,
  });
}

export function deleteListing({ token, keycloak, id, userId }) {
  const params = new URLSearchParams({ userId });

  return apiRequest(`${MARKETPLACE_ENDPOINT}/${id}?${params.toString()}`, {
    token,
    keycloak,
    method: 'DELETE',
  });
}
