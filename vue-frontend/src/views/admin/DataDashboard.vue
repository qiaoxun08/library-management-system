<template>
  <div class="dashboard-screen">
    <!-- 顶部标题栏 -->
    <header class="screen-header">
      <div class="header-left">
        <el-button class="back-btn" type="primary" text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
      <h1 class="screen-title">
        <span class="title-decoration"></span>
        数据可视化大屏
        <span class="title-decoration"></span>
      </h1>
      <div class="header-right">
        <el-button class="fullscreen-btn" type="primary" text @click="toggleFullscreen">
          <el-icon><FullScreen /></el-icon>
        </el-button>
        <span class="refresh-time">刷新时间: {{ lastRefreshTime }}</span>
      </div>
    </header>

    <!-- 数据面板网格 -->
    <div class="dashboard-grid">
      <!-- 左上：实时在馆人数 -->
      <div class="panel panel-online">
        <div class="panel-header">
          <span class="panel-title">实时在馆人数</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body online-panel">
          <div class="online-count">
            <span class="count-number">{{ onlineCount }}</span>
            <span class="count-unit">人</span>
          </div>
          <div class="online-trend">
            <div ref="onlineTrendChart" class="mini-chart"></div>
          </div>
        </div>
      </div>

      <!-- 右上：今日借阅量趋势 -->
      <div class="panel panel-borrow-trend">
        <div class="panel-header">
          <span class="panel-title">今日借阅量趋势</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body">
          <div ref="borrowTrendChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 左中：座位占用率热力图 -->
      <div class="panel panel-seat-heatmap">
        <div class="panel-header">
          <span class="panel-title">座位占用率</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body">
          <div ref="seatHeatmapChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 右中：热门图书 TOP10 -->
      <div class="panel panel-hot-books">
        <div class="panel-header">
          <span class="panel-title">热门图书 TOP10</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body">
          <div ref="hotBooksChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 左下：借阅分类分布 -->
      <div class="panel panel-category">
        <div class="panel-header">
          <span class="panel-title">借阅分类分布</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body">
          <div ref="categoryChart" class="chart-container"></div>
        </div>
      </div>

      <!-- 右下：本月逾期率趋势 -->
      <div class="panel panel-overdue">
        <div class="panel-header">
          <span class="panel-title">本月逾期率趋势</span>
          <span class="panel-decoration"></span>
        </div>
        <div class="panel-body">
          <div ref="overdueChart" class="chart-container"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { warmTheme } from '@/styles/echarts-theme'
import { getRealtimeStatistics } from '@/api/statistics'

