import '@fontsource/roboto/300.css'
import '@fontsource/roboto/400.css'
import '@fontsource/roboto/500.css'
import '@fontsource/roboto/700.css'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './view/App.tsx'
import { KeycloakProvider } from './provider/KeycloakProvider.tsx'
import { ServicesProvider } from './provider/ServicesProvider.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <KeycloakProvider>
      <ServicesProvider>
        <App></App>
      </ServicesProvider>
    </KeycloakProvider>
  </StrictMode>,
)
