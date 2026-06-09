/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        display: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
        body: ['"Plus Jakarta Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        crystal: {
          50: '#f0f4ff',
          100: '#dbe4ff',
          200: '#bac8ff',
          300: '#91a7ff',
          400: '#748ffc',
          500: '#5c7cfa',
          600: '#4c6ef5',
          700: '#4263eb',
          800: '#3b5bdb',
          900: '#364fc7',
        },
        obsidian: {
          50: '#f3f0ff',
          100: '#e5dbff',
          200: '#d0bfff',
          300: '#b197fc',
          400: '#9775fa',
          500: '#845ef7',
          600: '#7950f2',
          700: '#7048e8',
          800: '#6741d9',
          900: '#5f3dc4',
        },
      },
      animation: {
        'float': 'float 6s ease-in-out infinite',
        'glow': 'glow 2s ease-in-out infinite alternate',
        'slide-up': 'slideUp 0.5s ease-out',
        'slide-down': 'slideDown 0.3s ease-out',
        'fade-in': 'fadeIn 0.4s ease-out',
        'scale-in': 'scaleIn 0.3s ease-out',
        'shimmer': 'shimmer 2s linear infinite',
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-20px)' },
        },
        glow: {
          '0%': { boxShadow: '0 0 20px rgba(133, 94, 247, 0.3)' },
          '100%': { boxShadow: '0 0 40px rgba(133, 94, 247, 0.6)' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        slideDown: {
          '0%': { transform: 'translateY(-10px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        scaleIn: {
          '0%': { transform: 'scale(0.95)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        shimmer: {
          '0%': { backgroundPosition: '-200% 0' },
          '100%': { backgroundPosition: '200% 0' },
        },
      },
      backgroundImage: {
        'gradient-radial': 'radial-gradient(var(--tw-gradient-stops))',
        'gradient-conic': 'conic-gradient(from 180deg at 50% 50%, var(--tw-gradient-stops))',
        'noise': "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E\")",
      },
    },
  },
  plugins: [],
}
