<template>
  <div class="order-page">
    <!-- Header -->
    <div class="order-header">
      <button class="back-btn" @click="$router.push('/')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M19 12H5M12 5l-7 7 7 7"/></svg>
        Tiếp tục mua sắm
      </button>
      <div class="header-title">
        <span class="step-badge">Bước cuối</span>
        <h1>Xác nhận đơn hàng</h1>
      </div>
    </div>

    <div class="order-layout">
      <!-- LEFT: Items -->
      <div class="order-left">
        <div class="section-card">
          <div class="section-label">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
            Sản phẩm đã chọn
          </div>

          <div v-if="form.items.length === 0" class="empty-cart">
            <div class="empty-icon">🛒</div>
            <p>Giỏ hàng trống</p>
            <button class="btn-outline" @click="$router.push('/')">Khám phá sản phẩm</button>
          </div>

          <div v-else class="items-list">
            <div
              v-for="(item, idx) in form.items"
              :key="idx"
              class="item-row"
            >
              <div class="item-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8M12 17v4"/></svg>
              </div>
              <div class="item-info">
                <div class="item-name">{{ item.productName || 'Sản phẩm #' + item.variantId }}</div>
                <div class="item-meta">Variant ID: {{ item.variantId }}</div>
              </div>
              <div class="item-qty">
                <span class="qty-label">SL</span>
                <span class="qty-value">{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- RIGHT: Options + Submit -->
      <div class="order-right">
        <!-- Payment method -->
        <div class="section-card">
          <div class="section-label">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2"/><path d="M1 10h22"/></svg>
            Phương thức thanh toán
          </div>
          <div class="payment-options">
            <label
              v-for="opt in paymentOptions"
              :key="opt.value"
              class="payment-opt"
              :class="{ active: form.paymentMethod === opt.value }"
            >
              <input type="radio" :value="opt.value" v-model="form.paymentMethod" />
              <span class="opt-icon">{{ opt.icon }}</span>
              <span class="opt-label">{{ opt.label }}</span>
              <span v-if="form.paymentMethod === opt.value" class="check-mark">✓</span>
            </label>
          </div>
        </div>

        <!-- Notes -->
        <div class="section-card">
          <div class="section-label">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14,2 14,8 20,8"/></svg>
            Ghi chú (tùy chọn)
          </div>
          <textarea
            v-model="form.notes"
            class="notes-input"
            placeholder="Ví dụ: giao hàng buổi sáng, để trước cửa..."
            rows="3"
          />
        </div>

        <!-- Alert -->
        <div v-if="alert" class="alert-box">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
          {{ alert }}
        </div>

        <!-- Submit -->
        <button
          class="submit-btn"
          :class="{ loading }"
          :disabled="loading || form.items.length === 0"
          @click="submit"
        >
          <span v-if="!loading">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
            Đặt hàng ngay
          </span>
          <span v-else class="spinner-wrap">
            <span class="spinner" />
            Đang xử lý...
          </span>
        </button>

        <p class="order-note">
          Bằng cách đặt hàng, bạn đồng ý với điều khoản sử dụng của chúng tôi.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ordersApi } from "../../api/orders.api";
import { customersApi } from "../../api/customers.api";
import { cartApi } from "../../api/cart.api";
import { useCartStore } from "../../stores/cart";
import { toast } from "../../ui/toast";

const router = useRouter();
const cartStore = useCartStore();
const loading = ref(false);
const alert = ref("");

const form = reactive({
  customerId: null,
  paymentMethod: "CASH",
  channel: "ONLINE",
  notes: "",
  items: [],
});

const paymentOptions = [
  { value: "CASH", label: "Tiền mặt", icon: "💵" },
  { value: "TRANSFER", label: "Chuyển khoản", icon: "🏦" },
  { value: "CARD", label: "Thẻ tín dụng", icon: "💳" },
];

function pickOrderId(payload) {
  const root = payload?.data ?? payload;
  return root?.id ?? root?.data?.id ?? root?.orderId ?? root?.data?.orderId ?? null;
}

