# EU CAPTCHA React Component

This project implements a reusable React component for integrating the EU CAPTCHA service into your web applications.

## Demo

This repository includes a demo application that showcases the EU CAPTCHA component. To run the demo:

```bash
# Install dependencies
npm install

# Start the development server
npm run dev
```

## Using the EU CAPTCHA Component

The component can be easily integrated into any React application.

### Installation

```bash
# If using the component in another project, you can copy the component files or publish it as a package
npm install @eu/captcha-react  # Example if published to npm
```

### Basic Usage

```tsx
import { EUCaptcha } from './components/EUCaptcha';

function MyForm() {
  const handleVerify = (token: string) => {
    console.log('CAPTCHA verified with token:', token);
    // Process the successful verification, e.g., enable submit button
  };

  const handleError = (error: Error) => {
    console.error('CAPTCHA error:', error);
    // Handle the error
  };

  return (
    <form>
      {/* Your form fields */}
      
      <div>
        <label>Please verify you are human</label>
        <EUCaptcha 
          apiBaseUrl="https://your-eu-captcha-service-url"
          onVerify={handleVerify}
          onError={handleError}
        />
      </div>
      
      <button type="submit">Submit</button>
    </form>
  );
}
```

### Props

The EU CAPTCHA component accepts the following props:

| Prop | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `apiBaseUrl` | string | Yes | - | Base URL for the EU CAPTCHA service |
| `onVerify` | function | No | - | Callback function called when CAPTCHA is successfully verified |
| `onError` | function | No | - | Callback function called when CAPTCHA fails verification |
| `className` | string | No | '' | Additional CSS class name for the container |
| `language` | string | No | 'en' | Language for CAPTCHA interface (e.g., 'en', 'fr', 'de') |
| `captchaType` | string | No | 'image' | Type of CAPTCHA to display ('image' or 'audio') |

## API Integration

The component is designed to work with the EU CAPTCHA service API. It expects the following endpoints:

- `GET {apiBaseUrl}/api/captchaImg?language={language}` - Fetches an image CAPTCHA
- `GET {apiBaseUrl}/api/audioCaptcha?language={language}` - Fetches an audio CAPTCHA
- `POST {apiBaseUrl}/api/verify` - Verifies the CAPTCHA solution

The API responses should follow this format:

```typescript
// Image/Audio CAPTCHA response
{
  captchaId: string;
  captchaImg?: string;  // Base64 encoded image
  audioCaptcha?: string;  // Base64 encoded audio
  currentLanguage: string;
}

// Verification response
{
  success: boolean;
  token?: string;  // Provided on successful verification
}
```

## Accessibility

The component supports both image and audio CAPTCHAs to accommodate users with visual impairments. Users can toggle between these modes using the provided button.

## Customization

You can customize the appearance of the component by:

1. Passing a custom `className` prop
2. Overriding the CSS variables defined in `EUCaptcha.css`
3. Creating your own CSS that targets the component's class names

## License

This project is licensed under the terms of the European Union Public License (EUPL).
