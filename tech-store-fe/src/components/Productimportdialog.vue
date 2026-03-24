<template>
  <Teleport to="body">
    <Transition name="imp-fade">
      <div v-if="open" class="imp-overlay" @click.self="close">
        <div class="imp-modal">

          <!-- Left accent strip -->
          <div class="imp-strip">
            <div class="imp-strip__icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="12" y1="18" x2="12" y2="12"/>
                <line x1="9" y1="15" x2="15" y2="15"/>
              </svg>
            </div>
            <div class="imp-strip__line"></div>
            <div class="imp-strip__dot imp-strip__dot--1"></div>
            <div class="imp-strip__dot imp-strip__dot--2"></div>
          </div>

          <!-- Main content -->
          <div class="imp-body">
            <!-- Header -->
            <div class="imp-head">
              <div>
                <div class="imp-eyebrow">Nhập hàng loạt</div>
                <h2 class="imp-title">Import Excel</h2>
              </div>
              <button class="imp-close" @click="close">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>

            <!-- Steps -->
            <div class="imp-steps">
              <!-- Step 1 -->
              <div class="imp-step">
                <div class="imp-step__badge">01</div>
                <div class="imp-step__block">
                  <div class="imp-step__label">Tải file mẫu</div>
                  <div class="imp-step__desc">Điền đúng cột theo template — dữ liệu bắt đầu từ dòng 3</div>
                  <button class="imp-dl-btn" @click="downloadTemplate">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                      <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                      <polyline points="7 10 12 15 17 10"/>
                      <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    product_import_template.xlsx
                  </button>
                </div>
              </div>

              <div class="imp-connector"><div class="imp-connector__line"></div></div>

              <!-- Step 2 -->
              <div class="imp-step">
                <div class="imp-step__badge">02</div>
                <div class="imp-step__block" style="flex:1">
                  <div class="imp-step__label">Upload file</div>
                  <div class="imp-step__desc">Kéo thả hoặc click để chọn — chỉ hỗ trợ .xlsx · tối đa 5MB</div>

                  <label
                    class="imp-zone"
                    :class="{ 'imp-zone--drag': isDragging, 'imp-zone--ready': selectedFile }"
                    @dragover.prevent="isDragging = true"
                    @dragleave="isDragging = false"
                    @drop.prevent="onDrop"
                  >
                    <input type="file" accept=".xlsx,.xls" style="display:none" @change="onPick" ref="fileInput"/>

                    <template v-if="!selectedFile">
                      <div class="imp-zone__icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                          <polyline points="17 8 12 3 7 8"/>
                          <line x1="12" y1="3" x2="12" y2="15"/>
                          <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                        </svg>
                      </div>
                      <span class="imp-zone__text">Kéo file vào đây <em>hoặc click để chọn</em></span>
                    </template>

                    <template v-else>
                      <div class="imp-zone__file">
                        <div class="imp-zone__file-ico">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
                            <polyline points="14 2 14 8 20 8"/>
                          </svg>
                        </div>
                        <div>
                          <div class="imp-zone__filename">{{ selectedFile.name }}</div>
                          <div class="imp-zone__filesize">{{ formatBytes(selectedFile.size) }}</div>
                        </div>
                        <button class="imp-zone__remove" @click.prevent="clearFile">
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                          </svg>
                        </button>
                      </div>
                    </template>
                  </label>
                </div>
              </div>
            </div>

            <!-- Result -->
            <Transition name="imp-result-in">
              <div v-if="result" class="imp-result">
                <div class="imp-result__stats">
                  <div class="imp-stat">
                    <span class="imp-stat__n">{{ result.totalRows }}</span>
                    <span class="imp-stat__l">Tổng dòng</span>
                  </div>
                  <div class="imp-stat imp-stat--ok">
                    <span class="imp-stat__n">{{ result.successCount }}</span>
                    <span class="imp-stat__l">Thành công</span>
                  </div>
                  <div class="imp-stat imp-stat--err">
                    <span class="imp-stat__n">{{ result.failCount }}</span>
                    <span class="imp-stat__l">Thất bại</span>
                  </div>
                </div>
                <div v-if="result.failCount === 0" class="imp-result__ok">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"/></svg>
                  Tất cả sản phẩm đã được nhập thành công
                </div>
                <div v-if="result.errors?.length" class="imp-result__errs">
                  <div class="imp-result__errs-head">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    {{ result.errors.length }} lỗi cần xem lại
                  </div>
                  <div class="imp-result__errs-list">
                    <div v-for="(e, i) in result.errors" :key="i" class="imp-result__err-row">
                      <span class="imp-result__err-dot"></span>{{ e }}
                    </div>
                  </div>
                </div>
              </div>
            </Transition>

            <!-- Footer -->
            <div class="imp-footer">
              <button class="imp-btn imp-btn--ghost" @click="close">Đóng</button>
              <button class="imp-btn imp-btn--primary" :disabled="!selectedFile || loading" @click="submit">
                <span v-if="loading" class="imp-spinner"></span>
                <template v-else>
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                    <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                  </svg>
                </template>
                {{ loading ? 'Đang nhập…' : 'Bắt đầu nhập' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const props = defineProps({ open: Boolean })
const emit  = defineEmits(['close', 'imported'])

const selectedFile = ref(null)
const isDragging   = ref(false)
const loading      = ref(false)
const result       = ref(null)
const fileInput    = ref(null)
function close() { emit('close'); clearFile(); result.value = null }
function clearFile() { selectedFile.value = null; if (fileInput.value) fileInput.value.value = '' }
function onPick(e) { const f = e.target.files?.[0]; if (f) { selectedFile.value = f; result.value = null } }
function onDrop(e) {
  isDragging.value = false
  const f = e.dataTransfer.files?.[0]
  if (f && (f.name.endsWith('.xlsx') || f.name.endsWith('.xls'))) { selectedFile.value = f; result.value = null }
}
function formatBytes(b) {
  if (b < 1024) return b + ' B'
  if (b < 1048576) return (b/1024).toFixed(1) + ' KB'
  return (b/1048576).toFixed(1) + ' MB'
}
import { productsApi } from '../api/products.api.js'

// Thay downloadTemplate()
async function downloadTemplate() {
  try {
    const res = await productsApi.downloadTemplate()
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url; a.download = 'product_import_template.xlsx'; a.click()
    URL.revokeObjectURL(url)
  } catch { alert('Không thể tải file mẫu') }
}

// Thay submit()
async function submit() {
  if (!selectedFile.value) return
  loading.value = true; result.value = null
  try {
    const res = await productsApi.importFromExcel(selectedFile.value)
    result.value = res.data
    if (res.data.successCount > 0) emit('imported')
  } catch (e) {
    result.value = { totalRows: 0, successCount: 0, failCount: 0,
      errors: ['Lỗi kết nối server: ' + (e.response?.data?.message || e.message)] }
  } finally { loading.value = false }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Sora:wght@400;500;600;700;800&display=swap');

.imp-overlay {
  position: fixed; inset: 0;
  background: rgba(8,10,18,0.6);
  backdrop-filter: blur(10px);
  display: flex; align-items: center; justify-content: center;
  z-index: 9999; padding: 20px;
}
.imp-modal {
  display: flex; width: 100%; max-width: 560px;
  background: #0d1117;
  border-radius: 20px;
  border: 1px solid rgba(255,255,255,0.07);
  box-shadow: 0 40px 100px rgba(0,0,0,0.7), 0 0 0 1px rgba(255,255,255,0.03), inset 0 1px 0 rgba(255,255,255,0.05);
  overflow: hidden;
  font-family: 'Sora', sans-serif;
}
.imp-strip {
  width: 54px; flex-shrink: 0;
  background: linear-gradient(180deg, #1a56db 0%, #0e3a99 100%);
  display: flex; flex-direction: column; align-items: center;
  padding: 24px 0; gap: 10px;
}
.imp-strip__icon {
  width: 36px; height: 36px; border-radius: 10px;
  background: rgba(255,255,255,0.15); border: 1px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center; color: white;
}
.imp-strip__line { flex: 1; width: 1px; background: linear-gradient(180deg, rgba(255,255,255,0.2) 0%, transparent 100%); }
.imp-strip__dot { width: 5px; height: 5px; border-radius: 50%; background: rgba(255,255,255,0.25); }
.imp-strip__dot--1 { animation: pdot 2s ease-in-out infinite; }
.imp-strip__dot--2 { animation: pdot 2s ease-in-out 0.5s infinite; }
@keyframes pdot { 0%,100%{opacity:0.25;transform:scale(1)} 50%{opacity:1;transform:scale(1.5)} }

.imp-body {
  flex: 1; padding: 26px 26px 22px;
  display: flex; flex-direction: column; gap: 18px;
  overflow-y: auto; max-height: 88vh;
}
.imp-head { display: flex; align-items: flex-start; justify-content: space-between; }
.imp-eyebrow { font-size: 10px; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: #3b82f6; margin-bottom: 5px; }
.imp-title { font-size: 21px; font-weight: 800; color: #f1f5f9; margin: 0; letter-spacing: -0.02em; }
.imp-close {
  width: 30px; height: 30px; border-radius: 8px;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.07);
  color: #475569; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s;
}
.imp-close:hover { background: rgba(239,68,68,0.12); color: #f87171; border-color: rgba(239,68,68,0.25); }

.imp-steps { display: flex; flex-direction: column; }
.imp-step { display: flex; gap: 14px; align-items: flex-start; }
.imp-step__badge {
  width: 30px; height: 30px; border-radius: 8px; flex-shrink: 0;
  background: rgba(59,130,246,0.1); border: 1px solid rgba(59,130,246,0.25);
  font-size: 10.5px; font-weight: 800; color: #60a5fa; letter-spacing: 0.04em;
  display: flex; align-items: center; justify-content: center;
}
.imp-step__block { flex: 1; }
.imp-step__label { font-size: 13px; font-weight: 700; color: #e2e8f0; margin-bottom: 3px; }
.imp-step__desc  { font-size: 11.5px; color: #475569; line-height: 1.5; }
.imp-connector { padding: 7px 0 7px 15px; }
.imp-connector__line { width: 1px; height: 18px; background: rgba(255,255,255,0.05); }

.imp-dl-btn {
  display: inline-flex; align-items: center; gap: 7px; margin-top: 10px;
  padding: 7px 14px; border-radius: 8px; cursor: pointer; transition: all 0.15s;
  background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.09);
  color: #64748b; font-family: 'Sora', sans-serif; font-size: 11.5px; font-weight: 600;
}
.imp-dl-btn:hover { background: rgba(59,130,246,0.1); border-color: rgba(59,130,246,0.35); color: #60a5fa; }

.imp-zone {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; margin-top: 10px; padding: 22px 20px;
  border: 1.5px dashed rgba(255,255,255,0.09); border-radius: 12px;
  background: rgba(255,255,255,0.015); cursor: pointer; transition: all 0.2s; text-align: center; min-height: 96px;
}
.imp-zone:hover { border-color: rgba(59,130,246,0.4); background: rgba(59,130,246,0.04); }
.imp-zone--drag  { border-color: #3b82f6; background: rgba(59,130,246,0.07); }
.imp-zone--ready { border-style: solid; border-color: rgba(22,163,74,0.35); background: rgba(22,163,74,0.04); }
.imp-zone__icon {
  width: 36px; height: 36px; border-radius: 9px;
  background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.07);
  display: flex; align-items: center; justify-content: center; color: #334155;
}
.imp-zone__text { font-size: 12.5px; color: #475569; font-weight: 500; }
.imp-zone__text em { font-style: normal; color: #60a5fa; }
.imp-zone__file { display: flex; align-items: center; gap: 12px; width: 100%; }
.imp-zone__file-ico {
  width: 34px; height: 34px; border-radius: 8px; flex-shrink: 0;
  background: rgba(22,163,74,0.1); border: 1px solid rgba(22,163,74,0.22);
  display: flex; align-items: center; justify-content: center; color: #4ade80;
}
.imp-zone__filename { font-size: 12.5px; font-weight: 700; color: #e2e8f0; }
.imp-zone__filesize { font-size: 11px; color: #475569; margin-top: 1px; }
.imp-zone__remove {
  margin-left: auto; width: 26px; height: 26px; border-radius: 7px;
  background: rgba(239,68,68,0.08); border: 1px solid rgba(239,68,68,0.18);
  color: #f87171; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s;
}
.imp-zone__remove:hover { background: rgba(239,68,68,0.18); }

.imp-result { display: flex; flex-direction: column; gap: 9px; }
.imp-result__stats { display: grid; grid-template-columns: repeat(3,1fr); gap: 8px; }
.imp-stat {
  padding: 12px 8px; border-radius: 10px; text-align: center;
  background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.06);
}
.imp-stat--ok  { background: rgba(22,163,74,0.06);  border-color: rgba(22,163,74,0.18); }
.imp-stat--err { background: rgba(220,38,38,0.06);  border-color: rgba(220,38,38,0.18); }
.imp-stat__n { display: block; font-size: 22px; font-weight: 800; color: #e2e8f0; line-height: 1.1; }
.imp-stat--ok  .imp-stat__n { color: #4ade80; }
.imp-stat--err .imp-stat__n { color: #f87171; }
.imp-stat__l { display: block; font-size: 10.5px; font-weight: 600; color: #334155; margin-top: 3px; letter-spacing: 0.03em; }

.imp-result__ok {
  display: flex; align-items: center; gap: 8px; padding: 10px 14px;
  background: rgba(22,163,74,0.07); border: 1px solid rgba(22,163,74,0.18);
  border-radius: 8px; font-size: 12.5px; font-weight: 700; color: #4ade80;
}
.imp-result__errs { border: 1px solid rgba(220,38,38,0.18); border-radius: 10px; overflow: hidden; }
.imp-result__errs-head {
  display: flex; align-items: center; gap: 7px; padding: 9px 14px;
  background: rgba(220,38,38,0.07); color: #f87171; font-size: 12px; font-weight: 700;
  border-bottom: 1px solid rgba(220,38,38,0.12);
}
.imp-result__errs-list { max-height: 140px; overflow-y: auto; padding: 5px 0; }
.imp-result__err-row { display: flex; align-items: flex-start; gap: 8px; padding: 5px 14px; font-size: 11.5px; color: #64748b; }
.imp-result__err-dot { width: 4px; height: 4px; border-radius: 50%; background: #ef4444; flex-shrink: 0; margin-top: 6px; }

.imp-footer {
  display: flex; align-items: center; justify-content: flex-end; gap: 8px;
  padding-top: 4px; border-top: 1px solid rgba(255,255,255,0.05);
}
.imp-btn {
  display: inline-flex; align-items: center; gap: 7px; padding: 9px 20px;
  border-radius: 9px; border: none; font-family: 'Sora', sans-serif;
  font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.18s;
}
.imp-btn--ghost { background: transparent; color: #475569; border: 1px solid rgba(255,255,255,0.07); }
.imp-btn--ghost:hover { background: rgba(255,255,255,0.04); color: #64748b; }
.imp-btn--primary { background: #1a56db; color: white; box-shadow: 0 4px 16px rgba(26,86,219,0.35); }
.imp-btn--primary:hover:not(:disabled) { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 6px 22px rgba(26,86,219,0.45); }
.imp-btn--primary:disabled { opacity: 0.35; cursor: not-allowed; transform: none; box-shadow: none; }
.imp-spinner {
  width: 13px; height: 13px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.2); border-top-color: white;
  animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.imp-fade-enter-active { transition: opacity 0.2s ease; }
.imp-fade-leave-active { transition: opacity 0.18s ease; }
.imp-fade-enter-active .imp-modal { transition: transform 0.3s cubic-bezier(0.34,1.3,0.64,1), opacity 0.2s ease; }
.imp-fade-leave-active .imp-modal { transition: transform 0.18s ease, opacity 0.18s ease; }
.imp-fade-enter-from, .imp-fade-leave-to { opacity: 0; }
.imp-fade-enter-from .imp-modal { transform: scale(0.93) translateY(14px); opacity: 0; }
.imp-fade-leave-to   .imp-modal { transform: scale(0.97) translateY(6px); opacity: 0; }
.imp-result-in-enter-active { transition: all 0.25s cubic-bezier(0.34,1.2,0.64,1); }
.imp-result-in-leave-active { transition: all 0.15s ease; }
.imp-result-in-enter-from { opacity: 0; transform: translateY(10px); }
.imp-result-in-leave-to   { opacity: 0; }
</style>