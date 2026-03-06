<template>
  <div class="audit-page">
    <!-- ===== HEADER ===== -->
    <div class="page-header">
      <div class="title">Security Logs</div>
    </div>

    <!-- ===== FILTER PANEL ===== -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-input
          v-model="filter.username"
          placeholder="Username"
          style="width: 160px"
          clearable
        />

        <el-input
          v-model="filter.ipAddress"
          placeholder="IP Address"
          style="width: 160px"
          clearable
        />

        <el-input
          v-model="filter.keyword"
          placeholder="Description"
          style="width: 180px"
          clearable
        />

        <!-- Severity -->
        <el-select
          v-model="filter.severity"
          placeholder="Severity"
          clearable
          style="width: 150px"
        >
          <el-option label="CRITICAL" value="CRITICAL" />
          <el-option label="HIGH" value="HIGH" />
          <el-option label="MEDIUM" value="MEDIUM" />
          <el-option label="LOW" value="LOW" />
        </el-select>

        <!-- Status -->
        <el-select
          v-model="filter.status"
          placeholder="Status"
          clearable
          style="width: 150px"
        >
          <el-option label="SUCCESS" value="SUCCESS" />
          <el-option label="FAILED" value="FAILED" />
          <el-option label="BLOCKED" value="BLOCKED" />
          <el-option label="401" value="401" />
          <el-option label="403" value="403" />
        </el-select>

        <!-- FROM - TO -->
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="From"
          end-placeholder="To"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 260px"
        />

        <el-button type="primary" @click="searchLogs"> Search </el-button>

        <el-button @click="resetFilter"> Reset </el-button>
      </el-form>
    </el-card>

    <!-- ===== TABLE ===== -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="logs"
        border
        style="width: 100%"
        @sort-change="handleSort"
      >
        <el-table-column prop="id" label="ID" width="80" sortable="custom" />

        <el-table-column prop="username" label="Username" width="160" />

        <el-table-column prop="actionType" label="Action" width="200" />

        <el-table-column prop="targetEntity" label="Target" width="150" />

        <el-table-column prop="targetId" label="Target ID" width="120" />

        <el-table-column prop="ipAddress" label="IP Address" width="160" />

        <!-- SEVERITY CÓ MÀU -->
        <el-table-column label="Severity" width="130">
          <template #default="scope">
            <el-tag
              :type="severityTagType(scope.row.severity)"
              effect="dark"
              size="small"
            >
              {{ scope.row.severity }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- STATUS CÓ MÀU -->
        <el-table-column label="Status" width="110">
          <template #default="scope">
            <el-tag
              :type="statusTagType(scope.row.status)"
              effect="light"
              size="small"
            >
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column
          prop="description"
          label="Description"
          min-width="220"
        />

        <el-table-column
          prop="createdAt"
          label="Created At"
          width="190"
          sortable="custom"
        />
      </el-table>

      <!-- ===== PAGINATION ===== -->
      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="page"
          :page-size="size"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import http from "../../api/http";

/*Colors for severity*/
function severityTagType(severity) {
  switch (severity) {
    case "CRITICAL":
      return "danger"; // đỏ đậm
    case "HIGH":
      return "warning"; // cam
    case "MEDIUM":
      return "info"; // xanh dương nhạt
    case "LOW":
      return "success"; // xanh lá
    default:
      return "info";
  }
}
/*Colors for status*/
function statusTagType(status) {
  switch (status) {
    case 200:
    case "SUCCESS":
      return "success";
    case 401:
    case 403:
    case "FAILED":
      return "danger";
    default:
      return "info";
  }
}

/* ================= STATE ================= */

const logs = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(5);
const loading = ref(false);

const sortBy = ref("createdAt");
const sortDir = ref("DESC");

const dateRange = ref([]);

const filter = reactive({
  username: null,
  ipAddress: null,
  keyword: null,
  severity: null,
  status: null,
  startDate: null,
  endDate: null,
});

/* ================= BUILD PAYLOAD ================= */

function buildPayload() {
  if (dateRange.value?.length === 2) {
    filter.startDate = dateRange.value[0] + "T00:00:00";
    filter.endDate = dateRange.value[1] + "T23:59:59";
  } else {
    filter.startDate = null;
    filter.endDate = null;
  }

  return {
    username: filter.username || null,
    ipAddress: filter.ipAddress || null,
    keyword: filter.keyword || null,
    severities: filter.severity ? [filter.severity] : null,
    statuses: filter.status ? [filter.status] : null,
    startDate: filter.startDate,
    endDate: filter.endDate,
  };
}

/* ================= FETCH ================= */

async function fetchLogs() {
  loading.value = true;

  try {
    const payload = buildPayload();

    const res = await http.post("/api/auth/security-log/search", payload, {
      params: {
        page: page.value - 1,
        size: size.value,
        sortBy: sortBy.value,
        sortDir: sortDir.value,
      },
    });

    logs.value = res.data.content;
    total.value = res.data.totalElements;
  } catch (err) {
    console.error("Load failed:", err.response?.data || err);
  } finally {
    loading.value = false;
  }
}

/* ================= EVENTS ================= */

function searchLogs() {
  page.value = 1;
  fetchLogs();
}

function resetFilter() {
  filter.username = null;
  filter.ipAddress = null;
  filter.keyword = null;
  filter.severity = null;
  filter.status = null;
  dateRange.value = [];
  page.value = 1;
  fetchLogs();
}

function handlePageChange(p) {
  page.value = p;
  fetchLogs();
}

function handleSizeChange(s) {
  size.value = s;
  page.value = 1;
  fetchLogs();
}

function handleSort({ prop, order }) {
  if (!order) return;

  sortBy.value = prop;
  sortDir.value = order === "ascending" ? "ASC" : "DESC";
  fetchLogs();
}

/* ================= INIT ================= */

onMounted(fetchLogs);
</script>

<style scoped>
.audit-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 20px;
  font-weight: 800;
}

.filter-card {
  border-radius: 10px;
}

.filter-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pager {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
</style>
