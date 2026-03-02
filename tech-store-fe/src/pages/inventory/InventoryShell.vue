<template>
  <el-container class="sys">
    <!-- SIDEBAR -->
    <el-aside width="260px" class="aside">
      <div class="brand">
        <div class="brand-name">Tech Store</div>
        <div class="brand-sub">Inventory Portal</div>
      </div>

      <div class="pill">
        <div class="pill-k">Role</div>
        <div class="pill-v">INVENTORY</div>
      </div>

      <el-menu router :default-active="$route.path" class="menu">
        <el-menu-item index="/inventory/orders/paid">
          Paid Orders
        </el-menu-item>

        <el-menu-item index="/inventory/orders/processing">
          Processing Orders
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- CONTENT -->
    <el-container>
      <el-header class="topbar">
        <div class="header-wrap">
            <div class="page-title">Inventory Management</div>
          </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.sys {
  height: 100vh;
}

/* SIDEBAR */
.aside {
  background: #111827;
  color: #fff;
  padding: 20px;
}

/* BRAND */
.brand-name {
  font-size: 18px;
  font-weight: 700;
}

.brand-sub {
  font-size: 12px;
  opacity: 0.7;
}

/* ROLE PILL */
.pill {
  margin: 20px 0;
  background: rgba(255, 255, 255, 0.08);
  padding: 10px;
  border-radius: 8px;
}

.pill-k {
  font-size: 12px;
  opacity: 0.6;
}

.pill-v {
  font-weight: 600;
}

/* ===== MENU FIX ===== */

.menu {
  border: none;
  background: transparent;
}

/* text màu sáng */
.menu :deep(.el-menu-item) {
  color: #d1d5db;
  border-radius: 8px;
  margin-bottom: 6px;
}

/* hover */
.menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

/* active item */
.menu :deep(.is-active) {
  background: #2563eb;
  color: #fff !important;
  font-weight: 600;
}

/* remove default white bg */
.menu :deep(.el-menu) {
  background: transparent;
}

/* HEADER */
.topbar {
  background: #fff;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}

/* HEADER LAYOUT */
.header-wrap {
  display: flex;
  align-items: center;
  height: 100%;
}

/* small label */
.kicker {
  font-size: 12px;
  font-weight: 600;
  color: #2563eb;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* MAIN TITLE */
.page-title {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
  color: #111827;
}

/* description */
.muted {
  font-size: 13px;
  color: #6b7280;
}
</style>
