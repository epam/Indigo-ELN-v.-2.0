FROM node:7-alpine

RUN apk update
RUN apk add git
RUN mkdir /sources
WORKDIR /sources

COPY package.json /sources/
COPY package-lock.json /sources/
RUN npm install
COPY . /sources/
RUN npm run build
RUN ls -lR

CMD npm run start:prod -- --env.apiUrl=${INDIGOELN_SERVER} --host 0.0.0.0
