FROM node:22.14.0 AS build

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci --ignore-scripts

COPY . .

EXPOSE 4200

CMD ["npm", "run", "start-local", "--", "--host", "0.0.0.0", "--port", "4200"]