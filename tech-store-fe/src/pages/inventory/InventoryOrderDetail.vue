<template>
  <div class="container-xl" v-if="order">
    <el-card shadow="never">
      <!-- HEADER -->
      <div
        class="d-flex justify-content-between align-items-end flex-wrap mb-4"
      >
        <div>
          <div class="kicker">Inventory</div>
          <div class="title">Order #{{ order.orderNumber }}</div>
          <div class="muted">Warehouse order detail</div>
        </div>

        <!-- INVENTORY ACTION -->
        <div class="d-flex gap-2">
          <el-button
            v-if="order.status === 'PAID'"
            type="warning"
            @click="markProcessing"
          >
            Start Processing
          </el-button>

          <el-button
            v-if="order.status === 'PROCESSING'"
            type="primary"
            @click="markShipping"
          >
            Move To Shipping
          </el-button>
        </div>
      </div>

      <!-- ORDER INFO -->
      <el-descriptions border :column="2" class="mb-4">
        <el-descriptions-item label="Order">
          {{ order.orderNumber }}
        </el-descriptions-item>

        <el-descriptions-item label="Status">
          <el-tag :type="statusType(order.status)">
            {{ order.status }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="Customer">
          {{ order.customerName }}
        </el-descriptions-item>

        <el-descriptions-item label="Phone">
          {{ order.phone }}
        </el-descriptions-item>

        <el-descriptions-item label="Address">
          {{ order.address }}
        </el-descriptions-item>

        <el-descriptions-item label="Created At">
          {{ order.createdAt }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- ORDER ITEMS -->
      <div class="mb-3">
        <div class="kicker">Items</div>
        <div class="title-sm">Order Products</div>
      </div>

      <el-table :data="order.items" border stripe style="width: 100%">
        <el-table-column prop="productName" label="Product" />

        <el-table-column prop="quantity" label="Quantity" width="120" />

        <el-table-column prop="price" label="Price" width="150" />

        <el-table-column label="Subtotal" width="160">
          <template #default="{ row }">
            {{ formatVND(row.quantity * row.price) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- TOTAL -->
      <div class="d-flex justify-content-end mt-4">
        <el-card shadow="never" style="width: 300px">
          <div class="d-flex justify-content-between">
            <span>Total Amount</span>
            <strong>
              {{ formatVND(order.totalAmount) }}
            </strong>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const route = useRoute();
const order = ref(null);

const statusType = (status) => {
  switch (status) {
    case "PAID":
      return "success";

    case "PROCESSING":
      return "warning";

    case "SHIPPING":
      return "info";

    default:
      return "";
  }
};

/* ======================
   LOAD ORDER
====================== */
const load = async () => {
  const res = await ordersApi.getById(route.params.orderId);
  order.value = res.data;
};

/* ======================
   INVENTORY ACTIONS
====================== */
const markProcessing = async () => {
  await ordersApi.markAsProcessing(order.value.orderId);
  load();
};

const markShipping = async () => {
  await ordersApi.markAsShipping(order.value.orderId);
  load();
};

const formatVND = (value) => {
  if (!value) return "0 ₫";

  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

onMounted(load);
</script>
