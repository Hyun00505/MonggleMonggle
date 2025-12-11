import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      component: () => import("../layouts/MainLayout.vue"),
      children: [
        { path: "", redirect: { name: "auth" } },
        {
          path: "auth",
          name: "auth",
          component: () => import("../views/AuthView.vue"),
          meta: { hideHeader: true, requiresGuest: true },
        },
        {
          path: "calendar",
          name: "calendar",
          component: () => import("../views/CalendarView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "write",
          name: "write",
          component: () => import("../views/WriteView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "mypage",
          name: "mypage",
          component: () => import("../views/MyPageView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "loading",
          name: "loading",
          component: () => import("../views/LoadingView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "analysis",
          name: "analysis",
          component: () => import("../views/DreamAnalysisView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "monthly-analysis",
          name: "monthly-analysis",
          component: () => import("../views/MonthlyAnalysisView.vue"),
          meta: { requiresAuth: true },
        },
        {
          path: "gallery",
          name: "gallery",
          component: () => import("../views/ImageGalleryView.vue"),
          meta: { requiresAuth: true },
        },
        { path: ":pathMatch(.*)*", redirect: { name: "auth" } },
      ],
    },
  ],
  scrollBehavior: () => ({ top: 0 }),
});

// 인증 가드
router.beforeEach((to, from, next) => {
  const isLoggedIn = !!localStorage.getItem("accessToken");

  // 인증이 필요한 페이지
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: "auth" });
    return;
  }

  // 이미 로그인한 사용자가 auth 페이지 접근 시
  if (to.meta.requiresGuest && isLoggedIn) {
    next({ name: "calendar" });
    return;
  }

  next();
});

export default router;
