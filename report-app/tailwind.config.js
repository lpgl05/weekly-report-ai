import { Config } from 'tailwindcss'

export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#5570F1',
        'primary-light': '#6B7FFF',
        'primary-dark': '#4A5FE7',
        background: '#F4F5FA',
        'gray-text': '#5E6366',
      },
      fontFamily: {
        'inter': ['Inter', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'SimHei', 'Arial', 'Helvetica', 'sans-serif'],
      },
      fontSize: {
        '80': '80px',
        '40': '40px',
        '36': '36px',
      },
      lineHeight: {
        '97': '97px',
        '60': '60px',
      },
      dropShadow: {
        'custom': '12px 20px 60px rgba(48, 55, 89, 0.16)',
        'custom-2': '-80px 60px 200px rgba(48, 55, 89, 0.16)',
      }
    },
  },
  plugins: [],
} satisfies Config