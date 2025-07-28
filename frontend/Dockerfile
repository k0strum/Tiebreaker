# 1단계: 빌드 환경 (Node.js)
FROM node:18 AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# 2단계: 서빙 환경 (Nginx 웹서버)
FROM nginx:stable-alpine
# 빌드 단계에서 생성된 결과물(dist 폴더)을 Nginx의 기본 html 폴더로 복사
COPY --from=builder /app/dist /usr/share/nginx/html
# 컨테이너 80 포트 개방
EXPOSE 80
# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]