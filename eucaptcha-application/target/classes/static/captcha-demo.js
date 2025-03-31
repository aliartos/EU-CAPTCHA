// EU CAPTCHA Demo JavaScript
// This file contains the code to interact with the EU CAPTCHA API endpoints

let useAudio = false;
let euCaptchaToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJUZXh0dWFsIGV4YW1wbGUiLCJuYW1lIjoiRVVfQ0FQVENIQSIsImlhdCI6MTUxNjIzOTAyMn0.MJfBKb01QKVVafes5DoDDoRAVNios3H_nrWYWZZ30Vs";

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

// Function to get the selected language
function getLanguage() {
    let language = document.getElementById('language-select').value;
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

// Initialize the page
document.addEventListener('DOMContentLoaded', function() {
    // Set up event listeners
    document.getElementById('reload-button').addEventListener('click', reloadCaptcha);
    document.getElementById('submit-button').addEventListener('click', validateCaptcha);
    document.getElementById('capitalized').addEventListener('change', toggleCapitalized);
    document.getElementById('language-select').addEventListener('change', getCaptcha);
    document.getElementById('audio-captcha').addEventListener('play', onPlayAudio);
    
    // Get initial CAPTCHA
    getCaptcha();
    
    // Handle Enter key in the answer input
    document.getElementById('captcha-answer').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            validateCaptcha();
            e.preventDefault();
        }
    });
});