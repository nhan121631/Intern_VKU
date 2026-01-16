import Axios, { type InternalAxiosRequestConfig } from 'axios';
import { API_URL } from '../service/Constant';

const apiClient = Axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStorage = localStorage.getItem('auth-storage') ? JSON.parse(localStorage.getItem('auth-storage')!) : null;

    const access_token = authStorage?.state?.access_token;

    if (config.headers === undefined) {
      config.headers = new Axios.AxiosHeaders();
    }

    if (access_token) {
      config.headers.Authorization = `Bearer ${access_token}`;
    }

    config.headers.Accept = 'application/json';

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  async (error) => {
    const originalRequest = error.config;

    // Extract error data from response
    const errorData = error.response?.data;
    
    if (originalRequest.url === '/auth/login') {
      // Pass parsed error data for login errors
      return Promise.reject(errorData || error);
    }
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.error('Authentication failed, redirecting to login');
      localStorage.removeItem('auth-storage');
      window.location.href = '/login';
      return Promise.reject(errorData || error);
    }

    return Promise.reject(errorData || error);
  }
);

export default apiClient;