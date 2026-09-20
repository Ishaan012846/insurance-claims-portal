# Stage 1: Build stage with Node 20
FROM node:20-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Stage 2: Runtime stage with Nginx Alpine
FROM nginx:1.25-alpine

# Copy built static bundle from Node stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy Nginx configuration with reverse proxy rules
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
