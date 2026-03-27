<template>
  <div class="scm-root">

    <!-- ══════════════════════════════════════
         HEADER
    ══════════════════════════════════════ -->
    <div class="page-header">
      <div class="page-header__left">
        <div class="page-eyebrow">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          Quản lý khách hàng
        </div>
        <h1 class="page-title">Khách hàng tại quầy</h1>
        <p class="page-sub">Tìm khách hàng hiện có hoặc đăng ký mới khi mua tại cửa hàng</p>
      </div>
      <div class="page-header__actions">
        <button class="hdr-btn hdr-btn--amber" @click="openCreateModal">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Đăng ký khách mới
        </button>
      </div>
    </div>

    <!-- ══════════════════════════════════════
         MAIN CONTENT (2 columns)
    ══════════════════════════════════════ -->
    <div class="scm-body">

      <!-- ── LEFT: Search panel ── -->
      <div class="panel-left">

        <!-- Search box -->
        <div class="search-card">
          <div class="search-card__title">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
            </svg>
            Tìm khách hàng
          </div>
          <div class="search-bar">
            <div class="search-wrap" :class="{ focused: searchFocused }">
              <svg class="s-icon" width="15" height="15" viewBox="0 0 24 24" fill="none">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="1.8"/>
              </svg>
              <input
                ref="searchInput"
                v-model="query"
                class="s-input"
                placeholder="Tên / SĐT / Email..."
                autocomplete="off"
                spellcheck="false"
                @focus="searchFocused = true"
                @blur="searchFocused = false"
                @keyup.enter="doSearch"
                @input="onQueryInput"
              />
              <button v-if="query" class="s-clear" @click="resetSearch" tabindex="-1">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                  <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                  <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
              </button>
            </div>
            <button class="s-btn" @click="doSearch" :disabled="searchLoading || !query.trim()">
              <span v-if="!searchLoading">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="1.8"/>
                  <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
                Tìm
              </span>
              <span v-else class="spinner-sm"></span>
            </button>
          </div>

          <!-- Search error -->
          <transition name="fade-slide">
            <div v-if="searchError" class="error-bar">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.8"/>
                <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke="currentColor" stroke-width="1"/>
              </svg>
              {{ searchError }}
            </div>
          </transition>
        </div>

        <!-- Search results list -->
        <div v-if="searchDone" class="results-section">

          <!-- No results → prompt to create -->
          <div v-if="!searchResults.length && !searchLoading" class="no-result-card">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.4">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
            <p>Không tìm thấy khách hàng với "<strong>{{ lastQuery }}</strong>"</p>
            <span>Khách hàng chưa có tài khoản?</span>
            <button class="hdr-btn hdr-btn--amber" style="margin-top:4px" @click="openCreateModal(lastQuery)">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              Đăng ký ngay
            </button>
          </div>

          <!-- Result list -->
          <div v-else class="result-list">
            <div class="result-meta">
              <strong>{{ searchResults.length }}</strong> kết quả cho "{{ lastQuery }}"
            </div>
            <div
              v-for="cust in searchResults"
              :key="cust.id"
              class="result-row"
              :class="{ 'result-row--selected': selected?.id === cust.id }"
              @click="selectCustomer(cust)"
            >
              <div class="r-avatar" :class="cust.customerType === 'VIP' ? 'r-avatar--vip' : ''">
                {{ initials(cust.fullName) }}
              </div>
              <div class="r-info">
                <div class="r-name">{{ cust.fullName }}</div>
                <div class="r-meta">
                  <span v-if="cust.phone">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.89 12 19.79 19.79 0 0 1 1.87 3.18 2 2 0 0 1 3.84 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 8.91a16 16 0 0 0 6 6l.9-1.04a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                    </svg>
                    {{ cust.phone }}
                  </span>
                  <span v-if="cust.email">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                      <polyline points="22,6 12,13 2,6"/>
                    </svg>
                    {{ cust.email }}
                  </span>
                </div>
              </div>
              <div class="r-right">
                <span v-if="cust.customerType === 'VIP'" class="vip-badge">VIP</span>
                <span class="r-points">{{ (cust.loyaltyPoints ?? 0).toLocaleString() }} pts</span>
              </div>
            </div>
          </div>

        </div>

        <!-- Initial empty state -->
        <div v-else class="empty-hint">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#e2e8f0" stroke-width="1.3">
            <circle cx="11" cy="11" r="8"/><path d="M21 21l-4.35-4.35" stroke-linecap="round"/>
          </svg>
          <span>Nhập tên, SĐT hoặc email để tìm kiếm</span>
        </div>
      </div>

      <!-- ── RIGHT: Selected customer detail ── -->
      <div class="panel-right">

        <!-- No selection -->
        <div v-if="!selected" class="detail-placeholder">
          <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="#e2e8f0" stroke-width="1.2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <p>Chọn khách hàng từ kết quả tìm kiếm</p>
          <span>Thông tin chi tiết sẽ hiển thị tại đây</span>
        </div>

        <!-- Customer detail card -->
        <template v-else>
          <div class="detail-card">
            <!-- Card header -->
            <div class="detail-card__head" :class="selected.customerType === 'VIP' ? 'detail-card__head--vip' : ''">
              <div class="detail-avatar" :class="selected.customerType === 'VIP' ? 'detail-avatar--vip' : ''">
                {{ initials(selected.fullName) }}
              </div>
              <div class="detail-identity">
                <div class="detail-name">{{ selected.fullName }}</div>
                <div class="detail-badges">
                  <span v-if="selected.customerType === 'VIP'" class="badge badge--vip">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                    VIP
                  </span>
                  <span v-else class="badge badge--regular">Thường</span>
                  <span class="badge badge--id">ID #{{ selected.id }}</span>
                </div>
              </div>
              <button class="detail-close" @click="selected = null" title="Đóng">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>

            <!-- Points strip -->
            <div class="points-strip">
              <div class="points-item">
                <span class="points-val">{{ (selected.loyaltyPoints ?? 0).toLocaleString() }}</span>
                <span class="points-label">Điểm tích lũy</span>
              </div>
              <div class="points-divider"></div>
              <div class="points-item">
                <span class="points-val">{{ selected.totalOrders ?? '—' }}</span>
                <span class="points-label">Đơn hàng</span>
              </div>
              <div class="points-divider"></div>
              <div class="points-item">
                <span class="points-val">{{ formatMoney(selected.totalSpent) }}</span>
                <span class="points-label">Tổng chi tiêu</span>
              </div>
            </div>

            <!-- Info fields -->
            <div class="detail-fields">
              <div class="field-row" v-if="selected.phone">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.89 12 19.79 19.79 0 0 1 1.87 3.18 2 2 0 0 1 3.84 1h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 8.91a16 16 0 0 0 6 6l.9-1.04a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Số điện thoại</span>
                  <span class="field-val">{{ selected.phone }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.email">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Email</span>
                  <span class="field-val">{{ selected.email }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.dateOfBirth">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                    <line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Ngày sinh</span>
                  <span class="field-val">{{ formatDate(selected.dateOfBirth) }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.address">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Địa chỉ</span>
                  <span class="field-val">{{ selected.address }}</span>
                </div>
              </div>
              <div class="field-row" v-if="selected.createdAt">
                <div class="field-icon">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                </div>
                <div class="field-content">
                  <span class="field-label">Ngày đăng ký</span>
                  <span class="field-val">{{ formatDate(selected.createdAt) }}</span>
                </div>
              </div>
            </div>

            <!-- VIP note if any -->
            <div v-if="selected.vipNote" class="vip-note-strip">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              <span>{{ selected.vipNote }}</span>
            </div>

            <!-- Action buttons -->
            <div class="detail-actions">
              <button class="act-btn act-btn--primary" @click="linkToPos">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <rect x="2" y="3" width="20" height="14" rx="2"/>
                  <path d="M8 21h8M12 17v4" stroke-linecap="round"/>
                </svg>
                Dùng cho đơn POS
              </button>
              <button class="act-btn act-btn--outline" @click="copyPhone(selected.phone)" v-if="selected.phone">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
                Copy SĐT
              </button>
            </div>
          </div>
        </template>

      </div>
    </div>

    <!-- ══════════════════════════════════════
         CREATE CUSTOMER MODAL
    ══════════════════════════════════════ -->
    <Teleport to="body">
    <transition name="modal-fade">
      <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
        <div class="modal">

          <!-- Modal header -->
          <div class="modal-head">
            <div class="modal-head__left">
              <div class="modal-icon">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                  <line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
              </div>
              <div>
                <div class="modal-title">Đăng ký khách hàng mới</div>
                <div class="modal-sub">Tạo tài khoản nhanh cho khách mua tại cửa hàng</div>
              </div>
            </div>
            <button class="modal-close" @click="closeModal">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <!-- Modal body -->
          <div class="modal-body">

            <!-- Error -->
            <transition name="fade-slide">
              <div v-if="formError" class="form-error-bar">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12" stroke-linecap="round"/>
                  <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke-width="1"/>
                </svg>
                {{ formError }}
              </div>
            </transition>

            <!-- Required section -->
            <div class="form-section-label">Thông tin bắt buộc</div>

            <div class="form-row">
              <div class="form-group form-group--full">
                <label class="form-label">Họ và tên <span class="req">*</span></label>
                <input
                  v-model="form.fullName"
                  class="form-input"
                  :class="{ 'form-input--error': v.fullName }"
                  placeholder="Nguyễn Văn A"
                  @input="v.fullName = ''"
                />
                <span v-if="v.fullName" class="form-hint form-hint--err">{{ v.fullName }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Số điện thoại <span class="req">*</span></label>
                <input
                  v-model="form.phone"
                  class="form-input"
                  :class="{ 'form-input--error': v.phone }"
                  placeholder="09xxxxxxxx"
                  type="tel"
                  @input="v.phone = ''"
                />
                <span v-if="v.phone" class="form-hint form-hint--err">{{ v.phone }}</span>
              </div>
              <div class="form-group">
                <label class="form-label">Mật khẩu <span class="req">*</span></label>
                <div class="pw-wrap">
                  <input
                    v-model="form.password"
                    class="form-input"
                    :class="{ 'form-input--error': v.password }"
                    :type="showPw ? 'text' : 'password'"
                    placeholder="Tối thiểu 6 ký tự"
                    @input="v.password = ''"
                  />
                  <button class="pw-eye" type="button" @click="showPw = !showPw" tabindex="-1">
                    <svg v-if="!showPw" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
                <span v-if="v.password" class="form-hint form-hint--err">{{ v.password }}</span>
              </div>
            </div>

            <!-- Optional section -->
            <div class="form-section-label optional-label">
              Thông tin bổ sung
              <button class="toggle-optional" @click="showOptional = !showOptional">
                {{ showOptional ? 'Thu gọn' : 'Mở rộng' }}
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" :style="{ transform: showOptional ? 'rotate(180deg)' : '', transition: 'transform 0.2s' }">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
              </button>
            </div>

            <transition name="expand">
              <div v-if="showOptional" class="optional-fields">
                <div class="form-row">
                  <div class="form-group form-group--full">
                    <label class="form-label">Email</label>
                    <input
                      v-model="form.email"
                      class="form-input"
                      :class="{ 'form-input--error': v.email }"
                      placeholder="example@email.com"
                      type="email"
                      @input="v.email = ''"
                    />
                    <span v-if="v.email" class="form-hint form-hint--err">{{ v.email }}</span>
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label class="form-label">Ngày sinh</label>
                    <input
                      v-model="form.dateOfBirth"
                      class="form-input"
                      type="date"
                    />
                  </div>
                  <div class="form-group">
                    <label class="form-label">Giới tính</label>
                    <select v-model="form.gender" class="form-input form-select">
                      <option value="">Không xác định</option>
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
                    </select>
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group form-group--full">
                    <label class="form-label">Địa chỉ</label>
                    <input
                      v-model="form.address"
                      class="form-input"
                      placeholder="Số nhà, đường, phường/xã, quận/huyện..."
                    />
                  </div>
                </div>
              </div>
            </transition>

            <!-- Notice -->
            <div class="form-notice">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12" stroke-linecap="round"/>
                <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke-width="1"/>
              </svg>
              Tài khoản sẽ được tạo với vai trò <strong>Khách hàng</strong>. Khách có thể đăng nhập và xem lịch sử mua hàng sau khi đăng ký.
            </div>
          </div>

          <!-- Modal footer -->
          <div class="modal-foot">
            <button class="foot-btn foot-btn--cancel" @click="closeModal" :disabled="createLoading">Hủy</button>
            <button class="foot-btn foot-btn--create" @click="doCreate" :disabled="createLoading">
              <span v-if="!createLoading">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                  <line x1="19" y1="8" x2="19" y2="14"/><line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
                Đăng ký khách hàng
              </span>
              <span v-else class="spinner-sm spinner-sm--white"></span>
            </button>
          </div>
        </div>
      </div>
    </transition>
    </Teleport>
    <!-- ══════════════════════════════════════
         TOAST
    ══════════════════════════════════════ -->
    <Teleport to="body">
    <transition name="toast">
      <div v-if="toast.show" class="toast" :class="`toast--${toast.type}`">
        <svg v-if="toast.type === 'success'" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" stroke-linecap="round"/><polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12" stroke-linecap="round"/>
          <circle cx="12" cy="16" r="0.5" fill="currentColor" stroke-width="1"/>
        </svg>
        {{ toast.msg }}
      </div>
    </transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { customersApi } from "../../api/customers.api"; // ← import đúng named export

// ── State ─────────────────────────────────────────
const query         = ref("");
const lastQuery     = ref("");
const searchFocused = ref(false);
const searchLoading = ref(false);
const searchError   = ref("");
const searchResults = ref([]);
const searchDone    = ref(false);
const selected      = ref(null);

const showModal     = ref(false);
const showOptional  = ref(false);
const showPw        = ref(false);
const createLoading = ref(false);
const formError     = ref("");

const form = reactive({
  fullName: "", phone: "", password: "",
  email: "", dateOfBirth: "", gender: "", address: "",
});

const v = reactive({
  fullName: "", phone: "", password: "", email: "",
});

const toast = reactive({ show: false, msg: "", type: "success" });

// ── Helpers ───────────────────────────────────────
function initials(name = "") {
  return (name || "").split(" ").map(w => w[0] || "").filter(Boolean).slice(-2).join("").toUpperCase() || "KH";
}
function formatDate(str) {
  if (!str) return "—";
  const d = new Date(str);
  return isNaN(d) ? str : d.toLocaleDateString("vi-VN");
}
function formatMoney(val) {
  if (val === undefined || val === null) return "—";
  return Number(val).toLocaleString("vi-VN") + "₫";
}
function showToast(msg, type = "success") {
  toast.msg = msg; toast.type = type; toast.show = true;
  setTimeout(() => (toast.show = false), 3200);
}

// ── Search ────────────────────────────────────────
let debounceTimer = null;
function onQueryInput() {
  clearTimeout(debounceTimer);
  if (!query.value.trim()) {
    searchDone.value = false;
    searchResults.value = [];
    searchError.value = "";
  }
}

async function doSearch() {
  const q = query.value.trim();
  if (!q) return;
  lastQuery.value = q;
  searchLoading.value = true;
  searchError.value = "";
  searchDone.value = false;
  selected.value = null;
  try {
    // customersApi.listAll() → lấy toàn bộ, rồi filter phía client
    const res = await customersApi.listAll();                          // ← đổi từ getCustomers()
    const all = Array.isArray(res) ? res : (res?.data ?? res?.customers ?? []);

    const lower = q.toLowerCase();
    searchResults.value = all.filter(c =>
      (c.fullName  ?? "").toLowerCase().includes(lower) ||
      (c.phone     ?? "").includes(q) ||
      (c.email     ?? "").toLowerCase().includes(lower)
    );
    searchDone.value = true;
  } catch (e) {
    searchError.value = e?.response?.data?.message || e?.message || "Lỗi tìm kiếm. Vui lòng thử lại.";
  } finally {
    searchLoading.value = false;
  }
}

function resetSearch() {
  query.value = ""; lastQuery.value = "";
  searchResults.value = []; searchDone.value = false;
  searchError.value = ""; selected.value = null;
}

function selectCustomer(cust) { selected.value = cust; }

// ── Create ────────────────────────────────────────
function openCreateModal(prefill = "") {
  resetForm();
  if (typeof prefill === "string" && prefill) {
    if (/^[0-9+\s-]{7,}$/.test(prefill))  form.phone    = prefill;
    else if (prefill.includes("@"))         form.email    = prefill;
    else                                    form.fullName = prefill;
  }
  showModal.value = true;
}

function closeModal() { if (createLoading.value) return; showModal.value = false; }

function resetForm() {
  Object.assign(form, { fullName: "", phone: "", password: "", email: "", dateOfBirth: "", gender: "", address: "" });
  Object.assign(v,    { fullName: "", phone: "", password: "", email: "" });
  formError.value = ""; showPw.value = false; showOptional.value = false;
}

function validateForm() {
  let ok = true;
  if (!form.fullName.trim())                                            { v.fullName  = "Họ tên không được để trống"; ok = false; }
  if (!form.phone.trim())                                               { v.phone     = "Số điện thoại không được để trống"; ok = false; }
  else if (!/^[0-9+\s-]{7,15}$/.test(form.phone.trim()))               { v.phone     = "Số điện thoại không hợp lệ"; ok = false; }
  if (!form.password)                                                    { v.password  = "Mật khẩu không được để trống"; ok = false; }
  else if (form.password.length < 6)                                    { v.password  = "Mật khẩu tối thiểu 6 ký tự"; ok = false; }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))   { v.email     = "Email không hợp lệ"; ok = false; }
  return ok;
}

async function doCreate() {
  formError.value = "";
  if (!validateForm()) return;
  createLoading.value = true;
  try {
    const payload = {
      fullName: form.fullName.trim(),
      phone:    form.phone.trim(),
      password: form.password,
      role:     "CUSTOMER",
      ...(form.email       && { email:       form.email.trim() }),
      ...(form.dateOfBirth && { dateOfBirth: form.dateOfBirth }),
      ...(form.gender      && { gender:      form.gender }),
      ...(form.address     && { address:     form.address.trim() }),
    };
    const created = await customersApi.create(payload);               // ← đổi từ createCustomer()
    showModal.value = false;
    selected.value  = created?.data ?? created;
    showToast(`Đã đăng ký thành công cho ${form.fullName}!`, "success");
    if (searchDone.value) searchResults.value.unshift(selected.value);
  } catch (e) {
    formError.value = e?.response?.data?.message || e?.message || "Không thể tạo tài khoản. Vui lòng thử lại.";
  } finally {
    createLoading.value = false;
  }
}

// ── Actions ───────────────────────────────────────
function linkToPos() {
  if (!selected.value) return;
  try { sessionStorage.setItem("pos_customer", JSON.stringify(selected.value)); } catch {}
  showToast(`Đã gán khách hàng "${selected.value.fullName}" cho phiên POS`, "success");
}

async function copyPhone(phone) {
  if (!phone) return;
  try {
    await navigator.clipboard.writeText(phone);
    showToast("Đã copy số điện thoại", "success");
  } catch {
    showToast("Không thể copy", "error");
  }
}
</script>

<style scoped>
/* ══════════════════════════════════════════
   ROOT & LAYOUT
══════════════════════════════════════════ */
.scm-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  background: #f4f6f8;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px 0;
  flex-shrink: 0;
}
.page-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  color: #f59e0b;
  margin-bottom: 6px;
}
.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
  margin: 0 0 4px;
}
.page-sub { font-size: 13px; color: #64748b; margin: 0; }
.page-header__actions { display: flex; align-items: center; gap: 10px; flex-shrink: 0; padding-top: 6px; }

/* ── Body (2-col) ── */
.scm-body {
  flex: 1;
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;
  padding: 20px 28px 24px;
  overflow: hidden;
  min-height: 0;
}

/* ── Panels ── */
.panel-left {
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: #e2e8f0 transparent;
}
.panel-left::-webkit-scrollbar { width: 5px; }
.panel-left::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 99px; }

