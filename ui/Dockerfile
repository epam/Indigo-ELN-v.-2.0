FROM node:7-alpine as builder

ARG INDIGOELN_SERVER

RUN apk update
RUN apk add git
RUN mkdir /sources
WORKDIR /sources

COPY package.json /sources/
COPY package-lock.json /sources/
RUN npm install --silent
COPY . /sources/
RUN npm run build --env.apiUrl=$INDIGOELN_SERVER

FROM nginx:1.27
COPY --from=builder /sources/dist/* /usr/share/nginx/html/
