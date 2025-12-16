# 大模型输出数据与标准数据结构对照文档
# AI Model Output Data Mapping Documentation

## 概述
- **文档目的**: 对比分析大模型输出数据与标准化数据结构的对应关系
- **适用范围**: 数据转换层开发、前后端数据对接
- **版本**: v1.0
- **最后更新**: 2024-10-17

## 模块对照总览

| 模块名称 | 大模型字段路径 | 标准数据结构 | 对应关系 | 缺失字段 |
|---------|---------------|-------------|----------|----------|
| 冷线索 | `cold_leads` | `ColdLeadsData` | ✅ 完整 | 状态标识 |
| 热线索 | `hot_leads` | `HotLeadsData` | ✅ 完整 | 趋势标识 |
| AI销售助手 | `ai_sales_assistant` | `AISalesData` | ✅ 完整 | 单位定义 |
| 找客雷达 | `customer_radar` | `RadarAcquisitionData` | ✅ 完整 | 排名信息 |
| 直播 | `live_streaming` | `LiveStreamingData` | ✅ 完整 | 状态管理 |
| 商机一码通 | `business_opportunity_qr` | `BusinessQRData` | ✅ 完整 | 趋势分析 |

## 详细字段对照

### 1. 冷线索模块 (Cold Leads)

#### 大模型输出结构
```json
{
  "cold_leads": {
    "overall": {
      "description": "本周中部大区共计领取 + 分配 1250 次冷线索，查看 1080 人次，查看率 86.40%，查看率 < 90% 人数 28 人。",
      "num1": "1250"
    },
    "business_units": {
      "highest_low_view_rate_count": "智能硬件事业部（9 人）",
      "lowest_view_rate": "企业服务事业部（78.50%）"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `overall.description` | 整体描述 | `ColdLeadsData.overall` | 文本 | 提取数值信息 |
| `overall.num1` | 分配总数 | `totalAllocated.value` | 数字 | 直接转换 |
| `business_units.highest_low_view_rate_count` | 最差事业部 | `keyProblems.worstUnit` | 文本 | 提取事业部名称 |
| `business_units.lowest_view_rate` | 最差团队 | `keyProblems.worstTeam` | 文本 | 提取团队信息 |

#### 转换示例
```typescript
// 大模型数据
const aiData = {
  overall: {
    description: "本周中部大区共计领取 + 分配 1250 次冷线索，查看 1080 人次，查看率 86.40%",
    num1: "1250"
  }
};

