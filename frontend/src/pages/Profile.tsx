import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import authService, { UserProfile } from '../services/authService';

const Profile: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      if (!userId) {
        setError('User ID is missing');
        setLoading(false);
        return;
      }

      try {
        const userData = await authService.getProfile(userId);
        setProfile(userData);
      } catch (err) {
        if (err instanceof Error) {
          setError(err.message);
        } else if (typeof err === 'object' && err !== null && 'response' in err) {
          const axiosError = err as any;
          if (axiosError.response?.status === 403) {
            setError('You do not have permission to view this profile');
          } else {
            setError(axiosError.response?.data?.message || 'Failed to load profile');
          }
        } else {
          setError('Failed to load profile');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [userId]);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white p-4">
        <div className="mx-auto max-w-3xl rounded-2xl bg-white p-8 shadow-xl">
          <div className="text-center text-sm text-slate-600">Loading profile...</div>
        </div>
      </div>
    );
  }

  if (error || !profile) {
    return (
      <div className="min-h-screen bg-white p-4">
        <div className="mx-auto max-w-3xl rounded-2xl bg-white p-8 shadow-xl">
          <div className="mb-6 rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm font-medium text-red-600">
            {error || 'Profile not found'}
          </div>
          <button
            className="w-full rounded-lg border border-slate-300 bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-200"
            onClick={handleLogout}
          >
            Back to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white p-4">
      <div className="mx-auto max-w-3xl overflow-hidden rounded-2xl bg-white shadow-xl">
        <div className="flex flex-col gap-4 bg-gradient-to-br from-indigo-500 to-purple-600 p-6 text-white sm:flex-row sm:items-center">
          <div className="h-20 w-20 shrink-0 overflow-hidden rounded-xl border-2 border-white/80 bg-white/20">
            {profile.profilePictureUrl ? (
              <img src={profile.profilePictureUrl} alt={profile.username} className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-2xl font-bold">
                {profile.username.charAt(0).toUpperCase()}
              </div>
            )}
          </div>

          <div>
            <h1 className="text-2xl font-bold">{profile.username}</h1>
            <p className="mt-1 inline-block rounded-full bg-white/20 px-3 py-1 text-xs font-semibold">
              {profile.role}
            </p>
            <p className="mt-2 text-sm text-white/90">{profile.email}</p>
          </div>
        </div>

        <div className="space-y-6 p-6">
          {profile.bio && (
            <section>
              <h3 className="mb-1 text-sm font-semibold text-slate-800">Bio</h3>
              <p className="text-sm text-slate-600">{profile.bio}</p>
            </section>
          )}

          {profile.description && (
            <section>
              <h3 className="mb-1 text-sm font-semibold text-slate-800">Description</h3>
              <p className="text-sm text-slate-600">{profile.description}</p>
            </section>
          )}

          {profile.role === 'STUDENT' && (
            <section className="rounded-xl bg-slate-50 p-4">
              <h3 className="mb-3 text-sm font-semibold text-slate-800">Academic Profile</h3>
              <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
                {profile.gradeLevel && (
                  <MetricCard label="Grade Level" value={profile.gradeLevel} />
                )}
                {profile.specialization && (
                  <MetricCard label="Specialization" value={profile.specialization} />
                )}
                {profile.skillsCertified !== null && profile.skillsCertified !== undefined && (
                  <MetricCard label="Skills Certified" value={profile.skillsCertified} />
                )}
                {profile.explorerLevelXp !== null && profile.explorerLevelXp !== undefined && (
                  <MetricCard label="Explorer XP" value={profile.explorerLevelXp} />
                )}
                {profile.currentStreak !== null && profile.currentStreak !== undefined && (
                  <MetricCard label="Current Streak" value={profile.currentStreak} />
                )}
                {profile.activeVentures !== null && profile.activeVentures !== undefined && (
                  <MetricCard label="Active Ventures" value={profile.activeVentures} />
                )}
                {profile.problemsTackled !== null && profile.problemsTackled !== undefined && (
                  <MetricCard label="Problems Tackled" value={profile.problemsTackled} />
                )}
                {profile.lastActive && (
                  <MetricCard label="Last Active" value={new Date(profile.lastActive).toLocaleDateString()} />
                )}
              </div>
            </section>
          )}

          <section className="space-y-1 border-t border-slate-200 pt-4 text-sm text-slate-600">
            <p>
              <span className="font-semibold text-slate-800">Account Created:</span>{' '}
              {new Date(profile.createdAt).toLocaleDateString()}
            </p>
            <p>
              <span className="font-semibold text-slate-800">Last Updated:</span>{' '}
              {new Date(profile.updatedAt).toLocaleDateString()}
            </p>
          </section>

          <button
            className="w-full rounded-lg border border-slate-300 bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-200"
            onClick={handleLogout}
          >
            Log Out
          </button>
        </div>
      </div>
    </div>
  );
};

const MetricCard: React.FC<{ label: string; value: string | number }> = ({ label, value }) => (
  <div className="rounded-lg border border-slate-200 bg-white p-3 text-center">
    <p className="text-[11px] font-semibold uppercase tracking-wide text-slate-500">{label}</p>
    <p className="mt-1 text-lg font-bold text-pink-600">{value}</p>
  </div>
);

export default Profile;

