import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      component: () => import("../layouts/MainLayout.vue"),
      children: [
        {
          path: "",
          redirect: { name: "landing" },
        },
        {
          path: "main",
          name: "landing",
          component: () => import("../views/LandingView.vue"),
          meta: { hideHeader: true },
        },
        {
          path: "auth",
          redirect: { name: "landing" },
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
        {
          path: "ranking",
          name: "ranking",
          component: () => import("../views/RankingView.vue"),
          meta: { requiresAuth: true },
        },
        { path: ":pathMatch(.*)*", redirect: "/main" },
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
    next({ name: "landing" });
    return;
  }

  // landing 페이지: 로그인 상태에 따라 헤더 표시 방식 변경
  if (to.name === "landing") {
    if (isLoggedIn) {
      to.meta.hideHeader = false;
      to.meta.minimalHeader = true;
    } else {
      to.meta.hideHeader = true;
      to.meta.minimalHeader = false;
    }
  }

  next();
});

export default router;
