import { useState, useEffect, useCallback } from 'react';
import './EUCaptcha.css';

export interface EUCaptchaProps {
  /**
   * Base URL for the EU CAPTCHA service
   */
  apiBaseUrl: string;
  /**
   * Function called when CAPTCHA is successfully verified
   */
  onVerify?: (token: string) => void;
  /**
   * Function called when CAPTCHA fails verification
   */
  onError?: (error: Error) => void;
  /**
   * CSS class name for the container
   */
  className?: string;
  /**
   * Language for CAPTCHA interface (e.g., 'en-GB', 'fr-FR', 'de-DE')
   */
  language?: string;
  /**
   * Type of CAPTCHA to display ('STANDARD', 'WHATS_UP', or 'SLIDING')
   */
  captchaType?: 'STANDARD' | 'WHATS_UP' | 'SLIDING';
  /**
   * Length of the CAPTCHA text
   */
  captchaLength?: number;
  /**
   * Whether to use capitalized letters
   */
  capitalized?: boolean;
}

export interface CaptchaData {
  captchaId: string;
  captchaImg?: string;
  audioCaptcha?: string;
  degree?: number;
}

export const EUCaptcha: React.FC<EUCaptchaProps> = ({
  apiBaseUrl,
  onVerify,
  onError,
  className = '',
  language = 'en-GB',
  captchaType = 'STANDARD',
  captchaLength = 8,
  capitalized = true,
}) => {
  const [captchaData, setCaptchaData] = useState<CaptchaData | null>(null);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [useAudio, setUseAudio] = useState(false);

  // Common request headers with required xJwtString token
  const getCommonHeaders = useCallback(() => ({
    'Accept': 'application/json',
    'xJwtString': 'EuCaptchaToken'
  }), []);

  const fetchCaptcha = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Using the correct EU CAPTCHA API URL format
      const url = new URL(`${apiBaseUrl}/api/captchaImg`);
      url.searchParams.append('locale', language);
      url.searchParams.append('captchaLength', captchaLength.toString());
      url.searchParams.append('captchaType', captchaType);
      url.searchParams.append('capitalized', capitalized.toString());
      
      const response = await fetch(url.toString(), {
        headers: getCommonHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Failed to fetch CAPTCHA: ${response.statusText}`);
      }
      
      const data = await response.json();
      
      setCaptchaData({
        captchaId: data.captchaId || '',
        captchaImg: data.captchaImg || '',
        audioCaptcha: data.audioCaptcha || '',
        degree: data.degree
      });
      
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Unknown error occurred');
      setError(error.message);
      if (onError) onError(error);
    } finally {
      setLoading(false);
    }
  }, [apiBaseUrl, language, captchaType, captchaLength, capitalized, onError, getCommonHeaders]);

  const reloadCaptcha = useCallback(async () => {
    if (!captchaData?.captchaId) return fetchCaptcha();
    
    try {
      setLoading(true);
      setError(null);
      
      // Using the correct EU CAPTCHA API reload URL format
      const url = new URL(`${apiBaseUrl}/api/reloadCaptchaImg/${captchaData.captchaId}`);
      url.searchParams.append('locale', language);
      url.searchParams.append('captchaLength', captchaLength.toString());
      url.searchParams.append('captchaType', captchaType);
      url.searchParams.append('capitalized', capitalized.toString());
      
      const response = await fetch(url.toString(), {
        headers: getCommonHeaders()
      });
      
      if (!response.ok) {
        throw new Error(`Failed to reload CAPTCHA: ${response.statusText}`);
      }
      
      const data = await response.json();
      
      setCaptchaData({
        captchaId: data.captchaId || '',
        captchaImg: data.captchaImg || '',
        audioCaptcha: data.audioCaptcha || '',
        degree: data.degree
      });
      
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Unknown error occurred');
      setError(error.message);
      if (onError) onError(error);
    } finally {
      setLoading(false);
    }
  }, [apiBaseUrl, captchaData, language, captchaType, captchaLength, capitalized, fetchCaptcha, onError, getCommonHeaders]);

  const verifyCaptcha = useCallback(async () => {
    if (!captchaData || !userInput) return;
    
    try {
      setLoading(true);
      setError(null);
      
      // Using the correct EU CAPTCHA API validation URL format
      const url = `${apiBaseUrl}/api/validateCaptcha/${captchaData.captchaId}`;
      
      const formData = new URLSearchParams();
      formData.append('captchaAnswer', userInput);
      formData.append('useAudio', useAudio.toString());
      formData.append('captchaType', captchaType);
      
      // Include common headers plus content type
      const headers = {
        ...getCommonHeaders(),
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
      };
      
      const response = await fetch(url, {
        method: 'POST',
        headers: headers,
        body: formData
      });
      
      const data = await response.json();
      
      // Handle both successful responses and 400 error responses
      if (response.ok && data.responseCaptcha === 'success') {
        setSuccess(true);
        if (onVerify) onVerify(captchaData.captchaId || '');
      } else {
        // Even if status is 400, we might get a JSON response that we can use
        setError(data.message || 'Incorrect CAPTCHA response. Please try again.');
        reloadCaptcha(); // Get a new CAPTCHA after failed attempt
      }
      
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Unknown error occurred');
      setError(error.message);
      if (onError) onError(error);
    } finally {
      setLoading(false);
      setUserInput('');
    }
  }, [captchaData, userInput, apiBaseUrl, captchaType, useAudio, reloadCaptcha, onVerify, onError, getCommonHeaders]);

  const handleRefresh = () => {
    setUserInput('');
    setSuccess(false);
    reloadCaptcha();
  };

  const toggleAudio = () => {
    setUseAudio(!useAudio);
  };

  useEffect(() => {
    fetchCaptcha();
  }, [fetchCaptcha]);

  return (
    <div className={`eu-captcha-container ${className}`}>
      <div className="eu-captcha-content">
        {loading && <div className="eu-captcha-loading">Loading...</div>}
        
        {error && (
          <div className="eu-captcha-error">
            {error}
          </div>
        )}
        
        {!loading && captchaData && (
          <>
            <div className="eu-captcha-challenge">
              {!useAudio && captchaData.captchaImg && (
                <img 
                  src={captchaData.captchaImg.startsWith('data:') 
                    ? captchaData.captchaImg 
                    : `data:image/png;base64,${captchaData.captchaImg}`
                  } 
                  alt="CAPTCHA" 
                  className="eu-captcha-image" 
                />
              )}
              
              {useAudio && captchaData.audioCaptcha && (
                <audio 
                  controls 
                  src={captchaData.audioCaptcha.startsWith('data:') 
                    ? captchaData.audioCaptcha 
                    : `data:audio/wav;base64,${captchaData.audioCaptcha}`
                  }
                  className="eu-captcha-audio"
                >
                  Your browser does not support the audio element.
                </audio>
              )}
            </div>
            
            <div className="eu-captcha-controls">
              <button 
                onClick={handleRefresh} 
                type="button" 
                className="eu-captcha-refresh-btn"
                aria-label="Refresh CAPTCHA"
              >
                ↻
              </button>
              
              {captchaType === 'STANDARD' && captchaData.audioCaptcha && (
                <button 
                  onClick={toggleAudio} 
                  type="button" 
                  className="eu-captcha-toggle-btn"
                  aria-label={useAudio ? 'Switch to image CAPTCHA' : 'Switch to audio CAPTCHA'}
                >
                  {useAudio ? '🖼️' : '🔊'}
                </button>
              )}
            </div>
            
            {captchaType === 'WHATS_UP' && captchaData.degree !== undefined && (
              <div className="eu-captcha-rotation-controls">
                <label htmlFor="rotation-slider">Rotate the image to correct position:</label>
                <input 
                  type="range" 
                  id="rotation-slider" 
                  min="0" 
                  max="360" 
                  step="1"
                  value={userInput || '0'} 
                  onChange={(e) => setUserInput(e.target.value)}
                  className="eu-captcha-slider"
                />
                <span className="eu-captcha-rotation-value">{userInput || '0'}°</span>
              </div>
            )}
            
            <div className="eu-captcha-input-container">
              {(captchaType === 'STANDARD' || captchaType === 'SLIDING') && (
                <input
                  type="text"
                  value={userInput}
                  onChange={(e) => setUserInput(e.target.value)}
                  placeholder="Enter CAPTCHA text"
                  className="eu-captcha-input"
                  disabled={loading || success}
                />
              )}
              
              <button
                onClick={verifyCaptcha}
                type="button"
                className="eu-captcha-verify-btn"
                disabled={loading || !userInput || success}
              >
                Verify
              </button>
            </div>
            
            {success && (
              <div className="eu-captcha-success">
                CAPTCHA verified successfully!
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};

export default EUCaptcha;