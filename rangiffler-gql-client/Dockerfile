# --- build (Debian) ---
FROM node:22-bookworm AS build
ARG NPM_COMMAND=build:docker
WORKDIR /app

# быстрее и детерминированнее
COPY package*.json ./
RUN npm ci --no-audit --no-fund

# исходники
COPY . ./
ENV NODE_ENV=production
RUN npm run ${NPM_COMMAND}

# --- release (nginx alpine) ---
FROM nginx:1.27.1-alpine AS release
LABEL maintainer="Sentidas"
ENV TZ=Europe/Moscow
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]