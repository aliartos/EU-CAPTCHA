# AWS Integration Toggle for EU CAPTCHA

This document describes the changes made to implement an AWS integration toggle for the EU CAPTCHA project.

## Overview

The EU CAPTCHA project has been updated to allow users to choose whether to use AWS integrations or not. This is controlled by a new application property `aws.enabled`, which acts as a master switch for all AWS integrations.

## Changes Made

1. Added a new property `aws.enabled` in `application.properties` to control all AWS integrations
2. Modified the `CacheClientFactory` to respect this property and only use `MemcachedCacheClient` if both `aws.enabled` is true and `cache.type` is "memcached"
3. Modified the `ScheduledTasks` class to respect both `aws.enabled` and `aws.s3.enabled` properties
4. Updated the `CaptchaUsers` class to initialize `validUsers` with an empty map if it's not set by `ScheduledTasks`
5. Updated the `CaptchaController` class to bypass user validation when AWS is disabled
6. Updated the `CacheClientFactoryTest` class to account for these changes

## CaptchaController Changes

The `CaptchaController` class has been modified to bypass user validation when AWS is disabled. This is necessary because when AWS is disabled, the `captchaUsers.validUsers` map is empty, which would cause all requests to be rejected with a "Token is missing or invalid!" error.

The changes include:

1. Injecting the `aws.enabled` property into the controller
2. Modifying the three places where `captchaUsers.isNoValidUser()` is called to check if AWS is enabled first:
   - In the `getCaptchaImage` method
   - In the `reloadCaptchaImage` method
   - In the `validateCaptcha` method

With these changes, when AWS is disabled, the controller will skip the user validation check and allow requests to proceed regardless of the JWT token provided.

## How to Use

To enable or disable AWS integrations, set the `aws.enabled` property in `application.properties`:

```properties
# Master switch for all AWS integrations
aws.enabled=true  # or false
```

When `aws.enabled` is set to `false`:
- The application will use Redis for caching instead of AWS ElastiCache Memcached
- The `ScheduledTasks` bean will not be created, so no AWS S3 operations will be performed
- The `CaptchaUsers` class will initialize `validUsers` with an empty map
- The `CaptchaController` will bypass user validation, allowing requests to proceed regardless of the JWT token provided

When `aws.enabled` is set to `true`:
- If `cache.type` is "memcached", the application will use AWS ElastiCache Memcached for caching
- If `aws.s3.enabled` is also `true`, the `ScheduledTasks` bean will be created and AWS S3 operations will be performed
- The `CaptchaUsers` class will be populated with data from the file downloaded from AWS S3 or read from a local file
- The `CaptchaController` will perform user validation, rejecting requests with invalid JWT tokens

## Note

The `aws.s3.enabled` property is still used to control AWS S3 operations specifically, but it's only effective if `aws.enabled` is `true`.
