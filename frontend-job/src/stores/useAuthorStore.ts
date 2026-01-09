/* eslint-disable @typescript-eslint/no-explicit-any */
import type { NavigateFunction } from 'react-router';
import { create } from 'zustand';
import { devtools, persist } from 'zustand/middleware';
import apiClient from '../lib/apt-client-sp';


export interface LoggedInUser {
  id: string | number;
  fullName: string;
  isActive: boolean;
  roles: [
    {
      id: string | number;
      name: string;
    }
  ];
}

export interface AuthState {
  access_token?: string;
  refresh_token?: string;
  loggedInUser?: LoggedInUser;  
  loading: boolean;
  error: any;
  login: ({ username, password, navigate }: { username: string; password: string; navigate: NavigateFunction }) => Promise<void>;
  loginWithGoogle: ({ credential, navigate }: { credential: string; navigate: NavigateFunction }) => Promise<void>;
  logOut: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      (set) => {
        return {
          access_token: undefined,
          refresh_token: undefined,
          loggedInUser: undefined,
          loading: false,
          error: null,
          login: async ({ username, password, navigate }) => {
            try {
              set(
                {
                  loggedInUser: undefined,
                  access_token: undefined,
                  refresh_token: undefined,
                  error: null,
                  loading: true,
                },
                false,
                { type: '@AUTH/LOGIN/LOADING' }
              );

              const response: any = (await apiClient.post('/auth/login', { username, password })) as any;

              set(
                {
                  access_token: response.access_token,
                  refresh_token: response.refresh_token,
                  loggedInUser: response.loggedInUser,
                  loading: false,
                  error: null,
                },
                false,
                { type: '@AUTH/LOGIN/SUCCESS' }
              );
              navigate('/home');
            } catch (error) {
              set({ error, access_token: undefined, refresh_token: undefined, loggedInUser: undefined, loading: false }, false, {
                type: '@AUTH/LOGIN/ERROR',
              });
            }
          },

          loginWithGoogle: async ({ credential, navigate }) => {
            try {
              set(
                {
                  loggedInUser: undefined,
                  access_token: undefined,
                  refresh_token: undefined,
                  error: null,
                  loading: true,
                },
                false,
                { type: '@AUTH/GOOGLE_LOGIN/LOADING' }
              );

              const response: any = (await apiClient.post('/auth/google-login', { credential })) as any;

              set(
                {
                  access_token: response.access_token,
                  refresh_token: response.refresh_token,
                  loggedInUser: response.loggedInUser,
                  loading: false,
                  error: null,
                },
                false,
                { type: '@AUTH/GOOGLE_LOGIN/SUCCESS' }
              );
              navigate('/home');
            } catch (error) {
              set({ error, access_token: undefined, refresh_token: undefined, loggedInUser: undefined, loading: false }, false, {
                type: '@AUTH/GOOGLE_LOGIN/ERROR',
              });
            }
          },

          logOut: async () => {
            set({ access_token: undefined, refresh_token: undefined, loggedInUser: undefined });
          },
        };
      },
      {
        name: 'auth-storage',
      }
    )
  )
);