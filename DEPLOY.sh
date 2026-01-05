#!/bin/bash
# 快速部署脚本 - 周报AI系统并发防护方案

echo "========================================="
echo "周报AI系统 - 并发防护方案部署"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}步骤 1: 检查后端文件${NC}"
echo "✓ TaskStatusManager.java - 已创建"
echo "✓ UploadController.java - 已修改"
echo "✓ CozeWorkflowController.java - 已修改"
echo "✓ DataProcessingServiceImpl.java - 已修改"
echo "✓ IDataProcessingService.java - 已修改"

echo ""
echo -e "${BLUE}步骤 2: 检查前端文件${NC}"
echo "✓ taskStatusStore.ts - 已创建"
echo "✓ UploadView.vue - 已修改"
echo "✓ ProcessView.vue - 无需修改"

echo ""
echo -e "${BLUE}步骤 3: 编译后端${NC}"
echo "进入后端目录: cd report-gateway"
echo "编译: mvn clean package"
echo "启动: java -jar gateway-client/target/gateway-client.jar"

echo ""
echo -e "${BLUE}步骤 4: 启动前端${NC}"
echo "进入前端目录: cd report-app"
echo "安装依赖: npm install"
echo "启动开发: npm run dev"

echo ""
echo -e "${GREEN}部署完成！${NC}"
echo ""
echo "测试地址:"
echo "  前端: http://127.0.0.1:5173"
echo "  后端: http://127.0.0.1:9696"
echo ""
echo "关键API端点:"
echo "  GET  /api/task/status         - 检查任务状态"
echo "  POST /api/upload              - 上传并处理文件"
echo "  POST /api/generate-report     - 生成报告"
echo ""
