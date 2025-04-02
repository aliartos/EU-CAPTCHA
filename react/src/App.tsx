import { useState } from 'react'
import './App.css'
import { EUCaptcha } from './components/EUCaptcha'

function App() {
  const [captchaVerified, setCaptchaVerified] = useState(false);
  const [token, setToken] = useState<string>('');
  const [jwtToken, setJwtToken] = useState<string>('');
  const [apiBaseUrl, setApiBaseUrl] = useState<string>('http://localhost:8080');
  const [captchaLanguage, setCaptchaLanguage] = useState<string>('en-GB');
  const [captchaType, setCaptchaType] = useState<'STANDARD' | 'WHATS_UP' | 'SLIDING'>('STANDARD');
  const [captchaLength, setCaptchaLength] = useState<number>(8);
  const [capitalized, setCapitalized] = useState<boolean>(true);

  const handleVerify = (token: string, jwtToken: string) => {
    setToken(token);
    setJwtToken(jwtToken);
    setCaptchaVerified(true);
    console.log('CAPTCHA verified with token:', token);
    console.log('JWT token for subsequent calls:', jwtToken);
  };

  const handleError = (error: Error) => {
    console.error('CAPTCHA error:', error);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Example of using the JWT token in a subsequent API call
    if (captchaVerified) {
      console.log(`Making authenticated request with JWT: ${jwtToken}`);
      alert(`Form submitted successfully! JWT token: ${jwtToken}`);
    } else {
      alert('Please verify the CAPTCHA first.');
    }
  };

  const handleReset = () => {
    setCaptchaVerified(false);
    setToken('');
    setJwtToken('');
  };

  return (
    <div className="app-container">
      <header className="app-header">
        <h1>EU CAPTCHA Demo</h1>
        <p>A demonstration of the EU CAPTCHA React component</p>
      </header>

      <div className="demo-container">
        <div className="demo-form-container">
          <h2>Sample Form with CAPTCHA</h2>
          
          <form onSubmit={handleSubmit} className="demo-form">
            <div className="form-group">
              <label htmlFor="name">Name</label>
              <input 
                type="text" 
                id="name" 
                placeholder="Enter your name" 
                required 
              />
            </div>
            
            <div className="form-group">
              <label htmlFor="email">Email</label>
              <input 
                type="email" 
                id="email" 
                placeholder="Enter your email" 
                required 
              />
            </div>
            
            <div className="form-group captcha-container">
              <label>CAPTCHA Verification</label>
              <EUCaptcha 
                apiBaseUrl={apiBaseUrl}
                onVerify={handleVerify}
                onError={handleError}
                language={captchaLanguage}
                captchaType={captchaType}
                captchaLength={captchaLength}
                capitalized={capitalized}
                initialJwtToken={jwtToken}
              />
            </div>
            
            <div className="form-actions">
              <button 
                type="submit" 
                className="submit-button"
                disabled={!captchaVerified}
              >
                Submit Form
              </button>
              
              <button 
                type="button" 
                className="reset-button"
                onClick={handleReset}
              >
                Reset
              </button>
            </div>
          </form>
        </div>

        <div className="demo-configuration">
          <h2>Component Configuration</h2>
          
          <div className="config-form">
            <div className="form-group">
              <label htmlFor="apiUrl">API Base URL</label>
              <input 
                type="text" 
                id="apiUrl" 
                value={apiBaseUrl} 
                onChange={(e) => setApiBaseUrl(e.target.value)}
              />
              <small>The base URL of your EU CAPTCHA service</small>
            </div>
            
            <div className="form-group">
              <label htmlFor="language">Language</label>
              <select 
                id="language" 
                value={captchaLanguage} 
                onChange={(e) => setCaptchaLanguage(e.target.value)}
              >
                <option value="en-GB">English (UK)</option>
                <option value="en-US">English (US)</option>
                <option value="fr-FR">French</option>
                <option value="de-DE">German</option>
                <option value="es-ES">Spanish</option>
                <option value="it-IT">Italian</option>
                <option value="nl-NL">Dutch</option>
                <option value="pt-PT">Portuguese</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="captchaType">CAPTCHA Type</label>
              <select 
                id="captchaType" 
                value={captchaType} 
                onChange={(e) => setCaptchaType(e.target.value as 'STANDARD' | 'WHATS_UP' | 'SLIDING')}
              >
                <option value="STANDARD">Standard Text</option>
                <option value="WHATS_UP">Image Rotation</option>
                <option value="SLIDING">Sliding</option>
              </select>
            </div>
            
            <div className="form-group">
              <label htmlFor="captchaLength">CAPTCHA Length</label>
              <input 
                type="number" 
                id="captchaLength" 
                min="4" 
                max="12" 
                value={captchaLength} 
                onChange={(e) => setCaptchaLength(Number(e.target.value))}
              />
              <small>Only applies to STANDARD captcha type</small>
            </div>
            
            <div className="form-group">
              <label htmlFor="capitalized">Capitalized</label>
              <div className="checkbox-container">
                <input 
                  type="checkbox" 
                  id="capitalized" 
                  checked={capitalized} 
                  onChange={(e) => setCapitalized(e.target.checked)} 
                />
                <small>Use capitalized letters (STANDARD type only)</small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="usage-docs">
        <h2>Component Usage</h2>
        
        <div className="code-block">
          <pre>
            <code>{`import { EUCaptcha } from './components/EUCaptcha';

// In your component:
const handleVerify = (token, jwtToken) => {
  console.log('CAPTCHA verified with token:', token);
  console.log('JWT token for subsequent calls:', jwtToken);
};

const handleError = (error) => {
  console.error('CAPTCHA error:', error);
};

return (
  <EUCaptcha 
    apiBaseUrl="http://localhost:8080"
    onVerify={handleVerify}
    onError={handleError}
    language="en-GB"
    captchaType="STANDARD"
    captchaLength={8}
    capitalized={true}
    initialJwtToken={savedJwtToken} // Optional: Pass a previously saved token
  />
);`}</code>
          </pre>
        </div>
        
        <div className="notes">
          <h3>Notes:</h3>
          <ul>
            <li>Replace <code>apiBaseUrl</code> with your actual EU CAPTCHA service URL</li>
            <li>The component handles the CAPTCHA verification process</li>
            <li>Use <code>onVerify(token, jwtToken)</code> callback to get the verification token and JWT token</li>
            <li>The JWT token (xJwtString) is required for subsequent API calls to the EU CAPTCHA service</li>
            <li>Use <code>onError</code> callback to handle any errors</li>
            <li>Available CAPTCHA types: STANDARD, WHATS_UP (image rotation), SLIDING</li>
            <li>Use proper locale format like "en-GB", "fr-FR", etc.</li>
            <li>Set <code>captchaLength</code> for text length (STANDARD type only)</li>
            <li>Use <code>capitalized</code> to control case sensitivity (STANDARD type only)</li>
          </ul>
        </div>
      </div>

      {token && (
        <div className="verification-result">
          <h3>Verification Result</h3>
          <p>CAPTCHA Token: <code>{token}</code></p>
          <p>JWT Token: <code>{jwtToken}</code></p>
          <p className="token-usage-note">
            <strong>Note:</strong> Include this JWT token in the <code>xJwtString</code> header for subsequent API calls.
          </p>
        </div>
      )}

      <footer className="app-footer">
        <p>EU CAPTCHA React Component Demo - {new Date().getFullYear()}</p>
      </footer>
    </div>
  )
}

export default App
