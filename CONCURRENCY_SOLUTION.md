# 周报AI系统 - 并发任务防护方案实现总结

## 问题描述
当多个用户并发提交周报任务时，后续用户的数据会覆盖前面用户的处理结果，导致数据丢失。

## 解决方案概述
采用**简单的任务状态管理机制**：在用户上传文件时检查是否有其他用户正在执行任务，如果有则提示用户稍后再试。

## 实现细节

### 后端实现

#### 1. 任务状态管理器 (TaskStatusManager.java)
**位置**: `report-gateway/gateway-client/src/main/java/com/gateway/client/service/TaskStatusManager.java`

关键方法：
- `startTask(region)`: 尝试启动任务，返回是否成功（如果已有任务返回false）
- `completeTask()`: 标记任务完成
- `isTaskRunning()`: 查询当前是否有任务在执行
- `getCurrentTaskRegion()`: 获取当前任务的区域
- `getTaskElapsedSeconds()`: 获取任务已执行的时间

**特点**:
- 使用 `volatile` 关键字保证线程可见性
- 使用 `synchronized` 方法保证原子性
- 简单高效的内存存储（适合单服务器部署）

#### 2. 任务状态查询接口
**位置**: `UploadController.java` 的 `checkTaskStatus()` 方法

```
GET /api/task/status
```

**响应示例**:
```json
{
  "isRunning": true,
  "message": "有用户正在处理中部大区的数据，请稍候再试",
  "region": "中部大区",
  "elapsedSeconds": 45
}
```

#### 3. 上传接口改进
**位置**: `UploadController.java` 的 `invokeFeiShu()` 方法

增加了 `region` 参数，在 `DataProcessingServiceImpl.invokeFeiShu(files, region)` 中：
- 调用 `taskStatusManager.startTask(region)` 标记任务开始
- 如果有其他任务在执行，返回错误提示
- 使用 try-finally 确保任务完成时清除状态

#### 4. 生成报告接口改进
**位置**: `CozeWorkflowController.java` 的 `postCozeWorkflow()` 方法

- 在 finally 块中调用 `taskStatusManager.completeTask()` 
- 确保无论成功还是失败，都会清除任务状态

### 前端实现

#### 1. 任务状态Store (taskStatusStore.ts)
**位置**: `report-app/src/stores/taskStatusStore.ts`

关键功能：
- `checkTaskStatus()`: 调用后端API检查任务状态
- `isTaskRunning`: 存储任务运行状态
- `errorMessage`: 存储错误消息供UI展示

#### 2. 上传页面改进 (UploadView.vue)
**位置**: `report-app/src/views/UploadView.vue`

在 `confirmUpload()` 方法中：
```javascript
// 检查是否有任务正在执行
const hasRunningTask = await taskStatusStore.checkTaskStatus()

if (hasRunningTask) {
  alert(taskStatusStore.errorMessage)
  return
}

// 继续正常流程
```

#### 3. 进程页面 (ProcessView.vue)
已在 `submitToBackend()` 方法中正确传递 `region` 参数到后端。

## 工作流程

### 用户A (第一个上传)
1. **上传页面**: 点击"确认上传"
2. **前端**: 调用 `GET /api/task/status` → 返回 `isRunning=false` ✓
3. **前端**: 保存文件并跳转到处理页面
4. **处理页面**: 调用 `POST /api/upload` 并传递 `region` 参数
5. **后端**: `taskStatusManager.startTask("中部大区")` → 返回 `true` ✓
6. **后端**: 开始处理用户A的数据
7. **后端**: 完成数据处理，调用 `POST /api/generate-report`
8. **后端**: 生成报告完毕，调用 `taskStatusManager.completeTask()` ✓

### 用户B (并发上传，应被拒绝)
1. **上传页面**: 点击"确认上传"
2. **前端**: 调用 `GET /api/task/status` → 返回 `isRunning=true` ❌
3. **前端**: 显示提示："有用户正在处理中部大区的数据，请稍候再试"
4. **用户操作**: 稍后重试

