<template>
  <div class="home-page">
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon product"><el-icon :size="28"><Goods /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-label">商品数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon user"><el-icon :size="28"><User /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户数量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon order"><el-icon :size="28"><Document /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-label">订单数量</div>
        </div>
      </div>
    </div>

    <div class="chart-card">
      <div class="chart-header">
        <h3>订单趋势</h3>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="default"
          @change="loadCharts"
        />
      </div>
      <div ref="chartRef" class="chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { getAdminHomePage, getAdminHomeCharts } from '../../api/admin'
import * as echarts from 'echarts'

const chartRef = ref(null)
const dateRange = ref([])
let chartInstance = null

const stats = reactive({
  productCount: 0,
  userCount: 0,
  orderCount: 0
})

async function loadStats() {
  try {
    const res = await getAdminHomePage()
    stats.productCount = res.productTotal ?? 0
    stats.userCount = res.userTotal ?? 0
    stats.orderCount = res.orderTotal ?? 0
  } catch {}
}

async function loadCharts() {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getAdminHomeCharts(params)
    renderChart(res)
  } catch {}
}

function renderChart(data) {
  if (!chartInstance) return
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单总量', '已完成订单'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: data.dateStr || [] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '订单总量',
        type: 'line',
        smooth: true,
        data: data.orderTotalArray || [],
        itemStyle: { color: '#409eff' },
        areaStyle: { color: 'rgba(64,158,255,0.1)' }
      },
      {
        name: '已完成订单',
        type: 'line',
        smooth: true,
        data: data.orderSuccessArray || [],
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: 'rgba(103,194,58,0.1)' }
      }
    ]
  })
}

onMounted(async () => {
  await nextTick()
  chartInstance = echarts.init(chartRef.value)
  loadStats()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  chartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  chartInstance?.resize()
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-icon.product { background: #409eff; }
.stat-icon.user { background: #67c23a; }
.stat-icon.order { background: #e6a23c; }
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}
.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.chart-container {
  width: 100%;
  height: 400px;
}
</style>
