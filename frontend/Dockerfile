FROM node:18-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN if [ -f package-lock.json ]; then npm ci --include=dev --no-audit --no-fund --progress=false; else npm install --include=dev --no-audit --no-fund --progress=false; fi

COPY . .
RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]