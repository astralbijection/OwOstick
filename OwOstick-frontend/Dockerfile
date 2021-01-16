FROM node:14-alpine AS builder
WORKDIR /app
ENV NODE_ENV=production REACT_APP_HOST=api.astrid.tech:6970

COPY package.json yarn.lock ./
RUN yarn install

COPY . .
RUN yarn build

FROM nginx:alpine as server

RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/build/ /usr/share/nginx/html