export default {
  name: 'DataDashboard',
  setup() {
    const router = useRouter()

    // 图表引用
    const onlineTrendChart = ref(null)
    const borrowTrendChart = ref(null)
    const seatHeatmapChart = ref(null)
    const hotBooksChart = ref(null)
    const categoryChart = ref(null)
    const overdueChart = ref(null)

    // 数据
    const onlineCount = ref(0)
    const lastRefreshTime = ref('')
    const onlineTrendData = ref([])

    // ECharts 实例
    let charts = {}
    let refreshTimer = null

    // 返回上一页
    const goBack = () => {
      router.push('/admin/home')
    }

    // 全屏切换
    const toggleFullscreen = () => {
      if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
      } else {
        document.exitFullscreen()
      }
    }

    // 更新刷新时间
    const updateRefreshTime = () => {
      const now = new Date()
      lastRefreshTime.value = now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    // 模拟在线人数趋势数据
    const generateOnlineTrend = () => {
      const data = []
      for (let i = 0; i < 12; i++) {
        data.push(Math.floor(Math.random() * 50) + 20)
      }
      return data
    }

    // 初始化实时在馆人数趋势图
    const initOnlineTrendChart = () => {
      if (!onlineTrendChart.value) return
      charts.onlineTrend = echarts.init(onlineTrendChart.value, warmTheme)

      const option = {
        grid: { top: 10, right: 10, bottom: 20, left: 30 },
        xAxis: {
          type: 'category',
          data: ['8:00', '9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00'],
          axisLabel: { color: '#8E99A4', fontSize: 10 },
          axisLine: { lineStyle: { color: '#2A3A4A' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#8E99A4', fontSize: 10 },
          splitLine: { lineStyle: { color: '#1A2A3A' } }
        },
        series: [{
          type: 'line',
          data: onlineTrendData.value,
          smooth: true,
          symbol: 'none',
          lineStyle: { color: '#C0785C', width: 2 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(192, 120, 92, 0.3)' },
              { offset: 1, color: 'rgba(192, 120, 92, 0.05)' }
            ])
          }
        }]
      }

      charts.onlineTrend.setOption(option)
    }

    // 初始化今日借阅量趋势图
    const initBorrowTrendChart = (data) => {
      if (!borrowTrendChart.value) return
      charts.borrowTrend = echarts.init(borrowTrendChart.value, warmTheme)

      // 生成24小时数据
      const hours = []
      const values = []
      for (let i = 8; i <= 21; i++) {
        hours.push(`${i}:00`)
        const found = data.find(item => item.hour === i)
        values.push(found ? found.count : 0)
      }

      const option = {
        grid: { top: 20, right: 20, bottom: 30, left: 40 },
        xAxis: {
          type: 'category',
          data: hours,
          axisLabel: { color: '#8E99A4', fontSize: 10, rotate: 30 },
          axisLine: { lineStyle: { color: '#2A3A4A' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#8E99A4', fontSize: 10 },
          splitLine: { lineStyle: { color: '#1A2A3A' } }
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(20, 30, 40, 0.9)',
          borderColor: '#2A3A4A',
          textStyle: { color: '#E8C4AC' }
        },
        series: [{
          type: 'line',
          data: values,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#D4A84B', width: 2 },
          itemStyle: { color: '#D4A84B' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(212, 168, 75, 0.3)' },
              { offset: 1, color: 'rgba(212, 168, 75, 0.05)' }
            ])
          }
        }]
      }

      charts.borrowTrend.setOption(option)
    }

    // 初始化座位占用率热力图
    const initSeatHeatmapChart = (data) => {
      if (!seatHeatmapChart.value) return
      charts.seatHeatmap = echarts.init(seatHeatmapChart.value, warmTheme)

      const areas = data.map(item => item.area)
      const values = data.map(item => ({
        value: item.total > 0 ? Math.round((item.occupied / item.total) * 100) : 0,
        total: item.total,
        occupied: item.occupied
      }))

      const option = {
        grid: { top: 20, right: 20, bottom: 30, left: 80 },
        xAxis: {
          type: 'value',
          max: 100,
          axisLabel: { color: '#8E99A4', fontSize: 10, formatter: '{value}%' },
          splitLine: { lineStyle: { color: '#1A2A3A' } }
        },
        yAxis: {
          type: 'category',
          data: areas,
          axisLabel: { color: '#8E99A4', fontSize: 11 },
          axisLine: { lineStyle: { color: '#2A3A4A' } }
        },
        tooltip: {
          formatter: function(params) {
            const d = values[params.dataIndex]
            return `${params.name}<br/>占用率: ${params.value}%<br/>已占用: ${d.occupied}/${d.total}`
          },
          backgroundColor: 'rgba(20, 30, 40, 0.9)',
          borderColor: '#2A3A4A',
          textStyle: { color: '#E8C4AC' }
        },
        visualMap: {
          min: 0,
          max: 100,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: 0,
          inRange: {
            color: ['#1A3A2A', '#3D6B4A', '#6B8F71', '#D4A84B', '#C0785C', '#A85454']
          },
          textStyle: { color: '#8E99A4' }
        },
        series: [{
          type: 'bar',
          data: values.map(v => v.value),
          barWidth: '60%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0]
          },
          label: {
            show: true,
            position: 'right',
            formatter: '{c}%',
            color: '#E8C4AC',
            fontSize: 11
          }
        }]
      }

      charts.seatHeatmap.setOption(option)
    }

    // 初始化热门图书TOP10图表
    const initHotBooksChart = (data) => {
      if (!hotBooksChart.value) return
      charts.hotBooks = echarts.init(hotBooksChart.value, warmTheme)

      const reversedData = [...data].reverse()
      const titles = reversedData.map(item => {
        const title = item.title || ''
        return title.length > 8 ? title.substring(0, 8) + '...' : title
      })
      const counts = reversedData.map(item => item.count)

      const option = {
        grid: { top: 10, right: 50, bottom: 10, left: 100 },
        xAxis: {
          type: 'value',
          axisLabel: { color: '#8E99A4', fontSize: 10 },
          splitLine: { lineStyle: { color: '#1A2A3A' } }
        },
        yAxis: {
          type: 'category',
          data: titles,
          axisLabel: { color: '#E8C4AC', fontSize: 11 },
          axisLine: { lineStyle: { color: '#2A3A4A' } }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(20, 30, 40, 0.9)',
          borderColor: '#2A3A4A',
          textStyle: { color: '#E8C4AC' }
        },
        series: [{
          type: 'bar',
          data: counts,
          barWidth: '50%',
          itemStyle: {
            borderRadius: [0, 4, 4, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#2C3E50' },
              { offset: 1, color: '#C0785C' }
            ])
          },
          label: {
            show: true,
            position: 'right',
            color: '#E8C4AC',
            fontSize: 11
          }
        }]
      }

      charts.hotBooks.setOption(option)
    }

    // 初始化借阅分类分布饼图
    const initCategoryChart = (data) => {
      if (!categoryChart.value) return
      charts.category = echarts.init(categoryChart.value, warmTheme)

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)',
          backgroundColor: 'rgba(20, 30, 40, 0.9)',
          borderColor: '#2A3A4A',
          textStyle: { color: '#E8C4AC' }
        },
        legend: {
          orient: 'vertical',
          right: 10,
          top: 'center',
          textStyle: { color: '#8E99A4', fontSize: 11 }
        },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#0f1923',
            borderWidth: 2
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              color: '#E8C4AC'
            }
          },
          data: data.map((item, index) => ({
            name: item.name,
            value: item.value,
            itemStyle: {
              color: [
                '#2C3E50', '#C0785C', '#6B8F71', '#D4A84B',
                '#5A7D9A', '#A85454', '#3D5A80', '#D4956B'
              ][index % 8]
            }
          }))
        }]
      }

      charts.category.setOption(option)
    }

    // 初始化本月逾期率趋势图
    const initOverdueChart = (data) => {
      if (!overdueChart.value) return
      charts.overdue = echarts.init(overdueChart.value, warmTheme)

      const dates = data.map(item => {
        const date = item.date || ''
        return date.substring(5) // 只显示 MM-DD
      })
      const rates = data.map(item => {
        const total = item.total || 0
        const overdue = item.overdue || 0
        return total > 0 ? Math.round((overdue / total) * 100 * 10) / 10 : 0
      })

      const option = {
        grid: { top: 20, right: 20, bottom: 30, left: 40 },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { color: '#8E99A4', fontSize: 10, rotate: 30 },
          axisLine: { lineStyle: { color: '#2A3A4A' } }
        },
        yAxis: {
          type: 'value',
          axisLabel: { color: '#8E99A4', fontSize: 10, formatter: '{value}%' },
          splitLine: { lineStyle: { color: '#1A2A3A' } }
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>逾期率: {c}%',
          backgroundColor: 'rgba(20, 30, 40, 0.9)',
          borderColor: '#2A3A4A',
          textStyle: { color: '#E8C4AC' }
        },
        series: [{
          type: 'line',
          data: rates,
          smooth: true,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: { color: '#A85454', width: 2 },
          itemStyle: { color: '#A85454' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(168, 84, 84, 0.3)' },
              { offset: 1, color: 'rgba(168, 84, 84, 0.05)' }
            ])
          }
        }]
      }

      charts.overdue.setOption(option)
    }

    // 加载数据
    const loadData = async () => {
      try {
        const data = await getRealtimeStatistics()

        // 更新在线人数
        onlineCount.value = data.onlineCount || 0

        // 更新在线趋势
        onlineTrendData.value = generateOnlineTrend()

        // 初始化/更新图表
        await nextTick()

        if (charts.onlineTrend) {
          charts.onlineTrend.setOption({
            series: [{ data: onlineTrendData.value }]
          })
        } else {
          initOnlineTrendChart()
        }

        if (charts.borrowTrend) {
          const hours = []
          const values = []
          for (let i = 8; i <= 21; i++) {
            hours.push(`${i}:00`)
            const found = (data.todayBorrowingsByHour || []).find(item => item.hour === i)
            values.push(found ? found.count : 0)
          }
          charts.borrowTrend.setOption({
            series: [{ data: values }]
          })
        } else {
          initBorrowTrendChart(data.todayBorrowingsByHour || [])
        }

        if (!charts.seatHeatmap) {
          initSeatHeatmapChart(data.seatHeatmap || [])
        }

        if (charts.hotBooks) {
          const reversedData = [...(data.hotBooks || [])].reverse()
          charts.hotBooks.setOption({
            yAxis: { data: reversedData.map(item => (item.title || '').length > 8 ? item.title.substring(0, 8) + '...' : item.title) },
            series: [{ data: reversedData.map(item => item.count) }]
          })
        } else {
          initHotBooksChart(data.hotBooks || [])
        }

        if (!charts.category) {
          initCategoryChart(data.categoryDistribution || [])
        }

        if (charts.overdue) {
          const rates = (data.overdueTrend || []).map(item => {
            const total = item.total || 0
            const overdue = item.overdue || 0
            return total > 0 ? Math.round((overdue / total) * 100 * 10) / 10 : 0
          })
          charts.overdue.setOption({
            series: [{ data: rates }]
          })
        } else {
          initOverdueChart(data.overdueTrend || [])
        }

        updateRefreshTime()
      } catch (error) {
        console.error('加载实时统计数据失败:', error)
      }
    }

    // 窗口大小变化时重绘图表
    const handleResize = () => {
      Object.values(charts).forEach(chart => {
        if (chart) chart.resize()
      })
    }

    onMounted(() => {
      loadData()

      // 30秒自动刷新
      refreshTimer = setInterval(loadData, 30000)

      // 监听窗口大小变化
      window.addEventListener('resize', handleResize)
    })

    onBeforeUnmount(() => {
      // 清理定时器
      if (refreshTimer) {
        clearInterval(refreshTimer)
      }

      // 移除事件监听
      window.removeEventListener('resize', handleResize)

      // 销毁图表实例
      Object.values(charts).forEach(chart => {
        if (chart) chart.dispose()
      })
    })

    return {
      onlineTrendChart,
      borrowTrendChart,
      seatHeatmapChart,
      hotBooksChart,
      categoryChart,
      overdueChart,
      onlineCount,
      lastRefreshTime,
      goBack,
      toggleFullscreen
    }
  }
}
</script>

