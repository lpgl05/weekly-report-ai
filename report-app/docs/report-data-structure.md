# 营销周报数据结构文档
# Marketing Weekly Report Data Structure Documentation

## 概述
- **文档目的**: 定义前端与后台数据同步的标准化数据结构
- **适用范围**: 营销周报系统的前后端数据交互
- **版本**: v1.0
- **最后更新**: 2024-10-17

## 基础类型定义

### 事业部信息 (Business Unit Information)
```typescript
interface BusinessUnit {
  name: string;        // 事业部名称
  code: string;        // 事业部代码
}
```

### 总经理团队信息 (General Manager Team Information)
```typescript
interface ManagerTeam {
  name: string;        // 团队名称
  manager: string;     // 总经理姓名
  businessUnit: string; // 所属事业部
}
```

### 数据统计指标 (Data Statistics Metrics)
```typescript
interface Metrics {
  value: number;       // 数值
  unit?: string;       // 单位
  percentage?: number;  // 百分比
  status: 'good' | 'normal' | 'warning' | 'critical'; // 状态
  trend?: 'up' | 'stable' | 'down'; // 趋势
}
```

## 各模块数据结构

### 1. 冷线索模块 (Cold Leads Module)

#### 数据结构
```typescript
interface ColdLeadsData {
  overall: {
    totalAllocated: Metrics;     // 分配总数
    totalViewed: Metrics;       // 查看总数
    viewRate: Metrics;          // 查看率
    belowStandardCount: Metrics; // 低于标准人数
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;      // 事业部
    allocatedCount: Metrics;   // 分配数量
    viewedCount: Metrics;      // 查看数量
    viewRate: Metrics;         // 查看率
    status: Metrics;           // 状态
  }>;
  
  keyProblems: {
    description: string;       // 问题描述
    worstUnit: string;        // 最差事业部
    worstTeam: string;        // 最差团队
  };
}
```

#### 示例数据
```json
{
  "overall": {
    "totalAllocated": { "value": 11362, "status": "normal" },
    "totalViewed": { "value": 8578, "status": "normal" },
    "viewRate": { "value": 75.5, "percentage": 75.5, "status": "warning" },
    "belowStandardCount": { "value": 110, "status": "critical" }
  },
  "byBusinessUnit": [
    {
      "businessUnit": "广东事业部",
      "allocatedCount": { "value": 4521, "status": "normal" },
      "viewedCount": { "value": 3456, "status": "normal" },
      "viewRate": { "value": 76.45, "percentage": 76.45, "status": "warning" },
      "status": { "value": 0, "status": "warning" }
    }
  ],
  "keyProblems": {
    "description": "所有事业部查看率均低于90%标准，西南事业部最低（68.95%），需重点督促员工及时查看冷线索。",
    "worstUnit": "西南事业部",
    "worstTeam": "马翠艳团队"
  }
}
```

### 2. 热线索模块 (Hot Leads Module)

#### 数据结构
```typescript
interface HotLeadsData {
  overall: {
    unviewedIn6H: Metrics;     // 6小时未查看客户数
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;      // 事业部
    unviewedCount: Metrics;    // 未查看数量
    percentage: Metrics;      // 占比
    mainTeam: string;         // 主要团队
    status: Metrics;           // 状态
  }>;
  
  urgentProblems: {
    description: string;       // 问题描述
    seriousUnit: string;       // 最严重事业部
    seriousTeam: string;      // 最严重团队
  };
}
```

#### 示例数据
```json
{
  "overall": {
    "unviewedIn6H": { "value": 1538, "status": "critical" }
  },
  "byBusinessUnit": [
    {
      "businessUnit": "广东事业部",
      "unviewedCount": { "value": 1476, "status": "critical" },
      "percentage": { "value": 95.97, "percentage": 95.97, "status": "critical" },
      "mainTeam": "王阿丽团队(南宁) 656人",
      "status": { "value": 0, "status": "critical" }
    }
  ],
  "urgentProblems": {
    "description": "广东事业部热线索响应严重滞后，王阿丽团队(南宁)未查看656人，需立即建立响应机制。",
    "seriousUnit": "广东事业部",
    "seriousTeam": "王阿丽团队"
  }
}
```

### 3. AI销售助手模块 (AI Sales Assistant Module)

#### 数据结构
```typescript
interface AISalesData {
  overall: {
    totalHosted: Metrics;          // 托管总人数
    avgHostedPerPerson: Metrics;  // 人均托管
    activatedUsers: Metrics;      // 激活人数
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;          // 事业部
    shouldUseCount: Metrics;      // 应使用人数
    registeredCount: Metrics;      // 已注册人数
    hostedCount: Metrics;        // 已托管人数
    hostedUsersCount: Metrics;    // 托管人数
    avgHostedPerPerson: Metrics;  // 人均托管
    activatedCount: Metrics;      // 激活数
  }>;
  
  usageAnalysis: {
    description: string;           // 分析描述
    unhostedUsers: number;        // 未托管人数
    unregisteredUsers: number;    // 未注册人数
  };
}
```

### 4. 找客雷达获客模块 (Customer Acquisition Radar Module)

#### 数据结构
```typescript
interface RadarAcquisitionData {
  overall: {
    totalAcquired: Metrics;        // 累计获客
    avgAcquisitionPerPerson: Metrics; // 人均获客
    entrepreneurPercentage: Metrics; // 企业家占比
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;          // 事业部
    usersCount: Metrics;           // 使用人数
    totalAcquired: Metrics;        // 获客总数
    avgAcquisitionPerPerson: Metrics; // 人均获客
    entrepreneurCount: Metrics;    // 企业家数量
    entrepreneurPercentage: Metrics; // 企业家占比
  }>;
  
  managerRankings: Array<{
    rank: number;                  // 排名
    managerName: string;          // 总经理姓名
    businessUnit: string;         // 事业部
    acquisitionCount: Metrics;     // 获客数量
    avgAcquisitionPerPerson: Metrics; // 人均获客
  }>;
  
  acquisitionAnalysis: {
    description: string;           // 分析描述
    worstUnit: string;             // 最差事业部
    worstTeam: string;             // 最差团队
  };
}
```

