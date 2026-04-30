/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        /** 与 CSS 变量、前台深色主题统一 */
        dark: "#0b1220",
        light: "#f1f5f9",
        primary: {
          DEFAULT: "#6366f1",
          dark: "#4f46e5",
          light: "#818cf8",
        },
        secondary: "#8b5cf6",
        accent: "#ec4899",
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', '"Inter"', "system-ui", "-apple-system", "sans-serif"],
      },
      boxShadow: {
        glow: "0 0 40px -10px rgba(99, 102, 241, 0.35)",
        "glow-sm": "0 0 24px -8px rgba(99, 102, 241, 0.25)",
        card: "0 4px 24px -4px rgba(15, 23, 42, 0.45), 0 0 0 1px rgba(148, 163, 184, 0.06)",
      },
      backgroundImage: {
        "mesh":
          "radial-gradient(ellipse 80% 50% at 50% -20%, rgba(99, 102, 241, 0.22), transparent), radial-gradient(ellipse 60% 40% at 100% 50%, rgba(236, 72, 153, 0.12), transparent), radial-gradient(ellipse 50% 30% at 0% 80%, rgba(139, 92, 246, 0.15), transparent)",
      },
      transitionDuration: {
        250: "250ms",
      },
    },
  },
  plugins: [],
}
