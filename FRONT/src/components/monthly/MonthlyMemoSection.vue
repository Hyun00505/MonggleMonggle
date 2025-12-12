<template>
  <div class="section-card memo-section">
    <div class="section-header">
      <h3 class="section-title">
        <span class="title-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </span>
        이번 달 회고
      </h3>
      <button @click="startAddMemo" class="add-memo-btn">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        메모 추가
      </button>
    </div>

    <!-- 새 메모 작성 -->
    <div v-if="isAddingMemo" class="memo-note new-memo">
      <textarea v-model="newMemoContent" class="memo-textarea" placeholder="이번 달 꿈 기록을 되돌아보며 느낀 점을 자유롭게 작성해보세요..." ref="newMemoInput" />
      <div class="memo-note-actions">
        <button @click="addNewMemo" class="save-btn" :disabled="!newMemoContent.trim()">저장</button>
        <button @click="cancelAddMemo" class="cancel-btn">취소</button>
      </div>
    </div>

    <!-- 메모 목록 -->
    <div v-if="memos.length > 0" class="memo-list">
      <div v-for="memo in memos" :key="memo.id" class="memo-note" :class="getMemoColorClass(memo.id)">
        <div v-if="editingMemoId !== memo.id" class="memo-note-content">
          <p :class="{ 'memo-text-clamped': !isExpanded(memo.id) }">{{ memo.content }}</p>
          <button v-if="shouldShowExpandBtn(memo.content)" @click="toggleExpand(memo.id)" class="expand-btn">
            {{ isExpanded(memo.id) ? "접기" : "더보기" }}
          </button>
          <div class="memo-note-footer">
            <span class="memo-date">{{ formatDate(memo.createdAt) }}</span>
            <div class="memo-note-btns">
              <button @click="startEditMemo(memo)" class="memo-action-btn edit">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button @click="deleteMemoItem(memo.id)" class="memo-action-btn delete">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="memo-note-edit">
          <textarea v-model="editMemoContent" class="memo-textarea" />
          <div class="memo-note-actions">
            <button @click="saveEditMemo" class="save-btn" :disabled="!editMemoContent.trim()">저장</button>
            <button @click="cancelEditMemo" class="cancel-btn">취소</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 빈 상태 -->
    <div v-else-if="!isAddingMemo" class="memo-empty">
      <p>이번 달의 꿈에 대한 소감을 작성해보세요</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const props = defineProps({
  memos: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["add-memo", "edit-memo", "delete-memo"]);

// 색상 클래스 배열
const memoColorClasses = ["memo-purple", "memo-pink", "memo-pink", "memo-purple"];

// 상태
const isAddingMemo = ref(false);
const newMemoContent = ref("");
const editingMemoId = ref(null);
const editMemoContent = ref("");
const expandedMemos = ref(new Set());

// 새 메모 추가 시작
function startAddMemo() {
  isAddingMemo.value = true;
  newMemoContent.value = "";
}

// 새 메모 저장
function addNewMemo() {
  if (!newMemoContent.value.trim()) return;
  emit("add-memo", newMemoContent.value.trim());
  isAddingMemo.value = false;
  newMemoContent.value = "";
}

// 새 메모 추가 취소
function cancelAddMemo() {
  isAddingMemo.value = false;
  newMemoContent.value = "";
}

// 메모 수정 시작
function startEditMemo(memo) {
  editingMemoId.value = memo.id;
  editMemoContent.value = memo.content;
}

// 메모 수정 저장
function saveEditMemo() {
  if (!editMemoContent.value.trim()) return;
  emit("edit-memo", { id: editingMemoId.value, content: editMemoContent.value.trim() });
  editingMemoId.value = null;
  editMemoContent.value = "";
}

// 메모 수정 취소
function cancelEditMemo() {
  editingMemoId.value = null;
  editMemoContent.value = "";
}

// 메모 삭제
function deleteMemoItem(memoId) {
  if (confirm("이 메모를 삭제하시겠습니까?")) {
    emit("delete-memo", memoId);
  }
}

// 메모 색상 클래스
function getMemoColorClass(memoId) {
  const index = props.memos.findIndex((m) => m.id === memoId);
  return memoColorClasses[index % memoColorClasses.length];
}

// 날짜 포맷
function formatDate(dateString) {
  const date = new Date(dateString);
  const yy = String(date.getFullYear()).slice(-2);
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yy}.${mm}.${dd}`;
}

// 메모 펼치기/접기
function toggleExpand(memoId) {
  if (expandedMemos.value.has(memoId)) {
    expandedMemos.value.delete(memoId);
  } else {
    expandedMemos.value.add(memoId);
  }
  // 반응성 트리거
  expandedMemos.value = new Set(expandedMemos.value);
}

function isExpanded(memoId) {
  return expandedMemos.value.has(memoId);
}

// 내용이 5줄 이상인지 체크 (대략 100자 이상)
function shouldShowExpandBtn(content) {
  return content.length > 100 || content.split("\n").length > 4;
}

// 상태 초기화 (부모에서 호출 가능)
function resetState() {
  isAddingMemo.value = false;
  newMemoContent.value = "";
  editingMemoId.value = null;
  editMemoContent.value = "";
}

defineExpose({ resetState });
</script>

<style scoped>
.section-card {
  background: var(--gradient-bg-light);
  border-radius: 20px;
  padding: 1.5rem;
  border: 1px solid var(--border-purple);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: "Dongle", sans-serif;
  font-size: 1.8rem;
  margin: 0 0 1rem 0;
  color: #4c2b7b;
  line-height: 1.2;
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.title-icon svg {
  width: 20px;
  height: 20px;
  stroke: #8a6aa8;
}

.memo-section {
  padding-bottom: 1rem;
  height: 100%;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header .section-title {
  margin: 0;
}

.add-memo-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--color-purple), var(--color-purple-dark));
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 3px 10px var(--shadow-purple);
}

.add-memo-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px var(--shadow-purple-md);
}

.memo-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
  overflow-y: auto;
  padding-right: 0.5rem;
  scrollbar-width: thin;
  scrollbar-color: var(--color-purple) var(--color-purple-light);
}

.memo-list::-webkit-scrollbar {
  width: 6px;
}

.memo-list::-webkit-scrollbar-track {
  background: var(--color-purple-light);
  border-radius: 3px;
}

.memo-list::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, var(--color-purple), var(--color-pink));
  border-radius: 3px;
}

.memo-list::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, var(--color-purple-dark), var(--color-pink-dark));
}

.memo-note {
  position: relative;
  padding: 1rem;
  border-radius: 4px 4px 20px 4px;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 3px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

.memo-note::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 6px;
  border-radius: 4px 4px 0 0;
}

.memo-note:hover {
  transform: translateY(-3px) rotate(-0.5deg);
  box-shadow: 4px 6px 15px rgba(0, 0, 0, 0.12);
}

.memo-purple {
  background: linear-gradient(180deg, #f3eef7 0%, #ebe4f0 100%);
}
.memo-purple::before {
  background: var(--color-purple);
}

.memo-pink {
  background: linear-gradient(180deg, #fff0f5 0%, #ffe8ef 100%);
}
.memo-pink::before {
  background: var(--color-pink);
}

.new-memo {
  background: white;
  border: 2px dashed var(--border-dashed-purple);
  grid-column: 1 / -1;
  min-height: auto;
  margin-bottom: 16px;
}

.new-memo::before {
  display: none;
}

.memo-note-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.memo-note-content p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: #444;
  white-space: pre-wrap;
  word-break: break-word;
}

.memo-note-content p.memo-text-clamped {
  display: -webkit-box;
  -webkit-line-clamp: 5;
  line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.expand-btn {
  display: inline-block;
  margin-top: 0.5rem;
  padding: 0.25rem 0.6rem;
  border: none;
  border-radius: 6px;
  background: var(--color-purple-20);
  color: #8a6aa8;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.expand-btn:hover {
  background: var(--color-purple-40);
  color: #6a4a88;
}

.memo-note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.memo-date {
  font-size: 0.75rem;
  color: #999;
}

.memo-note-btns {
  display: flex;
  gap: 0.25rem;
}

.memo-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.2s;
}

.memo-action-btn.edit {
  color: #8a6aa8;
}

.memo-action-btn.edit:hover {
  background: var(--color-purple);
  color: white;
}

.memo-action-btn.delete {
  color: #c97a8d;
}

.memo-action-btn.delete:hover {
  background: var(--color-pink);
  color: white;
}

.memo-note-edit {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.memo-textarea {
  width: 100%;
  min-height: 121px;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: inherit;
  resize: none;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.2s;
  scrollbar-width: thin;
  scrollbar-color: var(--color-purple) var(--color-purple-light);
}

.memo-textarea::-webkit-scrollbar {
  width: 8px;
}

.memo-textarea::-webkit-scrollbar-track {
  background: var(--color-purple-light);
  border-radius: 4px;
}

.memo-textarea::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, var(--color-purple), var(--color-pink));
  border-radius: 4px;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.memo-textarea::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, var(--color-purple-dark), var(--color-pink-dark));
  background-clip: padding-box;
}

.memo-textarea:focus {
  outline: none;
  background: white;
  box-shadow: 0 0 0 2px var(--shadow-purple);
}

.memo-note-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  margin-top: 16px;
}

.memo-note-actions .save-btn,
.memo-note-actions .cancel-btn {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
}

.memo-empty {
  text-align: center;
  padding-top: 2rem;
  color: #aaa;
  font-style: italic;
}

.save-btn,
.cancel-btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.save-btn {
  background: linear-gradient(135deg, var(--color-purple), var(--color-purple-dark));
  color: white;
  box-shadow: 0 4px 12px var(--shadow-purple-md);
}

.save-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--shadow-purple-lg);
}

.cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn:hover {
  background: #eee;
}

@media (max-width: 768px) {
  .section-card {
    padding: 1.25rem;
  }

  .section-title {
    font-size: 1.5rem;
  }
}
</style>

