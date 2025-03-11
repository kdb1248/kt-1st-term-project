// src/plugins/i18n.js (예시)
import { createI18n } from 'vue-i18n';

const messages = {
    kr: {
      message: {
        won: '원',
        addToCart: '담기',
        logout: '로그아웃',
        cartTitle: '장바구니',
        totalAmount: '총 금액',
        menuManagement: '메뉴 관리',
        orderHistory: '주문내역',
        goShopping: '메뉴 보러가기',
        confirmLogout: '정말 로그아웃하시겠습니까?',
        noCartItems: '장바구니가 비어있습니다',
        toastTitle: '알림',
        no: '아니오',
        languageSelection: '언어 선택', 
        close: '닫기',
        totalQuantity: '총 수량',
        totalAmount: '총 금액',
        quantity: '개',
        sum:'합계',
        order:'주문하기',
        recentOrder:'최근 주문',
        orderCode:'주문번호',
        noOrderHistory:'주문 내역이 없습니다.',
        orderSuccess:'주문이 성공적으로 완료되었습니다.',
        // ...
      }
    },
    en: {
      message: {
        won: 'won',
        addToCart: 'Add to Cart',
        logout: 'Logout',
        cartTitle: 'Cart',
        totalAmount: 'Total Amount',
        menuManagement: 'Menu Management',
        orderHistory: 'Order History',
        goShopping: 'Go to Menu',
        confirmLogout: 'Are you sure you want to log out?',
        noCartItems: 'Your cart is empty',
        toastTitle: 'Notification',
        no: 'No',
        languageSelection: 'Language Selection',
        close: 'Close',
        totalQuantity: 'Total Quantity',
        quantity: 'items',
        order:'Order',
        recentOrder:'Recent Order',
        orderCode:'Order Code',
        noOrderHistory:'No order history',
        sum:'Sum',
        orderSuccess:'Order successfully completed',
        // ...
      }
    },
    zh: {
      message: {
        won: '韩元',
        addToCart: '加入购物车',
        logout: '登出',
        cartTitle: '购物车',
        totalAmount: '总金额',
        menuManagement: '菜单管理',
        orderHistory: '订单历史',
        goShopping: '去逛菜单',
        confirmLogout: '您确定要退出登录吗？',
        noCartItems: '购物车是空的',
        toastTitle: '提示', 
        no: '否',
        languageSelection: '语言选择',
        close: '关闭',
        totalQuantity: '总数量',
        quantity: '个',
        sum:'合计',
        order:'下单',
        recentOrder:'最近订单',
        orderCode:'订单编号',
        noOrderHistory:'没有订单历史',
        orderSuccess:'订单成功完成',
        // ...
      }
    },
    jp: {
      message: {
        won: 'ウォン',
        addToCart: 'カートに追加',
        logout: 'ログアウト',
        cartTitle: 'カート',
        totalAmount: '合計金額',
        menuManagement: 'メニュー管理',
        orderHistory: '注文履歴',
        goShopping: 'メニューを見る',
        confirmLogout: '本当にログアウトしますか？',
        noCartItems: 'カートが空です',
        toastTitle: '通知',
        no: '否',
        languageSelection: '言語選択',
        close: '閉じる',
        totalQuantity: '総数量',
        quantity: '個',
        sum:'合計',
        order:'注文する',
        recentOrder:'最近の注文',
        orderCode:'注文番号',
        noOrderHistory:'注文履歴がありません',
        orderSuccess:'注文が成功しました',
        // ...
      }
    },
  };
 

const i18n = createI18n({
  locale: 'kr', // 기본 언어
  fallbackLocale: 'kr',
  messages,
});

export default i18n;
