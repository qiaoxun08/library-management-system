// ========================================
// ECharts 「书香」暖色主题
// 在各图表组件中：import { warmTheme } from '@/styles/echarts-theme'
// 然后 echarts.init(dom, warmTheme)
// ========================================

export const warmTheme = {
  color: [
    '#2C3E50', // 暖墨蓝
    '#C0785C', // 赭石
    '#6B8F71', // 苔藓绿
    '#D4A84B', // 暗金
    '#5A7D9A', // 雾蓝
    '#A85454', // 暗朱红
    '#3D5A80', // 浅墨蓝
    '#D4956B', // 暖赭石
  ],
  backgroundColor: 'transparent',
  textStyle: {
    fontFamily: "'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif",
    color: '#4A5568'
  },
  title: {
    textStyle: {
      color: '#2C3440',
      fontFamily: "'Noto Serif SC', Georgia, serif",
      fontWeight: 600
    },
    subtextStyle: {
      color: '#7A8599'
    }
  },
  legend: {
    textStyle: {
      color: '#4A5568'
    }
  },
  tooltip: {
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderColor: '#EAE6E0',
    borderWidth: 1,
    textStyle: {
      color: '#2C3440',
      fontFamily: "'Noto Sans SC', sans-serif"
    },
    extraCssText: 'box-shadow: 0 4px 16px rgba(44, 62, 80, 0.08); border-radius: 10px;'
  },
  categoryAxis: {
    axisLine: { lineStyle: { color: '#DDD8D0' } },
    axisTick: { lineStyle: { color: '#DDD8D0' } },
    axisLabel: { color: '#7A8599' },
    splitLine: { lineStyle: { color: '#EAE6E0' } }
  },
  valueAxis: {
    axisLine: { lineStyle: { color: '#DDD8D0' } },
    axisTick: { lineStyle: { color: '#DDD8D0' } },
    axisLabel: { color: '#7A8599' },
    splitLine: { lineStyle: { color: '#EAE6E0', type: 'dashed' } }
  },
  line: {
    smooth: true,
    symbolSize: 6,
    lineStyle: { width: 2.5 }
  },
  bar: {
    barWidth: '45%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0]
    }
  },
  pie: {
    itemStyle: {
      borderColor: '#FFFFFF',
      borderWidth: 2
    }
  },
  heatmap: {
    itemStyle: {
      borderColor: '#FFFFFF',
      borderWidth: 1
    }
  }
}

// 借阅热度色阶（用于热力图）
export const heatColors = ['#F0EBE3', '#E8C4AC', '#D4956B', '#C0785C', '#A85454', '#2C3E50']

// ECharts 渐变色工厂函数
export function warmGradient(echarts, direction = 'vertical') {
  if (direction === 'vertical') {
    return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
      { offset: 0, color: '#2C3E50' },
      { offset: 1, color: '#3D5A80' }
    ])
  }
  return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: '#2C3E50' },
    { offset: 1, color: '#C0785C' }
  ])
}
