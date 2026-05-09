FROM node:16-alpine

WORKDIR /app

# 复制 package.json
COPY package*.json ./

# 设置 npm 镜像
RUN npm config set registry https://registry.npmmirror.com

# 安装依赖
RUN npm install

# 复制源代码
COPY . .

# 暴露端口
EXPOSE 80

# 启动开发服务器
CMD ["npm", "run", "serve"]
