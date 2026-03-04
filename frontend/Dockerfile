FROM node:22-alpine AS build

WORKDIR /app

COPY package.json ./
COPY bun.lock ./

RUN npm install

COPY angular.json ./
COPY tsconfig.json ./
COPY tsconfig.app.json ./
COPY .editorconfig ./
COPY .postcssrc.json ./
COPY public ./public
COPY src ./src

RUN npx ng build --configuration production

FROM nginx:1.27-alpine

ENV API_UPSTREAM=http://backend:8080

COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/livequiz-frontend/browser /usr/share/nginx/html

EXPOSE 80
