<template>
  <transition name="fade">
    <div v-if="isOpen" class="report-modal-overlay" @click.self="$emit('close')">
      <div class="report-modal">
        <div class="report-modal-header">
          <div class="report-mailbox">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z" />
              <path d="m4 8 7.4 4.63a2 2 0 0 0 2.2 0L21 8" />
              <path d="M4 16.5 9 13" />
              <path d="m20 16.5-5-3.5" />
            </svg>
          </div>
          <div>
            <div class="report-title">{{ year }}년 {{ month }}월 편지</div>
          </div>
          <button class="modal-close-btn" @click="$emit('close')" aria-label="리포트 닫기">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 6L6 18M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div class="report-letter">
          <div class="letter-top">
            <div class="letter-stamp">Monthly Letter</div>
            <div class="letter-tofrom">
              <div class="letter-line">
                <span>To.</span>
                {{ year }}년 {{ month }}월의 {{ receiverName }}
              </div>
              <div class="letter-line">
                <span>From.</span>
                몽글몽글
              </div>
            </div>
          </div>

          <div class="letter-body">
            <div v-if="isLoading" class="letter-placeholder">편지를 준비하는 중이에요...</div>
            <div v-else-if="error" class="letter-error">{{ error }}</div>
            <div v-else-if="report" class="letter-content" v-html="formattedReport"></div>
            <div v-else class="letter-placeholder">편지 내용을 여기에 담아 전달해 드릴게요.</div>
          </div>

          <div class="letter-footer">
            좋았던 꿈도, 조금 무서웠던 꿈도 모두 당신의 이야기예요. 다음 달에도 편지를 띄워요
            <svg class="footer-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79Z" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  year: {
    type: Number,
    required: true,
  },
  month: {
    type: Number,
    required: true,
  },
  report: {
    type: String,
    default: "",
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  receiverName: {
    type: String,
    default: "나에게",
  },
});

defineEmits(["close"]);

const formattedReport = computed(() => (props.report ? props.report.replace(/\n/g, "<br />") : ""));
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.report-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.28);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
  z-index: 50;
}

.report-modal {
  background: #fff;
  width: min(880px, 95vw);
  max-height: 90vh;
  overflow-y: auto;
  border-radius: 20px;
  box-shadow: 0 22px 48px rgba(0, 0, 0, 0.18);
  padding: 1.6rem;
  border: 1px solid rgba(205, 180, 219, 0.35);
}

.report-modal-header {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 1.1rem;
}

.report-mailbox {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #cdb4db, #a2d2ff);
  display: grid;
  place-items: center;
  color: #fff;
}

.report-title {
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.8rem;
  color: #4c2b7b;
  line-height: 1.05;
}

.modal-close-btn {
  margin-left: auto;
  border: none;
  background: none;
  cursor: pointer;
  color: #aaa;
  padding: 6px;
  border-radius: 10px;
}

.modal-close-btn:hover {
  background: #f6f3f9;
  color: #555;
}

.report-letter {
  margin-top: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 16px;
  background: #faf7ff;
  border: 1px solid rgba(205, 180, 219, 0.35);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6), 0 14px 30px rgba(205, 180, 219, 0.14);
  position: relative;
  overflow: hidden;
}

.report-letter::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(to bottom, transparent, transparent 26px, rgba(205, 180, 219, 0.12) 27px, rgba(205, 180, 219, 0.12) 28px);
  pointer-events: none;
}

.letter-top {
  display: flex;
  gap: 1rem;
  align-items: center;
  position: relative;
  z-index: 1;
  margin-bottom: 1rem;
  justify-content: space-between;
}

.letter-stamp {
  padding: 0.55rem 0.75rem;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(205, 180, 219, 0.35), rgba(162, 210, 255, 0.35));
  color: #5a3d7a;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.3rem;
  box-shadow: 0 6px 14px rgba(205, 180, 219, 0.26);
}

.letter-tofrom {
  display: flex;
  flex-direction: column;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.35rem;
  color: #5c4c79;
}

.letter-line span {
  color: #a26fb8;
  font-weight: 800;
  margin-right: 0.4rem;
}

.letter-body {
  position: relative;
  z-index: 1;
  background: rgba(250, 247, 255, 0.9);
  border-radius: 12px;
  border: 1px dashed rgba(205, 180, 219, 0.35);
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}

.letter-footer {
  margin-top: 0.9rem;
  text-align: right;
  font-family: "Dongle", sans-serif;
  font-weight: 700;
  font-size: 1.4rem;
  color: #7a5fa3;
  position: relative;
  z-index: 1;
}

.footer-icon {
  vertical-align: middle;
  color: #7a5fa3;
}

.letter-placeholder {
  min-height: 160px;
  padding: 1rem;
  background: rgba(252, 249, 255, 0.96);
  border-radius: 10px;
  color: #5c4c79;
  font-weight: 700;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.letter-content {
  min-height: 160px;
  padding: 1rem;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #4b3b67;
}

.letter-error {
  min-height: 160px;
  padding: 1rem;
  background: rgba(255, 235, 230, 0.9);
  border-radius: 10px;
  color: #c0392b;
  font-weight: 700;
  line-height: 1.6;
}

.report-modal::-webkit-scrollbar {
  width: 8px;
}

.report-modal::-webkit-scrollbar-thumb {
  background: rgba(205, 180, 219, 0.6);
  border-radius: 4px;
}
</style>

