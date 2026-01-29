FROM --platform=linux/x86_64 node:22.14.0 AS build

WORKDIR /app

# Copy package.json and package-lock.json to the container
COPY package.json package-lock.json ./

# Install dependencies
RUN npm install

# Install Keycloak dependencies
RUN npm install keycloak-angular@19 keycloak-js

COPY . .

# Install Angular CLI globally to use `ng serve`
RUN npm install -g @angular/cli

# Expose the default Angular development server port
EXPOSE 4200

# Start the Angular development server
CMD ["ng", "serve", "--host", "0.0.0.0", "--port", "4200"]
