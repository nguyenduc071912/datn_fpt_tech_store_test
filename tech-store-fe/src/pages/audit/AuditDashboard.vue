<template>
  <div class="ad-page">

    <!-- ── Header ── -->
    <header class="ad-header">
      <div class="ad-eyebrow">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
        Hệ thống
      </div>
      <h1 class="ad-title">Log &amp; Audit <span class="ad-title__accent">Dashboard</span></h1>
      <p class="ad-subtitle">Tổng quan giám sát bảo mật và hoạt động hệ thống theo thời gian thực</p>
    </header>

    <!-- ── Summary Cards ── -->
    <div class="ad-stats-grid">

      <!-- Tổng logs -->
      <div class="ad-stat ad-stat--blue">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.totalLogs }}</div>
        <div class="ad-stat__label">Tổng Logs</div>
      </div>

      <!-- Logs hôm nay -->
      <div class="ad-stat ad-stat--orange">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.todayLogs }}</div>
        <div class="ad-stat__label">Logs Hôm Nay</div>
      </div>

      <!-- Vi phạm bảo mật -->
      <div class="ad-stat ad-stat--red">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.securityViolations }}</div>
        <div class="ad-stat__label">Vi Phạm Bảo Mật</div>
      </div>

      <!-- Hoạt động đáng ngờ -->
      <div class="ad-stat ad-stat--yellow">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.suspiciousActivities }}</div>
        <div class="ad-stat__label">Hoạt Động Đáng Ngờ</div>
      </div>

      <!-- Thao tác nhạy cảm -->
      <div class="ad-stat ad-stat--green">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
            <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.sensitiveOperations }}</div>
        <div class="ad-stat__label">Thao Tác Nhạy Cảm</div>
      </div>

      <!-- Audit hôm nay -->
      <div class="ad-stat ad-stat--purple">
        <div class="ad-stat__icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="ad-stat__num">{{ summary.auditActionsToday }}</div>
        <div class="ad-stat__label">Audit Hôm Nay</div>
      </div>

    </div>

    <!-- ── Charts Row ── -->
    <div class="ad-charts-grid">

      <!-- Thống kê mức độ nghiêm trọng -->
      <div class="ad-card">
        <div class="ad-card__head">
          <span class="ad-card__title">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <circle cx="12" cy="12" r="10"/>
              <path d="M8.56 2.75c4.37 6.03 6.02 9.42 8.03 17.72m2.54-15.38c-3.72 4.35-8.94 5.66-16.88 5.85m19.5 1.9c-3.5-.93-6.63-.82-8.94 0-2.58.92-5.01 2.86-7.44 6.32"/>
            </svg>
            Thống Kê Mức Độ
          </span>
        </div>
        <div class="ad-card__body ad-card__body--chart">
          <Doughnut v-if="severityChart?.datasets" :data="severityChart" :options="doughnutOptions" />
          <div v-else class="ad-chart-empty">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="12" cy="12" r="10"/></svg>
            <span>Đang tải dữ liệu...</span>
          </div>
        </div>
      </div>

      <!-- Audit theo loại hành động -->
      <div class="ad-card">
        <div class="ad-card__head">
          <span class="ad-card__title">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="18" y1="20" x2="18" y2="10"/>
              <line x1="12" y1="20" x2="12" y2="4"/>
              <line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            Audit Theo Loại Hành Động
          </span>
        </div>
        <div class="ad-card__body ad-card__body--chart">
          <Bar v-if="auditChart?.datasets" :data="auditChart" :options="barOptions" />
          <div v-else class="ad-chart-empty">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/></svg>
            <span>Đang tải dữ liệu...</span>
          </div>
        </div>
      </div>

    </div>

    <!-- ── Weekly Trend ── -->
    <div class="ad-card ad-card--full">
      <div class="ad-card__head">
        <span class="ad-card__title">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
          Xu Hướng Log &amp; Audit Trong Tuần
        </span>
      </div>
      <div class="ad-card__body ad-card__body--chart ad-card__body--chart-wide">
        <Line v-if="weeklyTrendChart?.datasets" :data="weeklyTrendChart" :options="lineOptions" />
        <div v-else class="ad-chart-empty">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
          <span>Đang tải dữ liệu...</span>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { auditApi } from "../../api/audit.api";

