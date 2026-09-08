# ---- Etapa 1: Build ----
FROM node:20 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build

# ---- Etapa 2: Servidor (Nginx) ----
# Sin proxy /api/: el frontend le pega directo al api-gateway compartido de
# Buildr (environment.apiUrl, absoluto), no a un backend propio same-origin —
# ver src/environments/environment.prod.ts.
FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/buildrr-feedback-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
