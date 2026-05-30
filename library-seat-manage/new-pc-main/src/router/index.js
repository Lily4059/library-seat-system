import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import MyReservations from '../views/MyReservations.vue'
import Profile from '../views/Profile.vue'
import History from '../views/History.vue'
import CreditLog from '../views/CreditLog.vue'
import Notifications from '../views/Notifications.vue'
import Settings from '../views/Settings.vue'
import Favorites from '../views/Favorites.vue'
import HelpFeedback from '../views/HelpFeedback.vue'
import CreditStrategy from '../views/CreditStrategy.vue'
import StudyTime from '../views/StudyTime.vue'
import BindStudent from '../views/BindStudent.vue'
import SeatMap from '../views/SeatMap.vue'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/bind-student', component: BindStudent, meta: { requiresAuth: true } },
  { path: '/home', component: Home, meta: { requiresAuth: true } },
  { path: '/seat-map', component: SeatMap, meta: { requiresAuth: true } },
  { path: '/my-reservations', component: MyReservations, meta: { requiresAuth: true } },
  { path: '/profile', component: Profile, meta: { requiresAuth: true } },
  { path: '/settings', component: Settings, meta: { requiresAuth: true } },
  { path: '/history', component: History, meta: { requiresAuth: true } },
  { path: '/credit-log', component: CreditLog, meta: { requiresAuth: true } },
  { path: '/credit-strategy', component: CreditStrategy, meta: { requiresAuth: true } },
  { path: '/study-time', component: StudyTime, meta: { requiresAuth: true } },
  { path: '/notifications', component: Notifications, meta: { requiresAuth: true } },
  { path: '/favorites', component: Favorites, meta: { requiresAuth: true } },
  { path: '/help', component: HelpFeedback, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router