<style scoped>
.dashboard-screen {
  width: 100vw;
  min-height: 100vh;
  background: #0f1923;
  color: #E8C4AC;
  overflow: auto;
}

/* 顶部标题栏 */
.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(180deg, rgba(20, 30, 40, 0.95) 0%, rgba(15, 25, 35, 0.8) 100%);
  border-bottom: 2px solid #C0785C;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.header-right {
  justify-content: flex-end;
}

.back-btn {
  color: #E8C4AC !important;
}

.back-btn:hover {
  color: #C0785C !important;
}

.screen-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #E8C4AC;
  text-align: center;
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-decoration {
  display: inline-block;
  width: 40px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #C0785C, transparent);
}

.fullscreen-btn {
  color: #E8C4AC !important;
  font-size: 20px;
}

.fullscreen-btn:hover {
  color: #C0785C !important;
}

.refresh-time {
  font-size: 12px;
  color: #8E99A4;
}

/* 数据面板网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(3, 1fr);
  gap: 16px;
  padding: 16px 24px;
  height: calc(100vh - 80px);
}

/* 面板通用样式 */
.panel {
  background: linear-gradient(135deg, rgba(20, 30, 40, 0.9) 0%, rgba(25, 35, 45, 0.8) 100%);
  border-radius: 8px;
  border: 1px solid rgba(192, 120, 92, 0.2);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 12px 16px;
  background: rgba(192, 120, 92, 0.1);
  border-bottom: 1px solid rgba(192, 120, 92, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #E8C4AC;
}

.panel-decoration {
  display: inline-block;
  width: 30px;
  height: 3px;
  background: linear-gradient(90deg, #C0785C, transparent);
  border-radius: 2px;
}

.panel-body {
  flex: 1;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  min-height: 0;
}

/* 实时在馆人数面板 */
.online-panel {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.online-count {
  flex: 0 0 40%;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
}

.count-number {
  font-size: 64px;
  font-weight: 700;
  color: #C0785C;
  line-height: 1;
  text-shadow: 0 0 20px rgba(192, 120, 92, 0.3);
}

.count-unit {
  font-size: 18px;
  color: #8E99A4;
}

.online-trend {
  flex: 1;
  height: 100%;
}

.mini-chart {
  width: 100%;
  height: 100%;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(6, auto);
    height: auto;
  }

  .panel {
    min-height: 300px;
  }

  .count-number {
    font-size: 48px;
  }
}

@media (max-width: 768px) {
  .screen-header {
    flex-direction: column;
    gap: 12px;
    padding: 12px 16px;
  }

  .header-left,
  .header-right {
    min-width: auto;
  }

  .screen-title {
    font-size: 18px;
  }

  .dashboard-grid {
    padding: 12px 16px;
    gap: 12px;
  }
}
</style>