### 5. 直播模块 (Live Streaming Module)

#### 数据结构
```typescript
interface LiveStreamingData {
  overall: {
    totalViewers: Metrics;        // 总观看人数
    middleDbViewers: Metrics;    // 中间库人数
    otherViewers: Metrics;        // 其他观看人数
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;          // 事业部
    totalViewers: Metrics;        // 观看总数
    middleDbViewers: Metrics;      // 中间库人数
    otherViewers: Metrics;        // 其他观看人数
    middleDbPercentage: Metrics;   // 中间库占比
  }>;
  
  effectAnalysis: {
    description: string;           // 分析描述
    middleDbPercentage: number;   // 中间库占比
  };
}
```

### 6. 商机一码通模块 (Business QR Code Module)

#### 数据结构
```typescript
interface BusinessQRCodeData {
  overall: {
    qrCodeReach: Metrics;         // 二维码触达量
  };
  
  byBusinessUnit: Array<{
    businessUnit: string;          // 事业部
    reachCount: Metrics;          // 触达数量
    percentage: Metrics;           // 占比
    growthTrend: Metrics;         // 增长趋势
  }>;
  
  reachAnalysis: {
    description: string;           // 分析描述
    bestUnit: string;             // 最佳事业部
    worstUnit: string;             // 最差事业部
  };
}
```

### 7. 总结与建议模块 (Summary and Recommendations Module)

#### 数据结构
```typescript
interface SummaryRecommendations {
  recommendations: Array<{
    priority: 'high' | 'medium' | 'low'; // 优先级
    title: string;                 // 建议标题
    content: string;              // 建议内容
    expectedImpact: string;        // 预期影响
  }>;
}
```

## 完整报告数据结构

### 主结构
```typescript
interface MarketingWeeklyReport {
  metadata: {
    title: string;                // 报告标题
    reportDate: string;          // 报告日期
    period: {
      start: string;             // 开始日期
      end: string;               // 结束日期
    };
    generatedAt: string;         // 生成时间
    region: string;             // 大区信息
  };
  
  modules: {
    coldLeads: ColdLeadsData;    // 冷线索数据
    hotLeads: HotLeadsData;      // 热线索数据
    aiSales: AISalesData;        // AI销售助手数据
    radarAcquisition: RadarAcquisitionData; // 找客雷达获客数据
    liveStreaming: LiveStreamingData; // 直播数据
    businessQRCode: BusinessQRCodeData; // 商机一码通数据
  };
  
  summary: SummaryRecommendations; // 总结与建议
}
```

## API接口定义

### 获取报告数据

#### 请求参数
```typescript
interface GetReportRequest {
  regionCode: string;            // 大区代码
  reportDate: string;            // 报告日期
  period: {
    start: string;               // 开始日期
    end: string;                // 结束日期
  };
}
```

#### 响应结构
```typescript
interface GetReportResponse {
  success: boolean;              // 响应状态
  message: string;               // 响应消息
  data: MarketingWeeklyReport;   // 报告数据
  timestamp: string;             // 时间戳
}
```

### 重新生成报告

#### 请求参数
```typescript
interface RegenerateReportRequest {
  regionCode: string;            // 大区代码
  reportDate: string;            // 报告日期
  period: {
    start: string;               // 开始日期
    end: string;                // 结束日期
  };
  options?: {
    includeDetails: boolean;     // 是否包含详细数据
    refreshLevel: 'full' | 'incremental'; // 数据刷新级别
  };
}
```

#### 响应结构
```typescript
interface RegenerateReportResponse {
  success: boolean;              // 响应状态
  message: string;               // 响应消息
  reportId: string;              // 报告ID
  status: 'pending' | 'processing' | 'completed' | 'failed'; // 生成状态
  estimatedCompletion?: string;  // 预计完成时间
  timestamp: string;              // 时间戳
}
```

## 状态码定义

### 数据状态 (Data Status)
- `good` - 良好: 数据表现优秀，无需特别关注
- `normal` - 正常: 数据表现正常，符合预期
- `warning` - 警告: 数据表现需要关注，存在改进空间
- `critical` - 严重: 数据表现严重，需要立即处理

### 趋势状态 (Trend Status)
- `up` - 上升: 数据呈现上升趋势
- `stable` - 稳定: 数据保持稳定
- `down` - 下降: 数据呈现下降趋势

### 生成状态 (Generation Status)
- `pending` - 等待中: 报告生成任务已提交，等待处理
- `processing` - 处理中: 报告正在生成中
- `completed` - 已完成: 报告生成完成
- `failed` - 失败: 报告生成失败

## 使用说明

### 前端使用
1. 导入类型定义文件
2. 使用标准接口进行API调用
3. 根据状态码显示不同的UI样式
4. 处理错误和异常情况

### 后端使用
1. 按照数据结构组织数据
2. 确保数据格式符合规范
3. 提供标准的API响应
4. 处理数据验证和错误

### 数据验证
- 所有数值字段必须为有效数字
- 百分比字段范围应为0-100
- 状态字段必须为预定义值
- 日期字段必须符合ISO格式

## 更新日志

### v1.0 (2024-10-17)
- 初始版本发布
- 定义完整的营销周报数据结构
- 包含6个业务模块和总结建议模块
- 提供API接口定义和状态码规范