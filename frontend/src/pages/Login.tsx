import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService, { LoginRequest } from '../services/authService';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError(null);
  };

  const validateForm = (): boolean => {
    if (!formData.username || !formData.password) {
      setError('Username and password are required');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setLoading(true);
    setError(null);

    try {
      const loginData: LoginRequest = {
        username: formData.username,
        password: formData.password,
      };

      const response = await authService.login(loginData);
      if (!response.accessToken || !response.refreshToken) {
        setError(
          response.message ||
            'Login failed. Please check your credentials and try again.'
        );
        return;
      }

      localStorage.setItem('access_token', response.accessToken);
      localStorage.setItem('refresh_token', response.refreshToken);

      const session = await authService.validateSession();
      if (!session.valid || !session.userId) {
        setError('Login failed. Unable to resolve user session.');
        return;
      }

      localStorage.setItem(
        'user',
        JSON.stringify({
          userId: session.userId,
          username: response.username || formData.username,
          role: session.role || response.role || '',
        })
      );

      navigate(`/profile/${session.userId}`);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else if (typeof err === 'object' && err !== null && 'response' in err) {
        const axiosError = err as any;
        setError(
          axiosError.response?.data?.message ||
            'Login failed. Please check your credentials and try again.'
        );
      } else {
        setError('Login failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white px-4 py-8">
      <div className="mx-auto w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-slate-900">Welcome Back</h1>
          <p className="mt-2 text-sm text-slate-500">Log in to your account</p>
        </div>

        {error && (
          <div className="mb-4 rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm font-medium text-red-600">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="username"
              className="mb-1 block text-sm font-semibold text-slate-700"
            >
              Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              disabled={loading}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100"
            />
          </div>

          <div>
            <label
              htmlFor="password"
              className="mb-1 block text-sm font-semibold text-slate-700"
            >
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              disabled={loading}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-pink-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? 'Logging in...' : 'Log In'}
          </button>
        </form>

        <div className="mt-6 border-t border-slate-200 pt-4 text-center text-sm text-slate-500">
          Don't have an account?{' '}
          <Link
            to="/signup"
            className="font-semibold text-pink-600 hover:underline"
          >
            Sign up here
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
