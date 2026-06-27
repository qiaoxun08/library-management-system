<template>
  <div class="borrowing-history-view" v-loading="loading">
    <h2>{{ $t('reader.borrowingHistory.title') }}</h2>

    <!-- 借阅统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalBorrowings || 0 }}</div>
          <div class="stat-label">{{ $t('reader.borrowingHistory.totalBorrowings') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
          <el-icon :size="28"><Check /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.returnedCount || 0 }}</div>
          <div class="stat-label">{{ $t('reader.borrowingHistory.returned') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.currentBorrowings || 0 }}</div>
          <div class="stat-label">{{ $t('reader.borrowingHistory.currentBorrowings') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.thisMonthCount || 0 }}</div>
          <div class="stat-label">{{ $t('reader.borrowingHistory.thisMonth') }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card">
        <template #header>
          <span class="card-title">{{ $t('reader.borrowingHistory.monthlyChart') }}</span>
        </template>
        <div ref="monthlyChart" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <span class="card-title">{{ $t('reader.borrowingHistory.categoryChart') }}</span>
        </template>
        <div ref="categoryChart" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBorrowingsByReaderId } from '@/api/borrowing'

export default {
  name: 'BorrowingHistoryView',
  data() {
    return {
      stats: {},
      loading: false,
      monthlyChartInstance: null,
      categoryChartInstance: null
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
    if (this.monthlyChartInstance) {
      this.monthlyChartInstance.dispose()
    }
    if (this.categoryChartInstance) {
      this.categoryChartInstance.dispose()
    }
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const readerId = this.$store.getters.username
        if (!readerId) {
          this.$message.error(this.$t('reader.borrowingHistory.userNotFound'))
          return
        }
        const data = await getBorrowingsByReaderId(readerId)
        const records = Array.isArray(data) ? data : []

        // 从记录中计算统计值
        this.stats = {
          totalBorrowings: records.length,
          returnedCount: records.filter(r => r.status === 2).length,
          currentBorrowings: records.filter(r => r.status === 1).length,
          thisMonthCount: records.filter(r => {
            const d = new Date(r.borrowDate)
            const now = new Date()
            return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear()
          }).length
        }

        // 按月统计
        const monthlyData = {}
        records.forEach(r => {
          const d = new Date(r.borrowDate)
          const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
          monthlyData[key] = (monthlyData[key] || 0) + 1
        })

        // 按分类统计
        const categoryData = {}
        records.forEach(r => {
          const cat = r.category || this.$t('reader.borrowing.uncategorized')
          categoryData[cat] = (categoryData[cat] || 0) + 1
        })

        this.$nextTick(() => {
          this.initMonthlyChart(Object.entries(monthlyData).map(([k, v]) => ({ name: k, value: v })))
          this.initCategoryChart(categoryData)
        })
      } catch (error) {
        console.error('加载借阅历史失败:', error)
        this.$message.error(this.$t('reader.borrowingHistory.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    initMonthlyChart(monthlyData) {
      const chartDom = this.$refs.monthlyChart
      if (!chartDom) return

      this.monthlyChartInstance = echarts.init(chartDom)

      // 生成最近12个月的标签
      const months = []
      const monthKeys = []
      const now = new Date()
      for (let i = 11; i >= 0; i--) {
        const date = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
        months.push(`${date.getMonth() + 1}${this.$t('admin.statistics.months')}`)
        monthKeys.push(key)
      }

      // 构建月份->数量映射
      const dataMap = {}
      monthlyData.forEach(item => {
        dataMap[item.name] = item.value
      })

      // 按月标签顺序填充数据
      const data = monthKeys.map(k => dataMap[k] || 0)

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
          name: this.$t('reader.borrowingHistory.borrowQuantity'),
          minInterval: 1
        },
        series: [{
          name: this.$t('reader.borrowingHistory.borrowAmount'),
          type: 'bar',
          data: data,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }],
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        }
      }

      this.monthlyChartInstance.setOption(option)
    },
    initCategoryChart(categoryData) {
      const chartDom = this.$refs.categoryChart
      if (!chartDom) return

      this.categoryChartInstance = echarts.init(chartDom)

      const data = Object.entries(categoryData).map(([name, value]) => ({
        name,
        value
      }))

      const colors = ['#667eea', '#43e97b', '#f093fb', '#4facfe', '#f5576c', '#e6a23c']

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: `{b}: {c}${this.$t('reader.borrowing.bookUnit')} ({d}%)`
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'middle'
        },
        series: [{
          name: this.$t('reader.borrowingHistory.bookCategory'),
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['55%', '50%'],
          data: data,
          color: colors,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
          label: {
            formatter: `{b}\n{c}${this.$t('reader.borrowing.bookUnit')}`
          }
        }]
      }

      this.categoryChartInstance.setOption(option)
    },
    handleResize() {
      if (this.monthlyChartInstance) {
        this.monthlyChartInstance.resize()
      }
      if (this.categoryChartInstance) {
        this.categoryChartInstance.resize()
      }
    }
  }
}
</script>

<style scoped>
.borrowing-history-view {
  padding: 20px;
}

.borrowing-history-view h2 {
  margin: 0 0 24px 0;
  color: #303133;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 350px;
}
</style>