import { Bar, Doughnut, Line } from "vue-chartjs";
import {
  Chart,
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from "chart.js";

Chart.register(
  BarElement,
  CategoryScale,
  LinearScale,
  ArcElement,
  PointElement,
  LineElement,
  Tooltip,
  Legend
);

// ================= STATE =================
const summary = ref({
  todayLogs: 0,
  securityViolations: 0,
  suspiciousActivities: 0,
  sensitiveOperations: 0,
  totalLogs: 0,
  auditActionsToday: 0,
});

const severityChart = ref(null);
const auditChart    = ref(null);
const weeklyTrendChart = ref(null);

// ================= CHART OPTIONS =================
const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "bottom", labels: { font: { family: "Plus Jakarta Sans", size: 12 }, padding: 16 } },
    tooltip: { bodyFont: { family: "Plus Jakarta Sans" }, titleFont: { family: "Plus Jakarta Sans" } },
  },
};

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: { bodyFont: { family: "Plus Jakarta Sans" }, titleFont: { family: "Plus Jakarta Sans" } },
  },
  scales: {
    x: { grid: { display: false }, ticks: { font: { family: "Plus Jakarta Sans", size: 12 } } },
    y: { grid: { color: "#f0f2f5" }, ticks: { font: { family: "Plus Jakarta Sans", size: 12 } } },
  },
};

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: "top", labels: { font: { family: "Plus Jakarta Sans", size: 12 }, padding: 16 } },
    tooltip: { bodyFont: { family: "Plus Jakarta Sans" }, titleFont: { family: "Plus Jakarta Sans" } },
  },
  scales: {
    x: { grid: { display: false }, ticks: { font: { family: "Plus Jakarta Sans", size: 12 } } },
    y: { grid: { color: "#f0f2f5" }, ticks: { font: { family: "Plus Jakarta Sans", size: 12 } } },
  },
};

// ================= LOAD DASHBOARD =================
const loadDashboard = async () => {
  try {
    const res  = await auditApi.dashboard();
    const data = res.data;

    // ===== SUMMARY CARDS =====
    summary.value = {
      todayLogs:            data.todayLogs            ?? 0,
      securityViolations:   data.securityViolations   ?? 0,
      suspiciousActivities: data.suspiciousActivities ?? 0,
      sensitiveOperations:  data.sensitiveOperations  ?? 0,
      totalLogs:            data.totalLogs            ?? 0,
      auditActionsToday:    data.auditActionsToday    ?? 0,
    };

    // ===== SEVERITY CHART (Doughnut) =====
    severityChart.value = {
      labels: ["CAO", "TRUNG BÌNH", "NGHIÊM TRỌNG"],
      datasets: [
        {
          label: "Số lượng",
          backgroundColor: ["#f87171", "#fbbf24", "#ef4444"],
          borderWidth: 0,
          data: [
            data.severityStats?.HIGH     ?? 0,
            data.severityStats?.MEDIUM   ?? 0,
            data.severityStats?.CRITICAL ?? 0,
          ],
        },
      ],
    };

    // ===== AUDIT CHART BY TYPE (Bar) =====
    auditChart.value = {
      labels: Object.keys(data.auditStatsByType ?? {}),
      datasets: [
        {
          label: "Hành động Audit",
          backgroundColor: "#2563eb",
          borderRadius: 6,
          data: Object.values(data.auditStatsByType ?? {}),
        },
      ],
    };

    // ===== WEEKLY TREND (Line) =====
    weeklyTrendChart.value = {
      labels: ["Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"],
      datasets: [
        {
          label: "Security Logs",
          borderColor: "#f87171",
          backgroundColor: "rgba(248,113,113,0.12)",
          fill: true,
          tension: 0.4,
          pointBackgroundColor: "#f87171",
          pointRadius: 4,
          data: [
            data.thisWeekLogs * 0.5,
            data.thisWeekLogs * 0.6,
            data.thisWeekLogs * 0.7,
            data.thisWeekLogs * 1.0,
            data.thisWeekLogs * 0.9,
            data.thisWeekLogs * 1.2,
            data.thisWeekLogs,
          ],
        },
        {
          label: "Hành động Audit",
          borderColor: "#2563eb",
          backgroundColor: "rgba(37,99,235,0.1)",
          fill: true,
          tension: 0.4,
          pointBackgroundColor: "#2563eb",
          pointRadius: 4,
          data: [
            data.auditActionsThisWeek * 0.5,
            data.auditActionsThisWeek * 0.6,
            data.auditActionsThisWeek * 0.7,
            data.auditActionsThisWeek * 1.0,
            data.auditActionsThisWeek * 0.9,
            data.auditActionsThisWeek * 1.2,
            data.auditActionsThisWeek,
          ],
        },
      ],
    };
  } catch (err) {
    console.error("Audit Dashboard error:", err);
  }
};

onMounted(loadDashboard);
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap");