.panel-right {
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: thin;
  scrollbar-color: #e2e8f0 transparent;
}
.panel-right::-webkit-scrollbar { width: 5px; }
.panel-right::-webkit-scrollbar-thumb { background: #e2e8f0; border-radius: 99px; }

/* ══════════════════════════════════════════
   SEARCH CARD
══════════════════════════════════════════ */
.search-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  flex-shrink: 0;
}
.search-card__title {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
}

.search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}
.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 9px;
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 12px;
  height: 44px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-wrap.focused {
  border-color: #f59e0b;
  box-shadow: 0 0 0 3px rgba(245,158,11,0.08);
}
.s-icon { color: #94a3b8; flex-shrink: 0; }
.search-wrap.focused .s-icon { color: #f59e0b; }
.s-input {
  flex: 1; background: transparent; border: none; outline: none;
  color: #0f172a; font-size: 13.5px; font-family: inherit;
}
.s-input::placeholder { color: #cbd5e1; }
.s-clear {
  background: none; border: none; cursor: pointer;
  color: #94a3b8; display: flex; padding: 3px; transition: color 0.15s;
}
.s-clear:hover { color: #475569; }

.s-btn {
  height: 44px; padding: 0 18px;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; border-radius: 10px; color: #fff;
  font-size: 13px; font-weight: 700; cursor: pointer;
  transition: opacity 0.15s; font-family: inherit;
  display: flex; align-items: center; gap: 6px;
  white-space: nowrap; flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(245,158,11,0.25);
}
.s-btn span { display: flex; align-items: center; gap: 6px; }
.s-btn:hover:not(:disabled) { opacity: 0.88; }
.s-btn:disabled { opacity: 0.35; cursor: not-allowed; box-shadow: none; }

.error-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 12.5px;
  padding: 9px 12px; border-radius: 8px;
}

/* ── Results ── */
.results-section { display: flex; flex-direction: column; gap: 8px; }
.result-meta { font-size: 12px; color: #94a3b8; padding: 0 2px; }

.result-list { display: flex; flex-direction: column; gap: 6px; }

.result-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #ffffff;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}
.result-row:hover { border-color: #fde68a; background: #fffbeb; }
.result-row--selected {
  border-color: #f59e0b;
  background: #fffbeb;
  box-shadow: 0 2px 8px rgba(245,158,11,0.12);
}

.r-avatar {
  width: 38px; height: 38px; border-radius: 9px;
  background: linear-gradient(135deg, #94a3b8, #64748b);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 700; color: #fff; flex-shrink: 0;
  letter-spacing: -0.5px;
}
.r-avatar--vip { background: linear-gradient(135deg, #f59e0b, #d97706); }

.r-info { flex: 1; min-width: 0; }
.r-name { font-size: 14px; font-weight: 700; color: #0f172a; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.r-meta { display: flex; gap: 12px; margin-top: 2px; flex-wrap: wrap; }
.r-meta span { display: flex; align-items: center; gap: 4px; font-size: 11.5px; color: #64748b; }

.r-right { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0; }
.vip-badge {
  font-size: 10px; font-weight: 700; color: #b45309;
  background: #fef3c7; border: 1px solid #fde68a;
  padding: 2px 8px; border-radius: 10px;
}
.r-points { font-size: 11.5px; font-weight: 600; color: #d97706; }

/* No result */
.no-result-card {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; padding: 30px 20px; text-align: center;
  background: #ffffff; border: 1px dashed #e2e8f0;
  border-radius: 12px; color: #94a3b8;
}
.no-result-card p { font-size: 13.5px; font-weight: 600; color: #475569; margin: 4px 0 0; }
.no-result-card span { font-size: 12px; color: #94a3b8; }

/* Empty hint */
.empty-hint {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 10px;
  padding: 40px 20px; color: #94a3b8;
  text-align: center; font-size: 13px;
}

/* ══════════════════════════════════════════
   DETAIL PANEL
══════════════════════════════════════════ */
.detail-placeholder {
  height: 100%; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 12px;
  text-align: center; color: #94a3b8;
}
.detail-placeholder p { font-size: 15px; font-weight: 600; color: #64748b; margin: 0; }
.detail-placeholder span { font-size: 13px; }

/* Detail card */
.detail-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.detail-card__head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 22px;
  background: linear-gradient(135deg, #1e293b, #0f172a);
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.detail-card__head--vip {
  background: linear-gradient(135deg, #451a03, #78350f 55%, #92400e);
}

.detail-avatar {
  width: 52px; height: 52px; border-radius: 12px;
  background: linear-gradient(135deg, #475569, #334155);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 800; color: #fff;
  flex-shrink: 0; letter-spacing: -0.5px;
  border: 2px solid rgba(255,255,255,0.12);
}
.detail-avatar--vip { background: linear-gradient(135deg, #f59e0b, #d97706); border-color: rgba(253,230,138,0.3); }

.detail-identity { flex: 1; min-width: 0; }
.detail-name { font-size: 19px; font-weight: 800; color: #f1f5f9; letter-spacing: -0.4px; }
.detail-badges { display: flex; align-items: center; gap: 7px; margin-top: 5px; flex-wrap: wrap; }

.badge {
  font-size: 10px; font-weight: 700; letter-spacing: 0.05em;
  padding: 2px 9px; border-radius: 12px;
}
.badge--vip {
  display: inline-flex; align-items: center; gap: 4px;
  background: rgba(245,158,11,0.18); color: #fbbf24;
  border: 1px solid rgba(245,158,11,0.3);
}
.badge--regular { background: rgba(255,255,255,0.08); color: #94a3b8; border: 1px solid rgba(255,255,255,0.1); }
.badge--id { background: rgba(255,255,255,0.06); color: #64748b; border: 1px solid rgba(255,255,255,0.08); font-family: monospace; }

.detail-close {
  width: 30px; height: 30px; border-radius: 50%;
  background: rgba(255,255,255,0.07); border: 1px solid rgba(255,255,255,0.1);
  color: #94a3b8; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s; flex-shrink: 0;
}
.detail-close:hover { background: rgba(255,255,255,0.13); color: #f1f5f9; }

/* Points strip */
.points-strip {
  display: flex;
  align-items: center;
  padding: 16px 22px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f2f5;
  gap: 0;
}
.points-item { flex: 1; text-align: center; }
.points-val { display: block; font-size: 20px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; }
.points-label { display: block; font-size: 10.5px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.07em; margin-top: 2px; }
.points-divider { width: 1px; height: 36px; background: #e2e8f0; flex-shrink: 0; }

/* Fields */
.detail-fields { display: flex; flex-direction: column; gap: 0; padding: 6px 0; }
.field-row {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 12px 22px;
  border-bottom: 1px solid #f8fafc;
  transition: background 0.1s;
}
.field-row:hover { background: #fafbfc; }
.field-row:last-child { border-bottom: none; }
.field-icon { width: 28px; height: 28px; border-radius: 7px; background: #f0f2f5; display: flex; align-items: center; justify-content: center; color: #64748b; flex-shrink: 0; margin-top: 1px; }
.field-content { display: flex; flex-direction: column; gap: 1px; }
.field-label { font-size: 10.5px; font-weight: 700; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.06em; }
.field-val { font-size: 13.5px; color: #0f172a; font-weight: 500; }

/* VIP note */
.vip-note-strip {
  display: flex; align-items: flex-start; gap: 8px;
  margin: 0 22px 0; padding: 10px 14px;
  background: #fffbeb; border: 1px solid #fde68a;
  border-radius: 8px; font-size: 12.5px; color: #92400e;
  line-height: 1.5;
}
.vip-note-strip svg { color: #f59e0b; flex-shrink: 0; margin-top: 2px; }

/* Actions */
.detail-actions {
  display: flex; gap: 10px; padding: 16px 22px;
  border-top: 1px solid #f0f2f5; flex-wrap: wrap;
}
.act-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: 9px; font-size: 13px;
  font-weight: 700; cursor: pointer; font-family: inherit;
  transition: all 0.15s; border: none;
}
.act-btn--primary {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff; box-shadow: 0 2px 8px rgba(245,158,11,0.25);
}
.act-btn--primary:hover { opacity: 0.88; }
.act-btn--outline {
  background: #fff; color: #374151;
  border: 1.5px solid #e2e8f0;
}
.act-btn--outline:hover { border-color: #f59e0b; color: #d97706; background: #fffbeb; }

/* ══════════════════════════════════════════
   HEADER BUTTONS
══════════════════════════════════════════ */
.hdr-btn {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 18px; border-radius: 9px;
  font-size: 13px; font-weight: 700;
  cursor: pointer; border: none; font-family: inherit;
  transition: opacity 0.15s;
}
.hdr-btn--amber {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff; box-shadow: 0 2px 8px rgba(245,158,11,0.28);
}
.hdr-btn--amber:hover { opacity: 0.88; }

/* ══════════════════════════════════════════
   MODAL
══════════════════════════════════════════ */
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(15,23,42,0.55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 500; padding: 20px;
}
.modal {
  width: 100%; max-width: 540px;
  background: #ffffff;
  border-radius: 16px;
  display: flex; flex-direction: column;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
  overflow: hidden;
  max-height: 90vh;
  position: fixed;         /* Thay đổi từ mặc định sang fixed */
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* Dịch chuyển ngược lại 50% kích thước chính nó */
  
  /* Đảm bảo không bị vỡ layout khi dùng fixed */
  margin: 0;
}

.modal-head {
  display: flex; align-items: center; justify-content: space-between;
  padding: 18px 22px 14px;
  border-bottom: 1px solid #f0f2f5;
  flex-shrink: 0;
}
.modal-head__left { display: flex; align-items: center; gap: 12px; }
.modal-icon {
  width: 38px; height: 38px; border-radius: 10px;
  background: linear-gradient(135deg, #fef3c7, #fde68a);
  display: flex; align-items: center; justify-content: center;
  color: #d97706; flex-shrink: 0;
}
.modal-title { font-size: 15px; font-weight: 800; color: #0f172a; }
.modal-sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.modal-close {
  width: 30px; height: 30px; border-radius: 50%;
  background: #f4f6f8; border: 1px solid #e2e8f0;
  color: #64748b; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
}
.modal-close:hover { background: #e2e8f0; color: #0f172a; }

.modal-body { padding: 18px 22px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }

/* Form */
.form-section-label {
  font-size: 10.5px; font-weight: 700; color: #9ca3af;
  text-transform: uppercase; letter-spacing: 0.08em;
  display: flex; align-items: center; justify-content: space-between;
}
.optional-label { margin-top: 2px; }
.toggle-optional {
  display: inline-flex; align-items: center; gap: 4px;
  background: none; border: none; color: #f59e0b;
  font-size: 11.5px; font-weight: 600; cursor: pointer;
  font-family: inherit; padding: 0;
  transition: color 0.15s;
}
.toggle-optional:hover { color: #d97706; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group--full { grid-column: 1 / -1; }
.form-label { font-size: 12px; font-weight: 600; color: #374151; }
.req { color: #dc2626; }
.form-input {
  height: 40px; padding: 0 12px;
  border: 1.5px solid #e2e8f0; border-radius: 8px;
  font-size: 13.5px; color: #0f172a; font-family: inherit;
  outline: none; background: #fff;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.form-input:focus { border-color: #f59e0b; box-shadow: 0 0 0 3px rgba(245,158,11,0.08); }
.form-input--error { border-color: #fca5a5; }
.form-input--error:focus { box-shadow: 0 0 0 3px rgba(239,68,68,0.08); }
.form-select { appearance: none; cursor: pointer; }
.form-hint { font-size: 11.5px; margin-top: 1px; }
.form-hint--err { color: #dc2626; }

.pw-wrap { position: relative; }
.pw-wrap .form-input { width: 100%; padding-right: 38px; box-sizing: border-box; }
.pw-eye {
  position: absolute; right: 10px; top: 50%; transform: translateY(-50%);
  background: none; border: none; color: #94a3b8; cursor: pointer;
  display: flex; padding: 4px; transition: color 0.15s;
}
.pw-eye:hover { color: #475569; }

.optional-fields { display: flex; flex-direction: column; gap: 12px; }

.form-error-bar {
  display: flex; align-items: flex-start; gap: 9px;
  background: #fef2f2; border: 1px solid #fecaca;
  color: #dc2626; font-size: 13px;
  padding: 10px 14px; border-radius: 8px;
}

.form-notice {
  display: flex; align-items: flex-start; gap: 8px;
  background: #eff6ff; border: 1px solid #bfdbfe;
  color: #1d4ed8; font-size: 12px;
  padding: 10px 14px; border-radius: 8px; line-height: 1.5;
}

.modal-foot {
  display: flex; gap: 10px; justify-content: flex-end;
  padding: 14px 22px;
  border-top: 1px solid #f0f2f5;
  flex-shrink: 0;
}
.foot-btn {
  height: 40px; padding: 0 22px; border-radius: 9px;
  font-size: 13px; font-weight: 700; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
  display: inline-flex; align-items: center; gap: 8px;
}
.foot-btn--cancel {
  background: #f4f6f8; border: 1px solid #e2e8f0; color: #475569;
}
.foot-btn--cancel:hover:not(:disabled) { border-color: #cbd5e1; }
.foot-btn--create {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border: none; color: #fff;
  box-shadow: 0 2px 8px rgba(245,158,11,0.28);
  min-width: 170px; justify-content: center;
}
.foot-btn--create:hover:not(:disabled) { opacity: 0.88; }
.foot-btn:disabled { opacity: 0.4; cursor: not-allowed; box-shadow: none; }

/* ══════════════════════════════════════════
   TOAST
══════════════════════════════════════════ */
.toast {
  position: fixed; bottom: 28px; right: 28px;
  display: flex; align-items: center; gap: 10px;
  font-size: 13.5px; font-weight: 600;
  padding: 12px 18px; border-radius: 10px;
  z-index: 600; pointer-events: none;
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
}
.toast--success {
  background: #ecfdf5; border: 1px solid #6ee7b7; color: #065f46;
}
.toast--error {
  background: #fef2f2; border: 1px solid #fecaca; color: #dc2626;
}

/* ══════════════════════════════════════════
   SPINNERS
══════════════════════════════════════════ */
.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(0,0,0,0.15);
  border-top-color: #f59e0b;
  border-radius: 50%;
  animation: scm-spin 0.6s linear infinite;
  display: inline-block;
}
.spinner-sm--white {
  border-color: rgba(255,255,255,0.3);
  border-top-color: #fff;
}
@keyframes scm-spin { to { transform: rotate(360deg); } }

/* ══════════════════════════════════════════
   TRANSITIONS
══════════════════════════════════════════ */
.fade-slide-enter-active { transition: all 0.22s ease; }
.fade-slide-leave-active { transition: all 0.14s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(-5px); }
.fade-slide-leave-to { opacity: 0; }

.expand-enter-active { transition: all 0.25s ease; overflow: hidden; }
.expand-leave-active { transition: all 0.18s ease; overflow: hidden; }
.expand-enter-from { opacity: 0; max-height: 0; }
.expand-enter-to { max-height: 400px; }
.expand-leave-to { opacity: 0; max-height: 0; }

.modal-fade-enter-active { transition: all 0.25s ease; }
.modal-fade-leave-active { transition: all 0.18s ease; }
.modal-fade-enter-from { opacity: 0; }
.modal-fade-leave-to { opacity: 0; }
.modal-fade-enter-active .modal { animation: modal-pop 0.28s cubic-bezier(0.34,1.3,0.64,1); }
@keyframes modal-pop {
  from { transform: scale(0.92) translateY(12px); opacity: 0; }
  to   { transform: scale(1) translateY(0); opacity: 1; }
}

.toast-enter-active { transition: all 0.3s cubic-bezier(0.34,1.3,0.64,1); }
.toast-leave-active { transition: all 0.2s ease; }
.toast-enter-from { opacity: 0; transform: translateY(16px) scale(0.95); }
.toast-leave-to { opacity: 0; transform: translateY(8px); }

/* ══════════════════════════════════════════
   RESPONSIVE
══════════════════════════════════════════ */
@media (max-width: 900px) {
  .scm-body { grid-template-columns: 1fr; }
  .panel-right { min-height: 300px; }
  .page-header { padding: 16px 18px 0; flex-wrap: wrap; }
  .scm-body { padding: 16px 18px 20px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>