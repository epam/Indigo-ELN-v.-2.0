import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig({
  server: {
    proxy: {
      "/api": {
        target: "http://localhost",
        secure: false,
        // target: "https://indigo-eln-dev.test.lifescience.opensource.epam.com",
        // secure: true
      },
    },
  },
  plugins: [react()],
})
