<template>
  <div class="dashboard-page">

    <!-- Header -->
    <div class="page-header">
      <div>
        <div class="page-kicker">System</div>
        <h1>Dashboard</h1>
        <p class="page-sub">Truy cập nhanh các module quản trị</p>
      </div>
      <button class="btn-reload" @click="reload">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 11-2.12-9.36L23 10"/></svg>
        Làm mới
      </button>
    </div>

    <!-- Module grid -->
    <div class="modules-grid">
      <button
        v-for="c in cards"
        :key="c.title"
        class="module-card"
        @click="$router.push(c.to)"
      >
        <div class="module-icon" :style="{ background: c.color + '18', color: c.color }">
          <span>{{ c.icon }}</span>
        </div>
        <div class="module-info">
          <div class="module-title">{{ c.title }}</div>
          <div class="module-desc">{{ c.desc }}</div>
        </div>
        <svg class="module-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
      </button>
    </div>

    <!-- Revenue section -->
    <div class="revenue-card">
      <div class="revenue-header">
        <div>
          <div class="section-label">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/></svg>
            Doanh thu theo khách hàng
          </div>
          <div class="revenue-count">{{ revenueList.length }} khách hàng</div>
        </div>
        <div class="total-box">
          <div class="total-label">Tổng doanh thu</div>
          <div class="total-value">{{ totalRevenue.toLocaleString() }} đ</div>
        </div>
      </div>

      <div v-if="loadingRevenue" class="loading-wrap">
        <div class="spinner" />
        <span>Đang tải...</span>
      </div>

      <div v-else-if="revenueList.length === 0" class="empty-state">
        Không có dữ liệu doanh thu
      </div>

      <div v-else class="revenue-table-wrap">
        <table class="revenue-table">
          <thead>
            <tr>
              <th style="width:48px">#</th>
              <th>Khách hàng</th>
              <th style="text-align:right">Doanh thu</th>
              <th style="text-align:right;width:120px">Tỷ trọng</th>
              <th style="width:180px">Tỷ lệ</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, idx) in revenueList" :key="idx" class="revenue-row">
              <td class="idx-cell">{{ idx + 1 }}</td>
              <td class="name-cell">{{ row.customerName }}</td>
              <td class="money-cell">{{ row.totalRevenue?.toLocaleString() }} đ</td>
              <td class="pct-cell">
                {{ totalRevenue ? ((row.totalRevenue / totalRevenue) * 100).toFixed(1) : 0 }}%
              </td>
              <td class="bar-cell">
                <div class="bar-track">
                  <div
                    class="bar-fill"
                    :style="{ width: totalRevenue ? ((row.totalRevenue / totalRevenue) * 100) + '%' : '0%' }"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { ordersApi } from "../../api/orders.api";
import { toast } from "../../ui/toast";

const revenueList = ref([]);
const loadingRevenue = ref(false);

const totalRevenue = computed(() =>
  revenueList.value.reduce((sum, i) => sum + (i.totalRevenue || 0), 0)
);

const loadRevenueByCustomer = async () => {
  loadingRevenue.value = true;
  try {
    const res = await ordersApi.getRevenueByCustomer();
    revenueList.value = res.data || [];
  } finally {
    loadingRevenue.value = false;
  }
};

onMounted(() => loadRevenueByCustomer());

function reload() {
  loadRevenueByCustomer();
  toast("Dashboard đã làm mới.", "info");
}

const cards = [
  { title: "Users",               desc: "Quản lý tài khoản & phân quyền",     to: "/system/users",                   icon: "👤", color: "#2563eb" },
  { title: "Customers",           desc: "Khách hàng & hạng thành viên",        to: "/system/customers",               icon: "🧑‍🤝‍🧑", color: "#16a34a" },
  { title: "Inactive Customers",  desc: "Khách hàng không hoạt động",          to: "/system/inactive-customers",      icon: "😴", color: "#9ca3af" },
  { title: "Categories",          desc: "Danh mục sản phẩm",                   to: "/system/categories",              icon: "🗂️", color: "#d97706" },
  { title: "Products",            desc: "Tạo & quản lý sản phẩm",             to: "/system/products",                icon: "📦", color: "#7c3aed" },
  { title: "Orders — New",        desc: "Đơn hàng mới cần xử lý",             to: "/system/orders/new",              icon: "🆕", color: "#0891b2" },
  { title: "Orders — Processing", desc: "Hàng đợi đang xử lý",                to: "/system/orders/processing",       icon: "⚙️", color: "#ea580c" },
  { title: "Loyalty Summary",     desc: "Tổng hợp điểm thưởng",               to: "/system/Loyaltysummaryadmin",     icon: "⭐", color: "#ca8a04" },
  { title: "Pricing",             desc: "Giá biến thể & lịch sử",             to: "/system/pricing",                 icon: "💰", color: "#16a34a" },
  { title: "Promotions",          desc: "Mã giảm giá & chiến dịch",           to: "/system/promotions",              icon: "🎁", color: "#db2777" },
  { title: "Payment History",     desc: "Tất cả giao dịch thanh toán",         to: "/system/payments",                icon: "🧾", color: "#2563eb" },
  { title: "Birthday Manager",    desc: "Quản lý sinh nhật khách hàng",        to: "/system/birthday",                icon: "🎂", color: "#ec4899" },
  { title: "Customer Spending",   desc: "Chi tiêu theo khách hàng",            to: "/system/customer-spending",       icon: "📊", color: "#0891b2" },
  { title: "Settings",            desc: "Đơn vị tiền tệ mặc định",            to: "/system/settings/currency",       icon: "⚙️", color: "#6b7280" },
];
</script>

