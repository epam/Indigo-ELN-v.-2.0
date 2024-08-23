FROM node:7-alpine as builder

RUN apk update
RUN apk add git
RUN mkdir /sources
WORKDIR /sources

COPY package.json /sources/
COPY package-lock.json /sources/
RUN npm install
COPY . /sources/
RUN npm run build

FROM nginx:1.27
COPY --from-builder /sources/dist/* /usr/share/nginx/html/
