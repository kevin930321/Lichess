# LinovelibReader - Android 輕小說閱讀器

基於 tw.linovelib.com 的 Android 輕小說閱讀器應用

## 項目信息

- **應用名稱**: 嗶哩輕小說  
- **Package 名稱**: com.linovelib.reader
- **最低 SDK**: 21 (Android 5.0)
- **目標 SDK**: 34

## 功能特點

- 瀏覽推薦小說列表
- 搜索小說
- 查看小說詳情（作者、插畫師、簡介等）
- 閱讀章節內容
- 收藏小說到書架
- 記錄閱讀歷史

## 技術棧

- **網路請求**: OkHttp 4.12.0
- **HTML 解析**: Jsoup 1.17.2
- **圖片加載**: Glide 4.16.0
- **UI**: AndroidX + Material Design
- **數據庫**: SQLite

## 項目結構

```
com.linovelib.reader/
├── model/          # 數據模型
├── api/            # 網路請求
├── parser/         # HTML 解析
├── database/       # 數據庫
├── activity/       # 活動頁面
├── fragment/       # 片段
├── adapter/        # 適配器
└── utils/          # 工具類
```

## 開發進度

**已完成**:
- ✅ 項目結構搭建
- ✅ Gradle 配置
- ✅ 數據模型層
- ✅ API 和 HTML 解析層
- ✅ 數據庫層（收藏、閱讀歷史）

**進行中**:
- 🚧 UI 實現（Activities/Fragments/Adapters）
- 🚧 佈局文件
- 🚧 功能集成

## 構建項目

1. 使用 Android Studio 打開項目
2. 等待 Gradle 同步完成
3. 連接 Android 設備或啟動模擬器
4. 點擊 Run

## 注意事項

- 本應用僅供學習交流使用
- 請尊重原網站的使用條款
- 建議添加適當的請求延遲以避免過度訪問

## License

MIT
