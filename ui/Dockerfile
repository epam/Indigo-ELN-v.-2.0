FROM node:7-alpine

RUN apk update
RUN apk add git
COPY . /sources/
WORKDIR /sources

RUN npm install
RUN npm run build

CMD npm run start:prod -- --env.apiUrl=${INDIGOELN_SERVER} --host 0.0.0.0
