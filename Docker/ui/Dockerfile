FROM node:7-alpine

RUN apk update && apk add git
RUN git clone https://github.com/epam/Indigo-ELN-v.-2.0.git
WORKDIR Indigo-ELN-v.-2.0
RUN git checkout indigo-eln-ui
RUN npm install
RUN npm run build

CMD npm run start:prod -- --env.apiUrl=${INDIGOELN_SERVER} --host 0.0.0.0
