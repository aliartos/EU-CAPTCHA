import {useState, useEffect, useCallback} from 'react';
import './EUCaptcha.css';
import {JWTPayload, SignJWT} from 'jose';

export interface EUCaptchaProps {
    /**
     * Base URL for the EU CAPTCHA service
     */
    apiBaseUrl: string;
    /**
     * Function called when CAPTCHA is successfully verified
     * @param token - The CAPTCHA ID used for verification reference
     * @param jwtToken - The xJwtString token to use in subsequent API calls
     */
    onVerify?: (token: string, jwtToken: string) => void;
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
    /**
     * Initial xJwtString token to use (optional)
     */
    initialJwtToken?: string;
}

export interface CaptchaData {
    captchaId: string;
    captchaImg?: string;
    audioCaptcha?: string;
    degree?: number;
}

function randomNonce(length = 8) {
    const array = new Uint8Array(length);
    crypto.getRandomValues(array);
    return Array.from(array, dec => dec.toString(16).padStart(2, '0')).join('');
}

async function generateJWT(payload: JWTPayload | null, providedSecret: Uint8Array<ArrayBuffer> | null) {
    // Use the provided payload if it has keys; otherwise, add a random nonce.
    const actualPayload = (payload && Object.keys(payload).length > 0)
        ? payload
        : {nonce: randomNonce()};

    // Use the provided secret or generate a random 32-byte (256-bit) secret.
    const secret = providedSecret || crypto.getRandomValues(new Uint8Array(32));

    // Create and sign the JWT using HS256.
    const token = await new SignJWT(actualPayload)
        .setProtectedHeader({alg: 'HS256'})
        .setIssuedAt()
        .setExpirationTime('2h')
        .sign(secret);

    return token;
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
                                                        initialJwtToken,
                                                    }) => {
    const [captchaData, setCaptchaData] = useState<CaptchaData | null>(null);
    const [userInput, setUserInput] = useState('');
    const [rotationAngle, setRotationAngle] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [useAudio, setUseAudio] = useState(false);
    const [jwtToken, setJwtToken] = useState<string>(initialJwtToken || '');

    // Generate a new JWT token if not provided
    useEffect(() => {
        if (!initialJwtToken) {
            generateJWT(null, null).then(setJwtToken).catch(err => {
                if (onError) onError(err);
            });
        }
    }, [initialJwtToken, onError]);

    // Common request headers with required xJwtString token
    const getCommonHeaders = useCallback(() => ({
        'Accept': 'application/json',
        'xJwtString': jwtToken
    }), [jwtToken]);

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

            if (response.ok) {
                // Extract JWT token from the response headers or use the current one
                const receivedJwtToken = response.headers.get('xJwtString') || jwtToken;
                // Update the JWT token state
                setJwtToken(receivedJwtToken);
            }

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

                // Extract JWT token from the response headers or use the current one
                const receivedJwtToken = response.headers.get('xJwtString') || jwtToken;

                // Update the JWT token state
                setJwtToken(receivedJwtToken);

                // Call onVerify with both the captcha ID and JWT token
                if (onVerify) onVerify(captchaData.captchaId || '', receivedJwtToken);
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
    }, [captchaData, userInput, apiBaseUrl, captchaType, useAudio, reloadCaptcha, onVerify, onError, getCommonHeaders, jwtToken]);

    const handleRefresh = useCallback(() => {
        // Always clear these states when refreshing
        setUserInput('');
        setRotationAngle(0);
        setSuccess(false);
        setError(null);

        // Show loading state immediately for better feedback
        setLoading(true);

        // Use a small timeout to ensure the loading state is visible
        // This provides better visual feedback that something is happening
        setTimeout(() => {
            reloadCaptcha().catch((err) => {
                const error = err instanceof Error ? err : new Error('Failed to refresh CAPTCHA');
                setError(error.message);
                if (onError) onError(error);
                setLoading(false);
            });
        }, 100);
    }, [reloadCaptcha, onError]);

    const toggleAudio = () => {
        setUseAudio(!useAudio);
    };

    const handleRotationChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        // Get the raw value from the slider
        const rawValue = parseInt(e.target.value, 10);

        // Round to the nearest 15 degrees
        const roundedAngle = Math.round(rawValue / 15) * 15;

        // Update rotation angle state and user input
        setRotationAngle(roundedAngle);
        setUserInput(roundedAngle.toString());
    };

    useEffect(() => {
        if (!jwtToken) return;
        fetchCaptcha();
    }, [fetchCaptcha, jwtToken]);

    return (
        <div className={`eu-captcha-container ${className}`}>
            <div className="eu-captcha-content">
                {loading && <div className="eu-captcha-loading">Loading...</div>}

                {error && (
                    <div className="eu-captcha-error">
                        {error}
                        <button
                            onClick={handleRefresh}
                            className="eu-captcha-error-refresh"
                            aria-label="Try again"
                        >
                            Try again
                        </button>
                    </div>
                )}

                {!loading && captchaData && (
                    <>
                        <div className="eu-captcha-challenge">
                            {!useAudio && captchaData.captchaImg && captchaType !== 'WHATS_UP' && (
                                <img
                                    src={captchaData.captchaImg.startsWith('data:')
                                        ? captchaData.captchaImg
                                        : `data:image/png;base64,${captchaData.captchaImg}`
                                    }
                                    alt="CAPTCHA"
                                    className="eu-captcha-image"
                                />
                            )}

                            {!useAudio && captchaData.captchaImg && captchaType === 'WHATS_UP' && (
                                <div className="eu-captcha-rotation-wrapper">
                                    <img
                                        src={captchaData.captchaImg.startsWith('data:')
                                            ? captchaData.captchaImg
                                            : `data:image/png;base64,${captchaData.captchaImg}`
                                        }
                                        alt="Rotatable CAPTCHA"
                                        className="eu-captcha-image"
                                        style={{
                                            transform: `rotate(${rotationAngle}deg)`,
                                            transition: 'transform 0.2s ease-out'
                                        }}
                                    />
                                </div>
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

                        {captchaType === 'WHATS_UP' && (
                            <div className="eu-captcha-rotation-controls">
                                <label htmlFor="rotation-slider">Rotate the image to correct position:</label>
                                <input
                                    type="range"
                                    id="rotation-slider"
                                    min="0"
                                    max="345" // Changed to 345 (360-15) to ensure values are multiples of 15
                                    step="15"  // Set step to 15 degrees
                                    value={rotationAngle}
                                    onChange={handleRotationChange}
                                    className="eu-captcha-slider"
                                />
                                <span className="eu-captcha-rotation-value">{rotationAngle}°</span>
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
                                <button
                                    onClick={handleRefresh}
                                    className="eu-captcha-success-refresh"
                                    aria-label="Get a new CAPTCHA"
                                >
                                    Get a new CAPTCHA
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};

export default EUCaptcha;