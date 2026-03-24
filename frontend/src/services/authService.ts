import apiClient from './apiClient';

export type SupportedSignupRole = 'STUDENT' | 'SCHOOL_TEACHER' | 'PARENT';

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
  role: SupportedSignupRole;
  schoolId?: string;
  age?: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterResponse {
  userId?: string;
  username?: string;
  role?: string;
  message: string;
}

export interface LoginResponse {
  accessToken?: string;
  refreshToken?: string;
  role?: string;
  username?: string;
  message: string;
}

export interface SessionValidationResponse {
  valid: boolean;
  userId?: string;
  role?: string;
  message?: string;
}

export interface UserProfile {
  userId: string;
  username: string;
  email: string;
  role: string;
  bio?: string;
  description?: string;
  profilePictureUrl?: string;
  gradeLevel?: string;
  specialization?: string;
  lastActive?: string;
  skillsCertified?: number;
  explorerLevelXp?: number;
  currentStreak?: number;
  activeVentures?: number;
  problemsTackled?: number;
  createdAt: string;
  updatedAt: string;
}

const authService = {
  signup: async (data: SignUpRequest): Promise<RegisterResponse> => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/login', data);
    return response.data;
  },

  validateSession: async (): Promise<SessionValidationResponse> => {
    const response = await apiClient.get('/auth/validate');
    return response.data;
  },

  getProfile: async (userId: string): Promise<UserProfile> => {
    const response = await apiClient.get(`/profile/${userId}`);
    return response.data;
  },

  logout: (): void => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('access_token');
  },

  getStoredUser: (): { userId: string; username: string; role: string } | null => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
};

export default authService;
