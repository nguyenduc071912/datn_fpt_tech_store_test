<template>
  <div class="spin-page">

    <!-- Not VIP -->
    <div v-if="!status.isVip" class="lock-card">
      <div class="lock-icon">🔒</div>
      <h3>Tính năng dành riêng cho VIP</h3>
      <p>Nâng cấp tài khoản để quay vòng may mắn và nhận ưu đãi đặc biệt mỗi tuần.</p>
      <button class="btn-primary" @click="$router.push('/')">Khám phá gói VIP</button>
    </div>

    <!-- VIP -->
    <div v-else>

      <!-- Header -->
      <div class="page-header">
        <div>
          <div class="page-kicker">VIP Exclusive</div>
          <h1>Vòng Quay May Mắn</h1>
          <p class="page-sub">Quay 1 lần mỗi tuần — nhận thêm % giảm giá cho đơn hàng tiếp theo</p>
        </div>
        <!-- Active bonus pill -->
        <div v-if="status.currentBonus > 0" class="bonus-pill">
          <span class="bonus-dot" />
          Đang có ưu đãi <strong>{{ status.currentBonus }}%</strong> · hết {{ formatDate(status.bonusExpiresAt) }}
        </div>
      </div>

      <div class="spin-layout">

        <!-- Wheel area -->
        <div class="wheel-card">
          <div class="wheel-wrap">
            <!-- Pointer -->
            <div class="pointer">▼</div>
            <canvas
              ref="wheelCanvas"
              width="340"
              height="340"
              @click="handleSpin"
              :class="{ spinning: isSpinning, disabled: !status.canSpin }"
            />
          </div>

          <button
            v-if="status.canSpin"
            class="btn-spin"
            :disabled="isSpinning"
            @click="handleSpin"
          >
            <span v-if="!isSpinning">🎡 &nbsp;Quay ngay!</span>
            <span v-else class="pulse">Đang quay...</span>
          </button>

          <div v-else class="cooldown-notice">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
            {{ status.message }} · Quay lại thứ Hai tuần sau
          </div>
        </div>

        <!-- Right: bonus + history -->
        <div class="right-col">

          <!-- Current bonus -->
          <div v-if="status.currentBonus > 0" class="section-card bonus-card">
            <div class="section-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              Ưu đãi đang áp dụng
            </div>
            <div class="bonus-display">
              <div class="bonus-pct">{{ status.currentBonus }}<span>%</span></div>
              <div class="bonus-meta">
                <div class="bonus-label-text">Giảm giá bổ sung</div>
                <div class="bonus-exp">Hết hạn {{ formatDate(status.bonusExpiresAt) }}</div>
              </div>
            </div>
          </div>

          <!-- History -->
          <div class="section-card">
            <div class="section-label">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              Lịch sử quay
            </div>

            <div v-if="history.length === 0" class="empty-history">
              Chưa có lịch sử quay thưởng
            </div>

            <div v-else class="history-list">
              <div
                v-for="item in history"
                :key="item.id"
                class="history-row"
                :class="getHistoryClass(item)"
              >
                <div class="h-icon">{{ getHistoryIcon(item.status) }}</div>
                <div class="h-info">
                  <div class="h-pct">{{ item.discountBonus }}% giảm giá</div>
                  <div class="h-date">{{ formatDate(item.spunAt) }}</div>
                </div>
                <div class="h-status-tag" :class="getHistoryClass(item)">{{ item.status }}</div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Result Modal -->
    <Teleport to="body">
      <div v-if="showResult" class="modal-backdrop" @click="closeResult">
        <div class="modal-box" @click.stop>
          <div class="modal-emoji">🎉</div>
          <h3>Chúc mừng!</h3>
          <div class="modal-pct">{{ lastResult?.discountBonus }}<span>%</span></div>
          <p class="modal-msg">{{ lastResult?.message }}</p>
          <p class="modal-exp">Có hiệu lực đến {{ formatDate(lastResult?.expiresAt) }}</p>
          <button class="btn-primary" @click="closeResult">Tuyệt vời!</button>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script>
import { customersApi } from '../../api/customers.api';