// 转换后标准数据
const standardData = {
  overall: {
    totalAllocated: { value: 1250, status: "normal", unit: "次" },
    totalViewed: { value: 1080, status: "normal", unit: "人次" },
    viewRate: { value: 86.4, percentage: 86.4, status: "warning" }
  }
};
```

### 2. 热线索模块 (Hot Leads)

#### 大模型输出结构
```json
{
  "hot_leads": {
    "section_2_1": {
      "overall": "本周中部大区热线索 6H 未查看 15 人，未查看客户数 32 人。",
      "business_units": "6H 未查看客户数最多：智能硬件事业部（12 人）"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `section_2_1.overall` | 6H未查看情况 | `HotLeadsData.overall.unviewedIn6H` | 文本 | 提取数值信息 |
| `section_2_1.business_units` | 最严重事业部 | `urgentProblems.seriousUnit` | 文本 | 提取事业部名称 |
| `section_2_1.general_manager_teams` | 最严重团队 | `urgentProblems.seriousTeam` | 文本 | 提取团队信息 |

### 3. AI销售助手模块 (AI Sales Assistant)

#### 大模型输出结构
```json
{
  "ai_sales_assistant": {
    "section_3_1": {
      "overall": "截至 11 月 3 日，中部大区共计托管 8500 万人，人均托管 56.67 人",
      "activation": "激活人数 135 人；共有 25 人已注册但仍未托管，有 18 人未注册"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `section_3_1.overall` | 托管情况 | `AISalesData.overall.totalHosted` | 文本 | 提取托管人数 |
| `section_3_1.activation` | 激活情况 | `AISalesData.overall.activatedUsers` | 文本 | 提取激活人数 |
| `business_units_3_2.lowest_wechat_trusteeship` | 最低托管 | `byBusinessUnit.hostedCount` | 文本 | 提取数值信息 |

### 4. 找客雷达获客模块 (Customer Acquisition Radar)

#### 大模型输出结构
```json
{
  "customer_radar": {
    "section_4_1": {
      "overall": "本月中部大区找客雷达累计获客 850 人，人均获客 5.67 人",
      "entrepreneur_customers": "企业家获客 340 人（占比 40.00%），未达标人数 42 人"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `section_4_1.overall` | 获客情况 | `RadarAcquisitionData.overall.totalAcquired` | 文本 | 提取获客数 |
| `section_4_1.entrepreneur_customers` | 企业家获客 | `RadarAcquisitionData.overall.entrepreneurPercentage` | 文本 | 提取占比信息 |

### 5. 直播模块 (Live Streaming)

#### 大模型输出结构
```json
{
  "live_streaming": {
    "section_5_1": {
      "overall": "本周中部大区各类型直播共计产生观看客户数 1250 人",
      "middle_library_viewers": "其中中间库观看客户数 480 人，人均中间库观看客户数 3.20 人"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `section_5_1.overall` | 观看总数 | `LiveStreamingData.overall.totalViewers` | 文本 | 提取观看数 |
| `section_5_1.middle_library_viewers` | 中间库观看 | `LiveStreamingData.overall.middleLibraryViewers` | 文本 | 提取观看数 |

### 6. 商机一码通模块 (Business QR Code)

#### 大模型输出结构
```json
{
  "business_opportunity_qr": {
    "section_6_1": {
      "overall": "本周中部大区通过商机一码通触达共计 920 个客户"
    }
  }
}
```

#### 字段对照表

| 大模型字段 | 中文含义 | 标准字段 | 数据类型 | 转换规则 |
|-----------|----------|----------|----------|----------|
| `section_6_1.overall` | 触达客户数 | `BusinessQRData.overall.totalReached` | 文本 | 提取触达数 |

## 缺失字段分析

### 1. 状态标识系统 (Status Indicators)

**大模型缺失字段**:
- `status`: `good`/`normal`/`warning`/`critical`
- `trend`: `up`/`stable`/`down`

**解决方案**:
```typescript
function calculateStatus(value: number, threshold: number): Status {
  if (value >= threshold * 1.1) return 'good';
  if (value >= threshold * 0.9) return 'normal';
  if (value >= threshold * 0.7) return 'warning';
  return 'critical';
}
```

### 2. 数值单位定义 (Unit Definitions)

**大模型缺失字段**:
- `unit`: 明确的数值单位

**解决方案**:
```typescript
const unitMappings = {
  '次': 'times',
  '人': 'people', 
  '人次': 'person-times',
  '%': 'percent'
};
```

### 3. 标准化错误处理 (Error Handling)

**大模型缺失字段**:
- 数据验证机制
- 错误恢复策略

**解决方案**:
```typescript
interface ValidationResult {
  isValid: boolean;
  errors: string[];
  warnings: string[];
}
```

## 数据转换策略

### 1. 文本到数值转换
```typescript
function extractNumberFromText(text: string): number {
  const match = text.match(/\d+(?:\.\d+)?/);
  return match ? parseFloat(match[0]) : 0;
}
```

### 2. 状态自动计算
```typescript
function autoCalculateStatus(
  value: number, 
  thresholds: { good: number; warning: number; critical: number }
): Status {
  if (value >= thresholds.good) return 'good';
  if (value >= thresholds.warning) return 'normal';
  if (value >= thresholds.critical) return 'warning';
  return 'critical';
}
```

### 3. 完整转换函数示例
```typescript
function convertAIModelToStandard(aiData: AIModelOutput): MarketingWeeklyReport {
  return {
    metadata: {
      title: aiData.title,
      reportDate: extractDate(aiData.title),
      region: "中部大区"
    },
    modules: {
      coldLeads: convertColdLeads(aiData.cold_leads),
      hotLeads: convertHotLeads(aiData.hot_leads),
      // ... 其他模块转换
    }
  };
}
```

## 总结

### 大模型输出优势
1. **数据结构完整** - 6个主要模块都有完整数据
2. **业务语义清晰** - 字段命名贴近实际业务
3. **数据内容丰富** - 包含数值、文本、表格等多种形式
4. **层级结构合理** - 整体到细节的层次分明

### 需要补充的标准化字段
1. **状态管理系统** - 自动计算数据状态
2. **趋势分析标识** - 基于历史数据的趋势判断  
3. **单位标准化** - 统一的数值单位定义
4. **错误处理机制** - 数据验证和恢复策略

### 推荐实施方案
1. **开发数据转换层** - 专门处理大模型数据到标准格式的转换
2. **实现状态计算器** - 基于业务规则自动计算状态标识
3. **建立验证机制** - 确保数据质量和一致性
4. **提供转换工具** - 便于团队使用和维护

这个对照文档为数据转换层的开发提供了完整的参考依据，确保大模型输出能够无缝对接标准化数据结构。