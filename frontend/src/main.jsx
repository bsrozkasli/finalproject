import React from 'react'
import ReactDOM from 'react-dom/client'
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <Auth0Provider
            domain={import.meta.env.VITE_AUTH0_DOMAIN || "your-auth0-domain"}
            clientId={import.meta.env.VITE_AUTH0_CLIENT_ID || "your-auth0-client-id"}
            authorizationParams={{
                redirect_uri: window.location.origin,
                audience: "https://api.airline.com"
            }}
            cacheLocation="localstorage"
            onRedirectCallback={(appState) => {
                // Handle redirect after login if needed
                if (appState?.returnTo) {
                    window.location.href = appState.returnTo;
                }
            }}
        >
            <App />
        </Auth0Provider>
    </React.StrictMode>,
)
