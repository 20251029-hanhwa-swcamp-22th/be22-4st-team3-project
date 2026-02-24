FROM node:lts-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# nginx 이미지를 사용하여 프로덕션 스테이지를 구성한다.
FROM nginx:stable-alpine AS production-stage
# 빌드 스테이지에서 생성된 정적 파일들을 Nginx의 기본 웹 서버 디렉토리로 복사한다.
# 이렇게 하면 Nginx가 Vue.js 애플리케이션의 빌드된 파일들을 서빙할 수 있게 된다.
COPY --from=build-stage /app/dist /usr/share/nginx/html

# 로컬 디렉토리의 nginx.conf 파일을 Nginx의 설정 디렉토리로 복사한다.
# 이 설정 파일은 Nginx의 동작을 커스터마이즈하는데 사용된다.
# 예를 들어, 라우팅 규칙, SSL 설정, 로깅 등을 정의할 수 있다.
COPY ./nginx.conf /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://boot-service:8001
EXPOSE 80
CMD ["/bin/sh", "-c", "envsubst '${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]