## 数据流图

```
用户上传                前端                    后端
  ↓                     ↓                       ↓
点击上传 ----→ 检查任务状态 ----→ GET /api/task/status
                        ↓                       ↓
                    有任务？        返回 {isRunning: boolean}
                     /    \                     
                是/     \否
               /          \
           提示用户      继续上传 ----→ POST /api/upload (含region)
           稍后再试               ↓
                            标记任务开始
                          startTask(region)
                                 ↓
                            处理用户数据
                                 ↓
                         POST /api/generate-report
                                 ↓
                            清除任务状态
                          completeTask()
```

## 优势

1. **简单易维护**: 不需要复杂的分布式锁或消息队列
2. **实时生效**: 用户立即知道是否可以上传
3. **用户友好**: 明确提示说明哪个区域在处理，已处理时长
4. **资源高效**: 内存占用极少，无额外依赖

## 局限性和改进建议

### 当前局限
1. **单服务器限制**: 如果部署多个后端实例，需要分布式解决方案
2. **重启丢失**: 服务重启会清除任务状态
3. **异常崩溃**: 若后端异常退出，任务状态无法恢复

### 改进建议（可选）

#### 方案A: 引入Redis分布式锁（推荐）
```java
// 使用RedisTemplate实现分布式锁
private final RedisTemplate<String, String> redisTemplate;

public boolean startTask(String region) {
    Boolean result = redisTemplate.opsForValue()
        .setIfAbsent("task:running", region, Duration.ofHours(2));
    return Boolean.TRUE.equals(result);
}

public void completeTask() {
    redisTemplate.delete("task:running");
}
```

#### 方案B: 数据库存储任务状态
```java
// 使用数据库表存储任务状态
TaskStatus status = taskStatusRepository.findById("current_task");
if (status != null && status.isRunning()) {
    return false;
}
```

#### 方案C: 消息队列（最强大但复杂度高）
```java
// 使用MQ实现队列处理，一次只允许一个任务执行
// 所有上传请求入队，逐个处理
```

## 测试建议

### 测试场景1: 单用户上传
- 预期: ✓ 上传成功，任务正常完成

### 测试场景2: 两个用户快速上传
- 预期: 用户A上传成功，用户B看到提示"请稍候再试"

### 测试场景3: 并发压力测试
```bash
# 使用 Apache JMeter 或 wrk 进行压力测试
ab -n 100 -c 10 http://localhost:9696/api/task/status
```

### 测试场景4: 长时间任务监控
- 预期: 用户能看到任务已执行的时间逐秒增加

## 实现文件清单

### 后端 (Java)
- ✅ `TaskStatusManager.java` - 新增
- ✅ `UploadController.java` - 修改（添加checkTaskStatus接口，修改upload参数）
- ✅ `DataProcessingServiceImpl.java` - 修改（添加invokeFeiShu重载方法，管理任务状态）
- ✅ `CozeWorkflowController.java` - 修改（finally块中清除任务状态）
- ✅ `IDataProcessingService.java` - 修改（添加invokeFeiShu重载方法）

### 前端 (Vue3 + TypeScript)
- ✅ `taskStatusStore.ts` - 新增
- ✅ `UploadView.vue` - 修改（confirmUpload添加状态检查）
- ✅ `ProcessView.vue` - 无需修改（已在传递region参数）

## 配置说明

无需额外配置，代码开箱即用。

## 性能影响

- **额外请求**: 每次上传前多一个GET请求到 `/api/task/status`（~10ms）
- **内存占用**: 约 1KB 用于存储任务状态
- **CPU影响**: 忽略不计

## 监控指标

建议添加以下指标到日志或监控系统：
1. 任务状态查询次数
2. 被拒绝的上传请求数
3. 平均任务执行时间
4. 任务并发拒绝率
