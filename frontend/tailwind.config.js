/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          pink: '#FF1F5A',
          teal: '#0891B2',
          orange: '#FF8C00',
          charcoal: '#1E1E2E',
        },
      },
    },
  },
  plugins: [],
};

