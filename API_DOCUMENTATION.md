# 周报AI系统 - API 文档

## 任务状态管理 API

### 1. 检查任务状态

**端点**: `GET /api/task/status`

**描述**: 检查是否有用户正在执行任务

**请求参数**: 无

**响应示例** (有任务在执行):
```json
{
  "code": 0,
  "isRunning": true,
  "message": "有用户正在处理中部大区的数据，请稍候再试",
  "region": "中部大区",
  "elapsedSeconds": 45
}
```

**响应示例** (无任务在执行):
```json
{
  "code": 0,
  "isRunning": false
}
```

**HTTP状态码**: 200

**用途**: 前端在上传前调用，根据 `isRunning` 判断是否允许用户上传

**前端调用示例**:
```typescript
const response = await axios.get('http://127.0.0.1:9696/api/task/status')
if (response.data.isRunning) {
  alert(response.data.message)
  return
}
// 继续上传流程
```

---

## 文件上传 API

### 2. 上传并处理文件

**端点**: `POST /api/upload`

**描述**: 上传Excel文件并同步到飞书电子表格

**请求参数**:
| 参数名 | 类型 | 必需 | 说明 |
|-------|------|------|------|
| files | File[] | 是 | 要上传的Excel文件数组 |
| region | string | 否 | 大区名称（默认: "未知区域"） |

**请求示例** (FormData):
```javascript
const formData = new FormData()
formData.append('files', file1)
formData.append('files', file2)
formData.append('region', '中部大区')

axios.post('http://127.0.0.1:9696/api/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

**成功响应** (code=200):
```json
{
  "code": 200,
  "msg": "飞书接口调用成功",
  "data": null
}
```

**错误响应 - 有任务在执行**:
```json
{
  "code": 500,
  "msg": "系统正在处理其他用户的任务，请稍后再试",
  "data": null
}
```

**错误响应 - 文件验证失败**:
```json
{
  "code": 500,
  "msg": "请上传正确的文件，错误文件名：test.xlsx",
  "data": null
}
```

**HTTP状态码**: 200（无论成功还是失败）

**工作流程**:
1. 后端调用 `taskStatusManager.startTask(region)`
2. 如果返回 false（有任务在执行），返回错误
3. 如果返回 true，开始处理文件
4. 使用 try-finally 确保最后调用 `taskStatusManager.completeTask()`

---

## 报告生成 API

### 3. 生成周报

**端点**: `POST /api/generate-report`

**描述**: 调用Coze AI工作流生成周报

**请求参数**:
| 参数名 | 类型 | 必需 | 说明 |
|-------|------|------|------|
| region | string | 是 | 大区名称 |

**请求示例** (FormData):
```javascript
const formData = new FormData()
formData.append('region', '中部大区')

