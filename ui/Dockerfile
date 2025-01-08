FROM node:7-alpine AS builder

ARG INDIGOELN_SERVER

RUN apk update
RUN apk add git
RUN mkdir /sources
WORKDIR /sources

COPY package.json /sources/
COPY package-lock.json /sources/
RUN --mount=type=cache,id=cache_npm,target=/root/.npm npm install --silent --prefer-offline
COPY . /sources/
RUN --mount=type=cache,id=cache_npm,target=/root/.npm npm run build

FROM nginx:1.27
COPY --from=builder /sources/dist/ /usr/share/nginx/html/
COPY indigoeln-ui-local.conf.template /etc/nginx/templates/default.conf.template
