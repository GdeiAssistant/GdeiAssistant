# =============================================================================
# GdeiAssistant 前端镜像（Vue + Vite 构建 -> Nginx 托管）
# 生产构建时 baseURL 使用相对路径 /api，由 Nginx 代理到后端
# =============================================================================
FROM node:20-alpine AS builder
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
# 使用默认 /api，部署时由 Nginx 将 /api 代理到 gdeiassistant-backend
ENV VITE_APP_BASE_API=/api
RUN npm run build

# -----------------------------------------------------------------------------
# 运行阶段：Nginx 托管 dist
# -----------------------------------------------------------------------------
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
