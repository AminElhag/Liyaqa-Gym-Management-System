import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = {
  en: {
    translation: {
      // Common
      common: {
        loading: 'Loading...',
        error: 'Error',
        success: 'Success',
        save: 'Save',
        cancel: 'Cancel',
        delete: 'Delete',
        edit: 'Edit',
        add: 'Add',
        search: 'Search',
        filter: 'Filter',
        actions: 'Actions',
      },
      // Auth
      auth: {
        login: 'Login',
        logout: 'Logout',
        email: 'Email',
        password: 'Password',
        forgotPassword: 'Forgot Password?',
        signIn: 'Sign In',
        signUp: 'Sign Up',
        welcomeBack: 'Welcome Back',
      },
      // Dashboard
      dashboard: {
        title: 'Dashboard',
        overview: 'Overview',
        analytics: 'Analytics',
        reports: 'Reports',
      },
      // Members
      members: {
        title: 'Members',
        addMember: 'Add Member',
        memberList: 'Member List',
        memberDetails: 'Member Details',
      },
      // Subscriptions
      subscriptions: {
        title: 'Subscriptions',
        active: 'Active',
        expired: 'Expired',
        pending: 'Pending',
      },
      // Classes
      classes: {
        title: 'Classes',
        schedule: 'Schedule',
        book: 'Book',
        upcoming: 'Upcoming Classes',
      },
    },
  },
  ar: {
    translation: {
      // Common
      common: {
        loading: 'جاري التحميل...',
        error: 'خطأ',
        success: 'نجاح',
        save: 'حفظ',
        cancel: 'إلغاء',
        delete: 'حذف',
        edit: 'تعديل',
        add: 'إضافة',
        search: 'بحث',
        filter: 'تصفية',
        actions: 'إجراءات',
      },
      // Auth
      auth: {
        login: 'تسجيل الدخول',
        logout: 'تسجيل الخروج',
        email: 'البريد الإلكتروني',
        password: 'كلمة المرور',
        forgotPassword: 'نسيت كلمة المرور؟',
        signIn: 'تسجيل الدخول',
        signUp: 'تسجيل حساب جديد',
        welcomeBack: 'مرحباً بعودتك',
      },
      // Dashboard
      dashboard: {
        title: 'لوحة التحكم',
        overview: 'نظرة عامة',
        analytics: 'التحليلات',
        reports: 'التقارير',
      },
      // Members
      members: {
        title: 'الأعضاء',
        addMember: 'إضافة عضو',
        memberList: 'قائمة الأعضاء',
        memberDetails: 'تفاصيل العضو',
      },
      // Subscriptions
      subscriptions: {
        title: 'الاشتراكات',
        active: 'نشط',
        expired: 'منتهي',
        pending: 'قيد الانتظار',
      },
      // Classes
      classes: {
        title: 'الحصص',
        schedule: 'الجدول',
        book: 'حجز',
        upcoming: 'الحصص القادمة',
      },
    },
  },
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'en',
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
