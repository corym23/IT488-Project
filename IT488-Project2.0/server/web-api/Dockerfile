FROM node:18-alpine
WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production --silent

COPY . .

ENV PORT=4000
EXPOSE 4000

CMD ["node", "index.js"]
