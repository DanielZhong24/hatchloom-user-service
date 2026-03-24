import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService, { SignUpRequest, SupportedSignupRole } from '../services/authService';

const SignUp: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'STUDENT' as SupportedSignupRole,
    schoolId: '',
    age: '',
  });

  const requiresSchoolId = formData.role === 'STUDENT' || formData.role === 'SCHOOL_TEACHER';
  const requiresAge = formData.role === 'STUDENT';
  const schoolIdUuidRegex =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setError(null);
    setSuccess(null);
  };

  const validateForm = (): boolean => {
    if (!formData.username || !formData.email || !formData.password || !formData.confirmPassword) {
      setError('All base fields are required');
      return false;
    }
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return false;
    }
    if (formData.password.length < 5) {
      setError('Password must be at least 5 characters long');
      return false;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      setError('Please enter a valid email address');
      return false;
    }
    if (requiresSchoolId && !formData.schoolId) {
      setError('schoolId is required for this role');
      return false;
    }

    if (requiresSchoolId && !schoolIdUuidRegex.test(formData.schoolId.trim())) {
      setError('schoolId must be a valid UUID');
      return false;
    }

    if (requiresAge) {
      const parsedAge = Number(formData.age);
      if (!formData.age || Number.isNaN(parsedAge) || parsedAge <= 0) {
        setError('Age must be a positive number for students');
        return false;
      }
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (loading) return;
    if (!validateForm()) return;

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      const signUpData: SignUpRequest = {
        username: formData.username,
        email: formData.email,
        password: formData.password,
        role: formData.role,
      };

      if (requiresSchoolId) signUpData.schoolId = formData.schoolId.trim();
      if (requiresAge) signUpData.age = Number(formData.age);

      const registerResponse = await authService.signup(signUpData);
      if (!registerResponse.userId) {
        setError(registerResponse.message || 'Registration failed.');
        return;
      }

      const loginResponse = await authService.login({
        username: formData.username,
        password: formData.password,
      });

      if (!loginResponse.accessToken || !loginResponse.refreshToken) {
        setSuccess('Registration successful. Please log in.');
        navigate('/login');
        return;
      }

      localStorage.setItem('access_token', loginResponse.accessToken);
      localStorage.setItem('refresh_token', loginResponse.refreshToken);

      const session = await authService.validateSession();
      if (!session.valid || !session.userId) {
        setSuccess('Registration successful. Please log in.');
        navigate('/login');
        return;
      }

      localStorage.setItem(
        'user',
        JSON.stringify({
          userId: session.userId,
          username: formData.username,
          role: session.role || formData.role,
        })
      );

      navigate(`/profile/${session.userId}`);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      } else if (typeof err === 'object' && err !== null && 'response' in err) {
        const axiosError = err as any;
        setError(axiosError.response?.data?.message || 'Sign up failed. Please try again.');
      } else {
        setError('Sign up failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white px-4 py-8">
      <div className="mx-auto w-full max-w-md rounded-2xl bg-white p-8 shadow-xl">
        <div className="mb-8 text-center">
          <h1 className="text-3xl font-bold text-slate-900">Create Account</h1>
          <p className="mt-2 text-sm text-slate-500">Join our learning community</p>
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm font-medium text-red-600">{error}</div>}
        {success && <div className="mb-4 rounded-lg border border-emerald-300 bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="username" className="mb-1 block text-sm font-semibold text-slate-700">Username</label>
            <input type="text" id="username" name="username" value={formData.username} onChange={handleChange} placeholder="Enter your username" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
          </div>

          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-semibold text-slate-700">Email</label>
            <input type="email" id="email" name="email" value={formData.email} onChange={handleChange} placeholder="Enter your email" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
          </div>

          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-semibold text-slate-700">Password</label>
            <input type="password" id="password" name="password" value={formData.password} onChange={handleChange} placeholder="Enter your password (min 5 characters)" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
          </div>

          <div>
            <label htmlFor="confirmPassword" className="mb-1 block text-sm font-semibold text-slate-700">Confirm Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} placeholder="Confirm your password" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
          </div>

          <div>
            <label htmlFor="role" className="mb-1 block text-sm font-semibold text-slate-700">Role</label>
            <select id="role" name="role" value={formData.role} onChange={handleChange} disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100">
              <option value="STUDENT">Student</option>
              <option value="SCHOOL_TEACHER">Teacher</option>
              <option value="PARENT">Parent</option>
            </select>
          </div>

          {requiresSchoolId && (
            <div>
              <label htmlFor="schoolId" className="mb-1 block text-sm font-semibold text-slate-700">School ID</label>
              <input type="text" id="schoolId" name="schoolId" value={formData.schoolId} onChange={handleChange} placeholder="550e8400-e29b-41d4-a716-446655440000" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
            </div>
          )}

          {requiresAge && (
            <div>
              <label htmlFor="age" className="mb-1 block text-sm font-semibold text-slate-700">Age</label>
              <input type="number" id="age" name="age" min={1} value={formData.age} onChange={handleChange} placeholder="16" disabled={loading} className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none transition focus:border-pink-500 focus:ring-2 focus:ring-pink-100 disabled:cursor-not-allowed disabled:bg-slate-100" />
            </div>
          )}

          <button type="submit" className="w-full rounded-lg bg-pink-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-700 disabled:cursor-not-allowed disabled:opacity-60" disabled={loading}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div className="mt-6 border-t border-slate-200 pt-4 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-pink-600 hover:underline">
            Log in here
          </Link>
        </div>
      </div>
    </div>
  );
};

export default SignUp;
