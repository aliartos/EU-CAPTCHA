// EU CAPTCHA Demo JavaScript
// This file contains the code to interact with the EU CAPTCHA API endpoints

let useAudio = false;
let euCaptchaToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJUZXh0dWFsIGV4YW1wbGUiLCJuYW1lIjoiRVVfQ0FQVENIQSIsImlhdCI6MTUxNjIzOTAyMn0.MJfBKb01QKVVafes5DoDDoRAVNios3H_nrWYWZZ30Vs";
let currentRotationAngle = 0;

// Function to handle audio playback
function onPlayAudio() {
    useAudio = true;
}

// Function to handle capitalization toggle
function toggleCapitalized() {
    const checkBox = document.getElementById('capitalized');
    if (checkBox.checked === true) {
        sessionStorage.setItem("capitalized", "true");
    } else {
        sessionStorage.setItem("capitalized", "false");
    }
    getCaptcha();
}

// Function to get the selected language for standard CAPTCHA
function getLanguage() {
    let language = document.getElementById('language-select').value;
    if (language) {
        return language;
    } else {
        return "en-GB";
    }
}

// Function to get the selected language for rotation CAPTCHA
function getRotationLanguage() {
    let language = document.getElementById('rotation-language-select').value;
    if (language) {
        return language;
    } else {
        return "en-GB";
    }
}

// Function to get a new CAPTCHA
function getCaptcha() {
    const capitalized = document.getElementById('capitalized').checked;
    
    fetch(`api/captchaImg?locale=${getLanguage()}&captchaLength=8&captchaType=STANDARD&capitalized=${capitalized}`, {
        method: 'GET',
        headers: {
            'xJwtString': euCaptchaToken
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('captcha-image').src = "data:image/png;base64," + data.captchaImg;
        document.getElementById('captcha-image').setAttribute("captchaId", data.captchaId);
        document.getElementById('audio-captcha').src = "data:audio/wav;base64," + data.audioCaptcha;
        
        // Hide result messages
        document.getElementById('success-message').style.display = 'none';
        document.getElementById('error-message').style.display = 'none';
    })
    .catch(error => {
        console.error('Error fetching CAPTCHA:', error);
        document.getElementById('error-message').style.display = 'block';
    });
}

// Function to reload the CAPTCHA
function reloadCaptcha() {
    const captchaId = document.getElementById('captcha-image').getAttribute("captchaId");
    const capitalized = document.getElementById('capitalized').checked;
    
    fetch(`api/reloadCaptchaImg/${captchaId}?locale=${getLanguage()}&captchaLength=8&captchaType=STANDARD&capitalized=${capitalized}`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'xJwtString': euCaptchaToken
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('captcha-image').src = "data:image/png;base64," + data.captchaImg;
        document.getElementById('captcha-image').setAttribute("captchaId", data.captchaId);
        document.getElementById('audio-captcha').src = "data:audio/wav;base64," + data.audioCaptcha;
        document.getElementById('captcha-answer').value = "";
        useAudio = false;
        
        // Hide result messages
        document.getElementById('success-message').style.display = 'none';
        document.getElementById('error-message').style.display = 'none';
    })
    .catch(error => {
        console.error('Error reloading CAPTCHA:', error);
        document.getElementById('error-message').style.display = 'block';
    });
}

// Function to validate the CAPTCHA answer
function validateCaptcha() {
    const captchaId = document.getElementById('captcha-image').getAttribute("captchaId");
    const captchaAnswer = document.getElementById('captcha-answer').value;
    
    const params = new URLSearchParams();
    params.append('captchaAnswer', captchaAnswer);
    params.append('useAudio', useAudio);
    params.append('captchaType', 'STANDARD');
    
    fetch(`api/validateCaptcha/${captchaId}`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'xJwtString': euCaptchaToken
        },
        body: params
    })
    .then(response => response.json())
    .then(data => {
        if (data.responseCaptcha === 'success') {
            document.getElementById('success-message').style.display = 'block';
            document.getElementById('error-message').style.display = 'none';
        } else {
            document.getElementById('error-message').style.display = 'block';
            document.getElementById('success-message').style.display = 'none';
            reloadCaptcha();
        }
    })
    .catch(error => {
        console.error('Error validating CAPTCHA:', error);
        document.getElementById('error-message').style.display = 'block';
        document.getElementById('success-message').style.display = 'none';
        reloadCaptcha();
    });
}

