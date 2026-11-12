# syntax=docker/dockerfile:1

FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_KEYCLOAK_URL=http://keycloak.localhost:8080
ARG VITE_KEYCLOAK_REALM=agriculture
ARG VITE_KEYCLOAK_CLIENT_ID=react-client
ARG VITE_REFRIGERATOR_ENDPOINT=/refrigerator/fruits
ARG VITE_ORDERS_ENDPOINT=/api/orders
ARG VITE_RECLAMATIONS_ENDPOINT=/api/reclamations
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL \
    VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM \
    VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID \
    VITE_REFRIGERATOR_ENDPOINT=$VITE_REFRIGERATOR_ENDPOINT \
    VITE_ORDERS_ENDPOINT=$VITE_ORDERS_ENDPOINT \
    VITE_RECLAMATIONS_ENDPOINT=$VITE_RECLAMATIONS_ENDPOINT

RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