async function submit() {
  alert.value = "";
  if (!form.customerId) { alert.value = "Không thể xác định tài khoản. Vui lòng đăng nhập lại."; return; }
  const cleanedItems = (form.items || [])
    .filter((x) => x && x.variantId && x.quantity)
    .map((x) => ({ variantId: Number(x.variantId), quantity: Number(x.quantity) }));
  if (cleanedItems.length === 0) { alert.value = "Giỏ hàng trống."; return; }

  loading.value = true;
  try {
    const res = await ordersApi.create({
      customerId: Number(form.customerId),
      paymentMethod: form.paymentMethod,
      channel: form.channel,
      notes: form.notes || "",
      items: cleanedItems,
    });
    const orderId = pickOrderId(res?.data);
    toast("Đặt hàng thành công! 🎉", "success");
    if (orderId) {
      await cartStore.refreshCount();
      await cartStore.clearCart();
      router.push(`/orders/${orderId}`);
    } else toast("Đặt hàng thành công, nhưng không tìm thấy mã đơn.", "warning");
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || "Đặt hàng thất bại";
    alert.value = typeof msg === "string" ? msg : JSON.stringify(msg);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  try {
    const profileRes = await customersApi.getProfile();
    const customerId = profileRes?.data?.id ?? profileRes?.data?.data?.id ?? null;
    if (!customerId) { alert.value = "Không thể xác định tài khoản. Vui lòng đăng nhập lại."; return; }
    form.customerId = Number(customerId);

    const cartRes = await cartApi.getItems();
    const cartItems = Array.isArray(cartRes?.data) ? cartRes.data : cartRes?.data?.data || [];
    if (cartItems.length === 0) { alert.value = "Giỏ hàng của bạn đang trống."; return; }
    form.items = cartItems.map((item) => ({
      variantId: item.variantId,
      quantity: item.quantity,
      productName: item.productName,
    }));
  } catch (e) {
    alert.value = "Không thể tải dữ liệu đơn hàng.";
  }
});
</script>

<style scoped>
.order-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
}

/* Header */
.order-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1.5px solid #e5e7eb;
  border-radius: 50px;
  background: white;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.back-btn:hover { border-color: #2563eb; color: #2563eb; background: #eff6ff; }

.header-title { display: flex; flex-direction: column; gap: 2px; }
.step-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .08em;
  color: #2563eb;
  background: #eff6ff;
  padding: 2px 10px;
  border-radius: 50px;
  width: fit-content;
}
.header-title h1 {
  font-size: 22px;
  font-weight: 800;
  color: #111827;
  margin: 0;
}

/* Layout */
.order-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}
@media (max-width: 768px) {
  .order-layout { grid-template-columns: 1fr; }
}

/* Cards */
.section-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
}
.section-label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .07em;
  color: #6b7280;
  margin-bottom: 18px;
}

/* Empty cart */
.empty-cart {
  text-align: center;
  padding: 40px 0 20px;
  color: #9ca3af;
}
.empty-icon { font-size: 40px; margin-bottom: 10px; }
.empty-cart p { font-size: 14px; margin-bottom: 16px; }
.btn-outline {
  padding: 8px 20px;
  border: 1.5px solid #2563eb;
  border-radius: 50px;
  color: #2563eb;
  background: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;
}
.btn-outline:hover { background: #eff6ff; }

/* Items */
.items-list { display: flex; flex-direction: column; gap: 12px; }
.item-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #f9fafb;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  transition: border-color .15s;
}
.item-row:hover { border-color: #dbeafe; }
.item-icon {
  width: 40px; height: 40px;
  background: #eff6ff;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #2563eb;
  flex-shrink: 0;
}
.item-info { flex: 1; min-width: 0; }
.item-name { font-size: 14px; font-weight: 600; color: #111827; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-meta { font-size: 12px; color: #9ca3af; margin-top: 2px; }
.item-qty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  flex-shrink: 0;
}
.qty-label { font-size: 10px; font-weight: 700; text-transform: uppercase; color: #9ca3af; letter-spacing: .06em; }
.qty-value { font-size: 18px; font-weight: 800; color: #111827; line-height: 1; }

/* Payment options */
.payment-options { display: flex; flex-direction: column; gap: 10px; }
.payment-opt {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 2px solid #f3f4f6;
  border-radius: 12px;
  cursor: pointer;
  transition: all .15s;
  position: relative;
}
.payment-opt input { display: none; }
.payment-opt:hover { border-color: #bfdbfe; background: #f8faff; }
.payment-opt.active { border-color: #2563eb; background: #eff6ff; }
.opt-icon { font-size: 20px; }
.opt-label { font-size: 14px; font-weight: 600; color: #111827; flex: 1; }
.check-mark {
  font-size: 13px; font-weight: 800;
  color: white; background: #2563eb;
  width: 22px; height: 22px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
}

/* Notes */
.notes-input {
  width: 100%;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  font-size: 14px;
  font-family: inherit;
  resize: none;
  color: #374151;
  transition: border-color .15s;
  box-sizing: border-box;
}
.notes-input:focus { outline: none; border-color: #2563eb; }
.notes-input::placeholder { color: #d1d5db; }

/* Alert */
.alert-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 12px;
  font-size: 13px;
  color: #dc2626;
  margin-bottom: 16px;
}

/* Submit */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: #111827;
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all .2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.submit-btn:hover:not(:disabled) { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 8px 25px rgba(37,99,235,.3); }
.submit-btn:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.submit-btn span { display: flex; align-items: center; gap: 8px; }

.spinner-wrap { display: flex; align-items: center; gap: 8px; }
.spinner {
  width: 16px; height: 16px;
  border: 2.5px solid rgba(255,255,255,.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin .7s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

.order-note {
  font-size: 11px;
  color: #9ca3af;
  text-align: center;
  margin-top: 12px;
  line-height: 1.5;
}
</style>