export default {
  name: 'SpinWheel',
  data() {
    return {
      status: { isVip: false, canSpin: false, message: '', currentBonus: 0, bonusExpiresAt: null },
      prizes: [],
      history: [],
      isSpinning: false,
      showResult: false,
      lastResult: null,
      currentRotation: 0,
    };
  },
  mounted() { this.loadData(); },
  methods: {
    async loadData() {
      try {
        const [statusRes, prizesRes, historyRes] = await Promise.all([
          customersApi.getSpinWheelStatus(),
          customersApi.getSpinWheelPrizes(),
          customersApi.getSpinWheelHistory(),
        ]);
        this.status  = statusRes.data;
        this.prizes  = prizesRes.data;
        this.history = historyRes.data;
        if (this.status.isVip) this.$nextTick(() => this.drawWheel());
      } catch (e) { console.error(e); }
    },

    drawWheel() {
      const canvas = this.$refs.wheelCanvas;
      if (!canvas) return;
      const ctx = canvas.getContext('2d');
      const cx = canvas.width / 2, cy = canvas.height / 2, r = 155;
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const palette = ['#2563eb','#16a34a','#d97706','#dc2626','#7c3aed','#0891b2','#db2777','#65a30d'];
      const n = this.prizes.length;
      const seg = (2 * Math.PI) / n;

      // Shadow
      ctx.save();
      ctx.shadowColor = 'rgba(0,0,0,0.15)';
      ctx.shadowBlur = 24;
      ctx.beginPath(); ctx.arc(cx, cy, r, 0, 2 * Math.PI); ctx.fillStyle = '#fff'; ctx.fill();
      ctx.restore();

      this.prizes.forEach((prize, i) => {
        const s = i * seg + this.currentRotation;
        const e = (i + 1) * seg + this.currentRotation;

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.arc(cx, cy, r, s, e);
        ctx.closePath();
        ctx.fillStyle = palette[i % palette.length];
        ctx.fill();
        ctx.strokeStyle = '#fff';
        ctx.lineWidth = 2.5;
        ctx.stroke();

        ctx.save();
        ctx.translate(cx, cy);
        ctx.rotate(s + seg / 2);
        ctx.textAlign = 'right';
        ctx.fillStyle = '#fff';
        ctx.font = 'bold 15px "Be Vietnam Pro", sans-serif';
        ctx.fillText(prize.label, r - 14, 5);
        ctx.restore();
      });

      // Center
      ctx.beginPath(); ctx.arc(cx, cy, 22, 0, 2 * Math.PI);
      ctx.fillStyle = '#fff'; ctx.fill();
      ctx.strokeStyle = '#e5e7eb'; ctx.lineWidth = 3; ctx.stroke();
      ctx.fillStyle = '#374151'; ctx.font = 'bold 11px sans-serif';
      ctx.textAlign = 'center'; ctx.fillText('VIP', cx, cy + 4);
    },

    async handleSpin() {
      if (!this.status.canSpin || this.isSpinning) return;
      this.isSpinning = true;
      try {
        const res = await customersApi.spinWheel();
        this.lastResult = res.data;
        await this.animateSpin(res.data.discountBonus);
        this.showResult = true;
        await this.loadData();
      } catch (e) {
        alert(e.response?.data?.error || 'Lỗi khi quay thưởng');
        this.isSpinning = false;
      }
    },

    animateSpin(resultDiscount) {
      return new Promise(resolve => {
        const duration = 3500;
        const start = Date.now();
        const startRot = this.currentRotation;
        const idx = this.prizes.findIndex(p => p.discount == resultDiscount);
        const seg = (2 * Math.PI) / this.prizes.length;
        const target = (idx >= 0 ? idx * seg : 0);
        const total = Math.PI * 2 * 6 + target;
        const animate = () => {
          const p = Math.min((Date.now() - start) / duration, 1);
          const ease = 1 - Math.pow(1 - p, 4);
          this.currentRotation = startRot + total * ease;
          this.drawWheel();
          if (p < 1) requestAnimationFrame(animate);
          else { this.isSpinning = false; resolve(); }
        };
        animate();
      });
    },

    closeResult() { this.showResult = false; },

    formatDate(d) {
      if (!d) return '';
      return new Date(d).toLocaleString('vi-VN', { day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit' });
    },
    getHistoryClass(item) {
      if (item.isUsed) return 'used';
      if (new Date(item.expiresAt) < new Date()) return 'expired';
      return 'active';
    },
    getHistoryIcon(s) {
      if (s === 'Đã sử dụng') return '✅';
      if (s === 'Đã hết hạn') return '⏱️';
      return '🎁';
    },
  },
};
</script>

<style scoped>
.spin-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px 80px;
  font-family: 'Be Vietnam Pro', 'Segoe UI', sans-serif;
}

/* Lock */
.lock-card {
  text-align: center;
  padding: 72px 32px;
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 20px;
}
.lock-icon { font-size: 56px; margin-bottom: 20px; }
.lock-card h3 { font-size: 22px; font-weight: 800; color: #111827; margin-bottom: 10px; }
.lock-card p { font-size: 14px; color: #6b7280; margin-bottom: 24px; }

/* Header */
.page-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  flex-wrap: wrap; gap: 16px; margin-bottom: 28px;
}
.page-kicker { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .08em; color: #2563eb; background: #eff6ff; padding: 2px 10px; border-radius: 50px; display: inline-block; margin-bottom: 6px; }
.page-header h1 { font-size: 24px; font-weight: 800; color: #111827; margin: 0 0 4px; }
.page-sub { font-size: 13px; color: #6b7280; margin: 0; }
.bonus-pill {
  display: flex; align-items: center; gap: 8px;
  padding: 8px 16px; background: #f0fdf4; border: 1.5px solid #bbf7d0;
  border-radius: 50px; font-size: 13px; color: #16a34a; white-space: nowrap;
}
.bonus-dot { width: 8px; height: 8px; background: #22c55e; border-radius: 50%; flex-shrink: 0; animation: blink 1.5s infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.4} }

/* Layout */
.spin-layout {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  align-items: start;
}
@media (max-width: 720px) { .spin-layout { grid-template-columns: 1fr; } }

/* Wheel card */
.wheel-card {
  background: white;
  border: 1.5px solid #f0f0f0;
  border-radius: 20px;
  padding: 32px 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}
.wheel-wrap { position: relative; display: flex; align-items: center; justify-content: center; }
.pointer {
  position: absolute; top: -18px; left: 50%; transform: translateX(-50%);
  font-size: 22px; color: #dc2626; filter: drop-shadow(0 2px 4px rgba(0,0,0,.2));
  z-index: 2; line-height: 1;
}
canvas { display: block; cursor: pointer; border-radius: 50%; }
canvas.disabled { opacity: .55; cursor: not-allowed; }
canvas.spinning { cursor: wait; }

.btn-spin {
  padding: 14px 48px;
  background: #111827;
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 16px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all .2s;
  min-width: 180px;
}
.btn-spin:hover:not(:disabled) { background: #2563eb; transform: translateY(-2px); box-shadow: 0 8px 24px rgba(37,99,235,.3); }
.btn-spin:disabled { opacity: .5; cursor: not-allowed; transform: none; }
.pulse { animation: pulse 1s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.5} }

.cooldown-notice {
  display: flex; align-items: center; gap: 7px;
  font-size: 13px; color: #9ca3af;
  background: #f9fafb; padding: 10px 18px; border-radius: 50px;
}

/* Right col */
.right-col { display: flex; flex-direction: column; gap: 16px; }
.section-card { background: white; border: 1.5px solid #f0f0f0; border-radius: 16px; padding: 22px; }
.section-label { display: flex; align-items: center; gap: 7px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .07em; color: #9ca3af; margin-bottom: 16px; }

/* Bonus display */
.bonus-card { border-color: #fde68a; background: #fffbeb; }
.bonus-display { display: flex; align-items: center; gap: 16px; }
.bonus-pct { font-size: 52px; font-weight: 900; color: #d97706; line-height: 1; }
.bonus-pct span { font-size: 22px; }
.bonus-label-text { font-size: 14px; font-weight: 700; color: #111827; }
.bonus-exp { font-size: 12px; color: #9ca3af; margin-top: 4px; }

/* History */
.empty-history { text-align: center; padding: 32px 0; font-size: 13px; color: #d1d5db; }
.history-list { display: flex; flex-direction: column; gap: 10px; }
.history-row {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px; border-radius: 12px;
  background: #f9fafb; border: 1px solid #f3f4f6;
  transition: all .15s;
}
.history-row:hover { border-color: #dbeafe; }
.h-icon { font-size: 22px; flex-shrink: 0; }
.h-info { flex: 1; min-width: 0; }
.h-pct { font-size: 14px; font-weight: 700; color: #111827; }
.h-date { font-size: 11px; color: #9ca3af; margin-top: 2px; }
.h-status-tag { font-size: 11px; font-weight: 600; padding: 3px 10px; border-radius: 50px; flex-shrink: 0; }
.h-status-tag.active { background: #dcfce7; color: #16a34a; }
.h-status-tag.used { background: #f1f5f9; color: #64748b; }
.h-status-tag.expired { background: #fef2f2; color: #dc2626; }

/* Buttons */
.btn-primary {
  display: inline-flex; align-items: center; justify-content: center;
  padding: 12px 32px; background: #111827; color: white;
  border: none; border-radius: 50px; font-size: 14px; font-weight: 700;
  font-family: inherit; cursor: pointer; transition: all .2s;
}
.btn-primary:hover { background: #2563eb; }

/* Modal */
.modal-backdrop {
  position: fixed; inset: 0; z-index: 1000;
  background: rgba(0,0,0,.5); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  animation: fadeIn .2s;
}
.modal-box {
  background: white; border-radius: 24px; padding: 48px 40px;
  text-align: center; max-width: 380px; width: 90%;
  box-shadow: 0 32px 80px rgba(0,0,0,.2);
  animation: popIn .3s cubic-bezier(.34,1.56,.64,1);
}
.modal-emoji { font-size: 64px; margin-bottom: 16px; animation: bounce .6s; }
.modal-box h3 { font-size: 24px; font-weight: 800; color: #111827; margin-bottom: 16px; }
.modal-pct { font-size: 72px; font-weight: 900; color: #2563eb; line-height: 1; margin-bottom: 16px; }
.modal-pct span { font-size: 28px; }
.modal-msg { font-size: 15px; color: #374151; margin-bottom: 8px; }
.modal-exp { font-size: 12px; color: #9ca3af; margin-bottom: 28px; }
@keyframes fadeIn { from{opacity:0} to{opacity:1} }
@keyframes popIn { from{transform:scale(.85);opacity:0} to{transform:scale(1);opacity:1} }
@keyframes bounce { 0%,100%{transform:scale(1)} 50%{transform:scale(1.12)} }
</style>