<style scoped>
.dashboard-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
}

/* Header */
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 16px; margin-bottom: 28px;
}
.page-kicker {
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .08em; color: #2563eb; background: #eff6ff;
  padding: 2px 10px; border-radius: 50px; display: inline-block; margin-bottom: 6px;
}
.page-header h1 { font-size: 24px; font-weight: 800; color: #111827; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #6b7280; margin: 0; }
.btn-reload {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 18px; border: 1.5px solid #e5e7eb;
  border-radius: 50px; background: white; font-size: 13px;
  color: #374151; cursor: pointer; font-family: inherit;
  transition: all .15s; white-space: nowrap;
}
.btn-reload:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }

/* Modules grid */
.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
  margin-bottom: 28px;
}
.module-card {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px;
  background: white; border: 1.5px solid #f3f4f6;
  border-radius: 14px; cursor: pointer; text-align: left;
  transition: all .15s; font-family: inherit; width: 100%;
}
.module-card:hover {
  border-color: #bfdbfe; background: #f8faff;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(37,99,235,.08);
}
.module-icon {
  width: 42px; height: 42px; border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.module-info { flex: 1; min-width: 0; }
.module-title { font-size: 13px; font-weight: 700; color: #111827; }
.module-desc { font-size: 12px; color: #9ca3af; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.module-arrow { color: #d1d5db; flex-shrink: 0; transition: transform .15s, color .15s; }
.module-card:hover .module-arrow { color: #2563eb; transform: translateX(3px); }

/* Revenue card */
.revenue-card {
  background: white; border: 1.5px solid #f0f0f0;
  border-radius: 18px; padding: 24px; overflow: hidden;
}
.revenue-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 16px; margin-bottom: 20px;
}
.section-label {
  display: flex; align-items: center; gap: 7px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .07em; color: #9ca3af; margin-bottom: 4px;
}
.revenue-count { font-size: 22px; font-weight: 800; color: #111827; }
.total-box { text-align: right; }
.total-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .07em; color: #9ca3af; margin-bottom: 4px; }
.total-value { font-size: 24px; font-weight: 900; color: #16a34a; }

/* Loading */
.loading-wrap { display: flex; align-items: center; gap: 10px; padding: 40px 0; justify-content: center; color: #9ca3af; font-size: 13px; }
.spinner { width: 20px; height: 20px; border: 2.5px solid #e5e7eb; border-top-color: #2563eb; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.empty-state { text-align: center; padding: 48px 0; font-size: 13px; color: #d1d5db; }

/* Table */
.revenue-table-wrap { overflow-x: auto; border-radius: 12px; border: 1px solid #f3f4f6; }
.revenue-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.revenue-table thead tr { background: #f9fafb; }
.revenue-table th {
  padding: 10px 16px; text-align: left;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .06em; color: #9ca3af; white-space: nowrap;
  border-bottom: 1px solid #f3f4f6;
}
.revenue-row { border-bottom: 1px solid #f9fafb; transition: background .12s; }
.revenue-row:last-child { border-bottom: none; }
.revenue-row:hover { background: #f8faff; }
.revenue-table td { padding: 12px 16px; }
.idx-cell { color: #d1d5db; font-weight: 700; }
.name-cell { font-weight: 600; color: #111827; }
.money-cell { text-align: right; font-weight: 700; color: #111827; white-space: nowrap; }
.pct-cell { text-align: right; color: #6b7280; font-weight: 600; }
.bar-cell { padding-right: 20px; }
.bar-track { height: 6px; background: #f3f4f6; border-radius: 99px; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #2563eb, #60a5fa); border-radius: 99px; transition: width .4s ease; min-width: 2px; }
</style>