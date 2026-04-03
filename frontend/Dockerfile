# =============================================================================
# GdeiAssistant 前端镜像（Vue + Vite 构建 -> Nginx 托管）
# 生产构建时 baseURL 使用相对路径 /api，由 Nginx 代理到后端
# =============================================================================
FROM node:24.14.1-alpine AS builder
WORKDIR /app
ARG VITE_APP_BASE_API=/api
ARG VITE_PE_EXTERNAL_URL=

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
# 生产构建变量统一由根目录 .env 传入；默认仍使用相对路径 /api。
ENV VITE_APP_BASE_API=${VITE_APP_BASE_API}
ENV VITE_PE_EXTERNAL_URL=${VITE_PE_EXTERNAL_URL}
RUN npm run build

# -----------------------------------------------------------------------------
# 运行阶段：Nginx 托管 dist
# -----------------------------------------------------------------------------
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