// Function to get a new rotation CAPTCHA
function getRotationCaptcha() {
    fetch(`api/captchaImg?locale=${getRotationLanguage()}&captchaType=WHATS_UP&degree=15`, {
        method: 'GET',
        headers: {
            'xJwtString': euCaptchaToken
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('rotation-captcha-image').src = "data:image/png;base64," + data.captchaImg;
        document.getElementById('rotation-captcha-image').setAttribute("captchaId", data.captchaId);
        
        // Reset slider and rotation
        const slider = document.getElementById('rotation-slider');
        slider.value = 0;
        document.getElementById('rotation-value').textContent = '0';
        document.getElementById('rotation-captcha-image').style.transform = 'rotate(0deg)';
        currentRotationAngle = 0;
        
        // Hide result messages
        document.getElementById('rotation-success-message').style.display = 'none';
        document.getElementById('rotation-error-message').style.display = 'none';
    })
    .catch(error => {
        console.error('Error fetching rotation CAPTCHA:', error);
        document.getElementById('rotation-error-message').style.display = 'block';
    });
}

// Function to reload the rotation CAPTCHA
function reloadRotationCaptcha() {
    const captchaId = document.getElementById('rotation-captcha-image').getAttribute("captchaId");
    
    fetch(`api/reloadCaptchaImg/${captchaId}?locale=${getRotationLanguage()}&captchaType=WHATS_UP&degree=15`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'xJwtString': euCaptchaToken
        }
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('rotation-captcha-image').src = "data:image/png;base64," + data.captchaImg;
        document.getElementById('rotation-captcha-image').setAttribute("captchaId", data.captchaId);
        
        // Reset slider and rotation
        const slider = document.getElementById('rotation-slider');
        slider.value = 0;
        document.getElementById('rotation-value').textContent = '0';
        document.getElementById('rotation-captcha-image').style.transform = 'rotate(0deg)';
        currentRotationAngle = 0;
        
        // Hide result messages
        document.getElementById('rotation-success-message').style.display = 'none';
        document.getElementById('rotation-error-message').style.display = 'none';
    })
    .catch(error => {
        console.error('Error reloading rotation CAPTCHA:', error);
        document.getElementById('rotation-error-message').style.display = 'block';
    });
}

// Function to validate the rotation CAPTCHA
function validateRotationCaptcha() {
    const captchaId = document.getElementById('rotation-captcha-image').getAttribute("captchaId");
    
    const params = new URLSearchParams();
    params.append('captchaAnswer', currentRotationAngle.toString());
    params.append('useAudio', 'false');
    params.append('captchaType', 'WHATS_UP');
    
    fetch(`api/validateCaptcha/${captchaId}`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'xJwtString': euCaptchaToken
        },
        body: params
    })
    .then(response => response.json())
    .then(data => {
        if (data.responseCaptcha === 'success') {
            document.getElementById('rotation-success-message').style.display = 'block';
            document.getElementById('rotation-error-message').style.display = 'none';
        } else {
            document.getElementById('rotation-error-message').style.display = 'block';
            document.getElementById('rotation-success-message').style.display = 'none';
            reloadRotationCaptcha();
        }
    })
    .catch(error => {
        console.error('Error validating rotation CAPTCHA:', error);
        document.getElementById('rotation-error-message').style.display = 'block';
        document.getElementById('rotation-success-message').style.display = 'none';
        reloadRotationCaptcha();
    });
}

// Handle rotation slider
function handleRotationSlider() {
    const slider = document.getElementById('rotation-slider');
    const valueDisplay = document.getElementById('rotation-value');
    const image = document.getElementById('rotation-captcha-image');
    
    // Update rotation angle and display
    currentRotationAngle = parseInt(slider.value);
    valueDisplay.textContent = currentRotationAngle;
    image.style.transform = `rotate(${currentRotationAngle}deg)`;
}

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Set up event listeners for standard CAPTCHA
    document.getElementById('reload-button').addEventListener('click', reloadCaptcha);
    document.getElementById('submit-button').addEventListener('click', validateCaptcha);
    document.getElementById('capitalized').addEventListener('change', toggleCapitalized);
    document.getElementById('language-select').addEventListener('change', getCaptcha);
    document.getElementById('audio-captcha').addEventListener('play', onPlayAudio);
    
    // Set up event listeners for rotation CAPTCHA
    document.getElementById('rotation-reload-button').addEventListener('click', reloadRotationCaptcha);
    document.getElementById('rotation-submit-button').addEventListener('click', validateRotationCaptcha);
    document.getElementById('rotation-language-select').addEventListener('change', getRotationCaptcha);
    
    const rotationSlider = document.getElementById('rotation-slider');
    rotationSlider.addEventListener('input', handleRotationSlider);
    
    // Get initial CAPTCHAs
    getCaptcha();
    getRotationCaptcha();
    
    // Handle Enter key in the answer input
    document.getElementById('captcha-answer').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            validateCaptcha();
            e.preventDefault();
        }
    });
});