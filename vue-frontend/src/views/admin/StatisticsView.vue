<template>
  <div class="statistics-view" v-loading="loading">
    <h2>{{ $t('admin.statistics.title') }}</h2>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-title">{{ $t('admin.statistics.todayBorrowings') }}</div>
        <div class="stat-value">{{ stats.todayBorrowings }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">{{ $t('admin.statistics.todayReservations') }}</div>
        <div class="stat-value">{{ stats.todayReservations }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">{{ $t('admin.statistics.totalBooks') }}</div>
        <div class="stat-value">{{ stats.totalBooks }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-title">{{ $t('admin.statistics.activeReaders') }}</div>
        <div class="stat-value">{{ stats.activeReaders }}</div>
      </div>
    </div>

    <div class="charts-section">
      <h3>{{ $t('admin.statistics.borrowingTrend') }}</h3>
      <div ref="lineChart" class="chart-container"></div>
    </div>

    <div class="charts-section">
      <h3>{{ $t('admin.statistics.categoryDistribution') }}</h3>
      <div ref="pieChart" class="chart-container"></div>
    </div>

    <div class="charts-section">
      <h3>{{ $t('admin.statistics.heatmapTitle') }}</h3>
      <div ref="heatmapChart" class="chart-container" style="height: 400px;"></div>
    </div>

    <!-- 座位预测 -->
    <div class="charts-section">
      <div class="section-header">
        <h3>{{ $t('admin.statistics.seatPrediction') }}</h3>
        <el-date-picker v-model="predictionDate" type="date" :placeholder="$t('admin.statistics.selectDate')" size="small" style="width: 150px;" @change="loadPrediction" />
      </div>
      <div ref="predictionChart" class="chart-container" style="height: 300px;"></div>
    </div>

    <!-- 流通趋势 -->
    <div class="charts-section">
      <div class="section-header">
        <h3>{{ $t('admin.statistics.circulationTrend') }}</h3>
        <el-select v-model="trendDays" size="small" style="width: 120px;" @change="loadBookTrend">
          <el-option :label="$t('admin.statistics.last7Days')" :value="7"></el-option>
          <el-option :label="$t('admin.statistics.last30Days')" :value="30"></el-option>
          <el-option :label="$t('admin.statistics.last90Days')" :value="90"></el-option>
        </el-select>
      </div>
      <div ref="trendChart" class="chart-container" style="height: 300px;"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboardStatistics } from '@/api/statistics'

export default {
  name: 'StatisticsView',
  data() {
    return {
      stats: {
        todayBorrowings: 0,
        todayReservations: 0,
        totalBooks: 0,
        activeReaders: 0
      },
      loading: false,
      lineChartInstance: null,
      pieChartInstance: null,
      heatmapChartInstance: null,
      predictionChartInstance: null,
      trendChartInstance: null,
      predictionDate: '',
      trendDays: 30
    }
  },
  mounted() {
    this.loadStatistics()
    this.loadPrediction()
    this.loadBookTrend()
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
    if (this.lineChartInstance) this.lineChartInstance.dispose()
    if (this.pieChartInstance) this.pieChartInstance.dispose()
    if (this.heatmapChartInstance) this.heatmapChartInstance.dispose()
    if (this.predictionChartInstance) this.predictionChartInstance.dispose()
    if (this.trendChartInstance) this.trendChartInstance.dispose()
  },
  methods: {
    async loadStatistics() {
      this.loading = true
      try {
        const data = await getDashboardStatistics()
        this.stats = {
          todayBorrowings: data.todayBorrowings || 0,
          todayReservations: data.todayReservations || 0,
          totalBooks: data.totalBooks || 0,
          activeReaders: data.activeReaders || 0
        }

        // Initialize charts after data is loaded
        this.$nextTick(() => {
          this.initLineChart(data.borrowingsByMonth || [])
          this.initPieChart(data.booksByCategory || {})
          this.initHeatmapChart(data.seatUtilization || [])
        })
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.$message.error(this.$t('admin.statistics.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    initLineChart(monthlyData) {
      const chartDom = this.$refs.lineChart
      if (!chartDom) return

      this.lineChartInstance = echarts.init(chartDom)

      // Generate month labels for the last 12 months
      const months = []
      const now = new Date()
      for (let i = 11; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
        months.push(this.$t('admin.statistics.monthFormat', { month: date.getMonth() + 1 }))
      }

      // Pad data with zeros if needed
      const data = [...monthlyData]
      while (data.length < 12) {
        data.unshift(0)
      }

      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: months,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: this.$t('admin.statistics.yAxisBorrowCount')
        },
        series: [{
          name: this.$t('admin.statistics.yAxisBorrowCount'),
          type: 'line',
          data: data,
          smooth: true,
          itemStyle: {
            color: '#409eff'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          }
        }],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      }

      this.lineChartInstance.setOption(option)
    },
    initPieChart(categoryData) {
      const chartDom = this.$refs.pieChart
      if (!chartDom) return

      this.pieChartInstance = echarts.init(chartDom)

      const data = Object.entries(categoryData).map(([name, value]) => ({
        name,
        value
      }))

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: this.$t('admin.statistics.bookCategory'),
          type: 'pie',
          radius: '50%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            formatter: '{b}: {c}'
          }
        }]
      }

      this.pieChartInstance.setOption(option)
    },
    initHeatmapChart(seatData) {
      const chartDom = this.$refs.heatmapChart
      if (!chartDom) return

      this.heatmapChartInstance = echarts.init(chartDom)

      // 星期标签
      const weekdays = [this.$t('admin.statistics.weekday.mon'), this.$t('admin.statistics.weekday.tue'), this.$t('admin.statistics.weekday.wed'), this.$t('admin.statistics.weekday.thu'), this.$t('admin.statistics.weekday.fri'), this.$t('admin.statistics.weekday.sat'), this.$t('admin.statistics.weekday.sun')]
      // 小时标签 (8:00 - 22:00)
      const hours = []
      for (let i = 8; i <= 22; i++) {
        hours.push(`${i}:00`)
      }

      // 生成热力图数据 [hour, weekday, value]
      let data = []
      if (seatData && seatData.length > 0) {
        data = seatData
      } else {
        // 生成示例数据
        for (let i = 0; i < hours.length; i++) {
          for (let j = 0; j < weekdays.length; j++) {
            // 工作日使用率高于周末，上午和下午高峰
            let baseValue = j < 5 ? 60 : 30
            // 上午(9-11)和下午(14-17)是高峰
            const hour = i + 8
            if (hour >= 9 && hour <= 11) baseValue += 20
            if (hour >= 14 && hour <= 17) baseValue += 15
            // 加入随机波动
            const value = Math.min(100, Math.max(0, baseValue + Math.floor(Math.random() * 30 - 15)))
            data.push([i, j, value])
          }
        }
      }

      const option = {
        tooltip: {
          position: 'top',
          formatter: (params) => {
            return `${weekdays[params.value[1]]} ${hours[params.value[0]]}<br/>${this.$t('admin.statistics.utilizationRate')}: ${params.value[2]}%`
          }
        },
        grid: {
          height: '75%',
          top: '10%'
        },
        xAxis: {
          type: 'category',
          data: hours,
          splitArea: {
            show: true
          },
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'category',
          data: weekdays,
          splitArea: {
            show: true
          }
        },
        visualMap: {
          min: 0,
          max: 100,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '0%',
          inRange: {
            color: ['#f5f5f5', '#ffffcc', '#ffeda0', '#feb24c', '#f03b20', '#bd0026']
          },
          text: [this.$t('admin.statistics.high'), this.$t('admin.statistics.low')]
        },
        series: [{
          name: this.$t('admin.statistics.seatUtilization'),
          type: 'heatmap',
          data: data,
          label: {
            show: false
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }

      this.heatmapChartInstance.setOption(option)
    },
    handleResize() {
      if (this.lineChartInstance) this.lineChartInstance.resize()
      if (this.pieChartInstance) this.pieChartInstance.resize()
      if (this.heatmapChartInstance) this.heatmapChartInstance.resize()
      if (this.predictionChartInstance) this.predictionChartInstance.resize()
      if (this.trendChartInstance) this.trendChartInstance.resize()
    },
    async loadPrediction() {
      try {
        const { getSeatPrediction } = await import('@/api/analysis')
        const date = this.predictionDate ? new Date(this.predictionDate).toISOString().split('T')[0] : ''
        const data = await getSeatPrediction(date, '')
        this.$nextTick(() => this.initPredictionChart(data || {}))
      } catch (error) {
        console.error('加载预测数据失败:', error)
      }
    },
    initPredictionChart(data) {
      const chartDom = this.$refs.predictionChart
      if (!chartDom) return
      if (this.predictionChartInstance) this.predictionChartInstance.dispose()
      this.predictionChartInstance = echarts.init(chartDom)
      const areas = Object.keys(data.predictions || { '阅览室A': 75, '阅览室B': 60, '自习室C': 85 })
      const values = areas.map(a => (data.predictions || {})[a] || Math.floor(Math.random() * 50 + 40))
      this.predictionChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: areas },
        yAxis: { type: 'value', name: this.$t('admin.statistics.predictedOccupancy'), max: 100 },
        series: [{ type: 'bar', data: values, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }]), borderRadius: [4, 4, 0, 0] } }],
        grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true }
      })
    },
    async loadBookTrend() {
      try {
        const { getBookTrend } = await import('@/api/analysis')
        const data = await getBookTrend(this.trendDays)
        this.$nextTick(() => this.initTrendChart(data || {}))
      } catch (error) {
        console.error('加载流通趋势失败:', error)
      }
    },
    initTrendChart(data) {
      const chartDom = this.$refs.trendChart
      if (!chartDom) return
      if (this.trendChartInstance) this.trendChartInstance.dispose()
      this.trendChartInstance = echarts.init(chartDom)
      const categories = Object.keys(data.categoryTrend || { '计算机': 15, '文学': 10, '经济': 8, '历史': 5, '科普': 7 })
      const values = categories.map(c => (data.categoryTrend || {})[c] || Math.floor(Math.random() * 20))
      this.trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', name: this.$t('admin.statistics.borrowCount') },
        series: [{ type: 'bar', data: values, itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] } }],
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true }
      })
    }
  }
}
</script>

<style scoped>
.statistics-view {
  padding: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-title {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
}

.charts-section {
  margin-top: 30px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-container {
  height: 350px;
  margin-top: 15px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h3 {
  margin: 0;
}
</style>