axios.post('http://127.0.0.1:9696/api/generate-report', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
```

**成功响应**:
```json
{
  "code": 200,
  "msg": "",
  "data": {
    "销售数据": {...},
    "AI分析": {...},
    "建议": {...}
  }
}
```

**错误响应**:
```json
{
  "code": 500,
  "msg": "调用Coze工作流失败",
  "data": null
}
```

**HTTP状态码**: 200

**特殊说明**: 
- 无论成功还是失败，都会在 finally 块中清除任务状态
- 这确保任务一定会被标记为完成，其他用户可以继续上传

---

## 允许的文件列表 API

### 4. 获取允许上传的文件名

**端点**: `GET /api/upload/allowed`

**描述**: 获取系统允许上传的文件名列表，用于前端校验和提示

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    "【老】30专线组服秘数据追踪.xlsx",
    "【新】30专线组服秘数据追踪.xlsx",
    "单场直播转发及观看看板（优先找客雷达）.xlsx",
    "观看直播统计.xlsx",
    "冷线索.xlsx",
    ...
  ]
}
```

**HTTP状态码**: 200

**用途**: 前端可以显示这个列表，指导用户上传正确的文件

---

## 系统验证 API

### 5. 系统健康检查

**端点**: `GET /api/validate`

**描述**: 验证系统是否正常运行

**请求参数**: 无

**成功响应**:
```json
{
  "code": 200,
  "msg": "The system is running!!!!",
  "data": null
}
```

**HTTP状态码**: 200

**用途**: 检查后端服务是否在线

---

## 错误代码说明

| 错误代码 | 说明 |
|---------|------|
| 200 | 成功 |
| 500 | 通用错误 |

---

## 并发场景流程图

### 正常流程 (用户A上传)
```
用户A上传
  ↓
GET /api/task/status
  ↓ (isRunning=false)
继续上传
  ↓
POST /api/upload (region=中部大区)
  ↓ (taskStatusManager.startTask() = true)
后端处理文件... [5-30秒]
  ↓
POST /api/generate-report
  ↓
清除任务状态 (finally块)
  ↓
用户A收到报告 ✓
```

### 并发阻止流程 (用户B在A执行中上传)
```
用户A正在执行中 (15秒已过)

用户B上传
  ↓
GET /api/task/status
  ↓ (isRunning=true, region=中部大区, elapsedSeconds=15)
弹出提示: "有用户正在处理中部大区的数据，请稍候再试"
  ↓
用户B取消/等待

... 用户A继续处理...

用户A完成后 (taskStatusManager.completeTask())
  ↓
用户B重新上传
  ↓
GET /api/task/status
  ↓ (isRunning=false)
继续上传 ✓
```

---

## 最佳实践

### 前端

1. **始终先检查状态**
```typescript
const hasRunningTask = await taskStatusStore.checkTaskStatus()
if (hasRunningTask) {
  alert(taskStatusStore.errorMessage)
  return
}
```

2. **显示友好的等待提示**
```typescript
if (response.data.isRunning) {
  alert(`有用户正在处理${response.data.region}的数据，已执行${response.data.elapsedSeconds}秒。`)
  alert('预计需要 5-10 分钟，请稍后再试。')
}
```

3. **实现重试机制**
```typescript
let retryCount = 0
const maxRetries = 5
const retryInterval = 10000 // 10秒

async function uploadWithRetry() {
  while (retryCount < maxRetries) {
    const hasTask = await taskStatusStore.checkTaskStatus()
    if (!hasTask) {
      // 上传
      break
    }
    retryCount++
    await new Promise(resolve => setTimeout(resolve, retryInterval))
  }
}
```

### 后端

1. **确保 try-finally 结构**
```java
if (!taskStatusManager.startTask(region)) {
  return AjaxResult.error("系统繁忙，请稍后再试");
}
try {
  // 处理业务逻辑
} finally {
  taskStatusManager.completeTask();
}
```

2. **处理异常时也要清除状态**
```java
try {
  invokeFeiShuInternal(files);
} catch (Exception e) {
  log.error("处理异常", e);
  throw e;
} finally {
  taskStatusManager.completeTask(); // 必须执行
}
```

---

## 监控和日志

### 关键日志输出
```
INFO  - processing-调用飞书接口-service开始
INFO  - invokeFeiShu: 成功标记任务开始, region=中部大区
...处理日志...
INFO  - invokeFeiShu: 任务完成，任务状态已清除
INFO  - postCozeWorkflow:任务状态已清除
```

### 异常日志
```
WARN  - invokeFeiShu: 有任务正在执行，拒绝新请求
ERROR - processing-调用飞书接口-异常: ...
```

---

## 性能建议

1. **缓存允许的文件列表**
   前端可以在初始化时调用 GET /api/upload/allowed 一次，然后本地缓存

2. **定期检查任务状态**
   如果用户被拒绝，可以每10秒自动检查一次，而不是让用户手动重试

3. **设置任务超时**
   如果任务执行超过2小时（可配置），自动清除状态，防止卡死

---

## 测试 cURL 命令

```bash
# 1. 检查任务状态
curl -X GET "http://127.0.0.1:9696/api/task/status"

# 2. 获取允许的文件列表
curl -X GET "http://127.0.0.1:9696/api/upload/allowed"

# 3. 上传文件（需要实际文件）
curl -X POST "http://127.0.0.1:9696/api/upload" \
  -F "files=@file1.xlsx" \
  -F "region=中部大区"

# 4. 生成报告
curl -X POST "http://127.0.0.1:9696/api/generate-report" \
  -F "region=中部大区"

# 5. 系统验证
curl -X GET "http://127.0.0.1:9696/api/validate"
```
