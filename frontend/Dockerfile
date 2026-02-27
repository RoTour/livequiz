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

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/livequiz-frontend/browser /usr/share/nginx/html

EXPOSE 80
