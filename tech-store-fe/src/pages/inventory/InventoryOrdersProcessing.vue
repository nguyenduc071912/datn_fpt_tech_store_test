<template>
  <div class="container-xl">
    <el-card shadow="never">

      <div class="mb-3">
        <div class="kicker">Inventory</div>
        <div class="title">Processing Orders</div>
        <div class="muted">Preparing orders in warehouse</div>
      </div>

      <el-table :data="orders" border stripe>

        <el-table-column prop="orderNumber" label="Order"/>
        <el-table-column prop="status" label="Status"/>

        <el-table-column label="Actions">
          <template #default="{ row }">

            <el-button
              type="primary"
              @click="markShipping(row.orderId)"
            >
              Shipping
            </el-button>

            <el-button @click="goDetail(row.orderId)">
              Detail
            </el-button>

          </template>
        </el-table-column>

      </el-table>

    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";

const router = useRouter();
const orders = ref([]);

const loadOrders = async () => {
  const res = await ordersApi.listProcessing();
  orders.value = res.data;
};

const markShipping = async (orderId) => {
  await ordersApi.markAsShipping(orderId);
  loadOrders();
};

const goDetail = (orderId) => {
  router.push(`/inventory/orders/${orderId}`);
};

onMounted(loadOrders);
</script>