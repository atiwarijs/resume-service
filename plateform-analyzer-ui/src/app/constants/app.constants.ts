export const APP_CONSTANTS = {
  // API Endpoints
  API: {
    BASE_URL: 'http://localhost:8086/api/v1/security',
    AUTH: {
      LOGIN: '/auth/login',
      REGISTER: '/auth/register-user',
      REFRESH_TOKEN: '/auth/refresh-token',
      LOGOUT: '/auth/logout',
      UPDATE_USER: '/auth/update-user',
      UPDATE_PASSWORD: '/auth/update-password'
    },
    USER: {
      USER_INFO: '/user/userinfo/{username}/{email}',
      EMAIL_CHECK: '/user/email-check/{email}'
    }
  },

  // Storage Keys
  STORAGE: {
    ACCESS_TOKEN: 'access_token',
    REFRESH_TOKEN: 'refresh_token',
    USER_DATA: 'user_data',
    IS_LOGGED_IN: 'is_logged_in'
  },

  // SSO Configuration
  SSO: {
    GOOGLE: {
      CLIENT_ID: 'YOUR_GOOGLE_CLIENT_ID',
      REDIRECT_URI: window.location.origin + '/auth/google/callback'
    },
    FACEBOOK: {
      APP_ID: 'YOUR_FACEBOOK_APP_ID',
      REDIRECT_URI: window.location.origin + '/auth/facebook/callback'
    }
  },

  // Routes
  ROUTES: {
    LOGIN: '/login',
    DASHBOARD: '/dashboard',
    PROFILE: '/profile',
    HOME: '/'
  },

  // Token Expiration
  TOKEN: {
    ACCESS_TOKEN_EXPIRY: 15 * 60 * 1000, // 15 minutes
    REFRESH_TOKEN_EXPIRY: 7 * 24 * 60 * 60 * 1000 // 7 days
  }
};