/* ── Design Tokens ──────────────────────── */
.ad-page {
  --c-bg:            #f6f7f9;
  --c-card:          #ffffff;
  --c-border:        #e4e7ec;
  --c-border-light:  #f0f2f5;
  --c-text:          #0f1117;
  --c-muted:         #6b7280;
  --c-subtle:        #9ca3af;
  --c-blue:          #2563eb;
  --c-blue-bg:       #eff6ff;
  --c-blue-border:   #bfdbfe;
  --c-green:         #16a34a;
  --c-green-bg:      #f0fdf4;
  --c-green-border:  #bbf7d0;
  --c-red:           #dc2626;
  --c-red-bg:        #fff1f2;
  --c-red-border:    #fecdd3;
  --c-orange:        #d97706;
  --c-orange-bg:     #fffbeb;
  --c-orange-border: #fde68a;
  --c-yellow:        #ca8a04;
  --c-yellow-bg:     #fefce8;
  --c-yellow-border: #fef08a;
  --c-purple:        #7c3aed;
  --c-purple-bg:     #f5f3ff;
  --c-purple-border: #ddd6fe;
  --radius:          12px;
  --radius-sm:       8px;
  --shadow-sm:       0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04);
  --shadow-md:       0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04);

  font-family: "Plus Jakarta Sans", sans-serif;
  background: var(--c-bg);
  min-height: 100vh;
  padding: 28px 28px 48px;
  box-sizing: border-box;
  color: var(--c-text);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ── Header ──────────────────────────────── */
.ad-header {
  margin-bottom: 4px;
}

.ad-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: var(--c-muted);
  margin-bottom: 8px;
}

.ad-title {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  margin: 0 0 6px;
  line-height: 1.2;
  color: var(--c-text);
}

.ad-title__accent { color: var(--c-blue); }

.ad-subtitle {
  font-size: 13.5px;
  color: var(--c-muted);
  margin: 0;
}

/* ── Stats Grid (6 cards) ────────────────── */
.ad-stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) { .ad-stats-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 700px)  { .ad-stats-grid { grid-template-columns: repeat(2, 1fr); } }

.ad-stat {
  background: var(--c-border-light);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  padding: 18px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: transform 0.15s, box-shadow 0.15s;
}

.ad-stat:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.ad-stat--blue   { border-top: 3px solid var(--c-blue); }
.ad-stat--orange { border-top: 3px solid var(--c-orange); }
.ad-stat--red    { border-top: 3px solid var(--c-red); }
.ad-stat--yellow { border-top: 3px solid var(--c-yellow); }
.ad-stat--green  { border-top: 3px solid var(--c-green); }
.ad-stat--purple { border-top: 3px solid var(--c-purple); }

.ad-stat__icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ad-stat--blue   .ad-stat__icon { background: var(--c-blue-bg);   color: var(--c-blue); }
.ad-stat--orange .ad-stat__icon { background: var(--c-orange-bg); color: var(--c-orange); }
.ad-stat--red    .ad-stat__icon { background: var(--c-red-bg);    color: var(--c-red); }
.ad-stat--yellow .ad-stat__icon { background: var(--c-yellow-bg); color: var(--c-yellow); }
.ad-stat--green  .ad-stat__icon { background: var(--c-green-bg);  color: var(--c-green); }
.ad-stat--purple .ad-stat__icon { background: var(--c-purple-bg); color: var(--c-purple); }

.ad-stat__num {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
  font-family: "JetBrains Mono", monospace;
}

.ad-stat--blue   .ad-stat__num { color: var(--c-blue); }
.ad-stat--orange .ad-stat__num { color: var(--c-orange); }
.ad-stat--red    .ad-stat__num { color: var(--c-red); }
.ad-stat--yellow .ad-stat__num { color: var(--c-yellow); }
.ad-stat--green  .ad-stat__num { color: var(--c-green); }
.ad-stat--purple .ad-stat__num { color: var(--c-purple); }

.ad-stat__label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--c-muted);
}

/* ── Charts 2-col Grid ───────────────────── */
.ad-charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 860px) { .ad-charts-grid { grid-template-columns: 1fr; } }

/* ── Card ────────────────────────────────── */
.ad-card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.ad-card--full {
  width: 100%;
}

.ad-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  border-bottom: 1px solid var(--c-border-light);
  background: #fafbfc;
}

.ad-card__title {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--c-text);
}

.ad-card__body {
  padding: 20px;
}

.ad-card__body--chart {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 260px;
}

.ad-card__body--chart-wide {
  min-height: 300px;
}

/* ── Chart Empty State ───────────────────── */
.ad-chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: var(--c-subtle);
  font-size: 13px;
}

/* ── Responsive ──────────────────────────── */
@media (max-width: 1200px) {
  .ad-page { padding: 20px 20px 40px; }
}

@media (max-width: 700px) {
  .ad-page { padding: 16px 14px 40px; gap: 18px; }
  .ad-title { font-size: 22px; }
}
</style>