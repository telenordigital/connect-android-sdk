# Connect SDK for Android

* [Prerequisites](#prerequisites)
* [Install](#install)
* [Basic usage](#basic-usage)
  * [Authenticating a user and authorizing app](#authenticating-a-user-and-authorizing-app)
  * [Getting a valid access token](#getting-a-valid-access-token)
  * [Access user information](#access-user-information)
* [Example app](#example-app)
* [Setup](#setup)
  * [Set Staging or Production Environment](#set-staging-or-production-environment)
  * [Select Client type](#select-client-type)
  * [Adding the Client ID and redirect URI](#adding-the-client-id-and-redirect-uri)
  * [Adding permissions](#adding-permissions)
* [Detailed Usage](#detailed-usage)
  * [Adding a ConnectLoginButton](#adding-a-connectloginbutton)
  * [Next steps for public clients](#next-steps-for-public-clients)
  * [Retrieving information about the logged in user](#retrieving-information-about-the-logged-in-user)
* [Mobile Connect](#mobile-connect)

The Connect SDK for Android allows developers to create applications that use CONNECT ID for
sign-in. More information about Telenor Connect can be found on our
[partner portal](http://portal.telenordigital.com/).

Documentation for CONNECT ID and it's APIs can be found at
[telenordigital.github.io/id-docs.telenordigital.com](https://telenordigital.github.io/id-docs.telenordigital.com/).

Please use the GitHub _Watch_ feature to get notified on new releases of the SDK.

## Prerequisites

Before being able to use Telenor Connect in your application, you first need to
[get your application registered](http://docs.telenordigital.com/getting-started/)
with Telenor Connect.

## Install

The binaries are included on JCenter, so the SDK can be added by including a line in your `build.gradle` file for your app.

```gradle
dependencies {
    // ...
    compile 'com.telenor.connect:connect-android-sdk:1.4.1' // add this line
}
```

You might have to add JCenter as a repository on your top-level `build.gradle` file if this isn't done already:
```gradle
allprojects {
    repositories {
        jcenter()
    }
}
```

## Basic usage

Notice: The AndroidManifest.xml needs to be [setup](#setup) before you can use the SDK.

### Authenticating a user and authorizing app

You can authenticate the user and authorize your application by using a `ConnectLoginButton`:


```java
public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize the SDK
        ConnectSdk.sdkInitialize(getApplicationContext());
        // Find the ConnectLoginButton present in activity_sign_in.xml
        ConnectLoginButton loginButton = (ConnectLoginButton) findViewById(R.id.login_button);
        // Set the scope. The user can click the button afterwords
        loginButton.setLoginScopeTokens("profile openid");

        // When users have clicked the loginButton and signed in, this method call will check
        // that, and run the success callback method.
        // It checks if the Activity was started by a valid call to the redirect uri with a
        // code and state, for example example-clientid://oauth2callback?code=123&state=xyz .
        // It also takes a callback that has a onSuccess and onError function.
        // If it is a success we have stored tokens, and can go to SignedInActivity.
        // Not needed if not using Chrome Custom Tabs.
        ConnectSdk.handleRedirectUriCallIfPresent(getIntent(), new ConnectCallback() {
            @Override
            public void onSuccess(Object successData) {
                goToSignedInActivity();
            }

            @Override
            public void onError(Object errorData) {
                Log.e(ConnectUtils.LOG_TAG, errorData.toString());
            }
        });
    }

    private void goToSignedInActivity() {
        final Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
        startActivity(intent);
        finish();
    }

    // Overriding onActivityResult here serves the same purpose as
    // handleRedirectUriCallIfPresent further up. It is needed on older devices
    // that don't support Chrome Custom Tabs, or if the intent-filter for the redirect uri
    // to this activity is missing.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            goToSignedInActivity();
        }
    }

}
```

Where `activity_sign_in.xml` looks like this:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.telenor.connect.connectidexample.SignInActivity">

    <com.telenor.connect.ui.ConnectLoginButton
        android:layout_centerInParent="true"
        android:id="@+id/login_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        style="@style/com_telenor_ConnectButton.Dark"/>

</RelativeLayout>

```

### Getting a valid access token

Once the user is signed in you can get a valid Access Token by calling `ConnectSdk.getValidAccessToken(…)`:

```java
ConnectSdk.getValidAccessToken(new AccessTokenCallback() {
    @Override
    public void onSuccess(String accessToken) {
        // app code
    }

    @Override
    public void onError(Object errorData) {
        // app code
    }
});
```

The above method will always return a valid access token, unless no user is signed in, in which
case you will get a `ConnectRefreshTokenMissingException`.

You can also manually check the expiration time of the stored access token and check if it is expired.

```java
Date expirationTime = ConnectSdk.getAccessTokenExpirationTime();

if (expirationTime == null) { // if no user is signed in
    goToLogin();
    return;
}

if (new Date().before(expirationTime)) {
    Toast.makeText(this, "Token has not expired yet. expirationTime=" + expirationTime, Toast.LENGTH_LONG).show();
    String validAccessToken = ConnectSdk.getAccessToken();
    // use the access token for something
} else {
    Toast.makeText(this, "Token has expired. expirationTime=" + expirationTime, Toast.LENGTH_LONG).show();
    ConnectSdk.updateTokens(new AccessTokenCallback() {
        @Override
        public void onSuccess(String accessToken) {
            Toast.makeText(SignedInActivity.this, "Got new access token and expiration time. New time: " + ConnectSdk.getAccessTokenExpirationTime(), Toast.LENGTH_LONG).show();
            String validAccessToken = ConnectSdk.getAccessToken();
        }

        @Override
        public void onError(Object errorData) {
            Toast.makeText(SignedInActivity.this, "Failed to refresh token.", Toast.LENGTH_LONG).show();
        }
    });
}
```

### Access User Information

The SDK allows for two ways of accessing user information. Either by requesting and accessing an `IdToken` or by making a network call using `getUserInfo(…)`.

Note: The presence of the fields depend on the **scope** and **claim** variables that were given at sign in time. See http://docs.telenordigital.com/apis/connect/id/authentication.html for more details.

#### Accessing User Information by IdToken

When authenticating the user make sure to request the `openid` scope:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_in);

    ConnectSdk.sdkInitialize(getApplicationContext());
    ConnectLoginButton loginButton = (ConnectLoginButton) findViewById(R.id.login_button);
    loginButton.setLoginScopeTokens("openid");
}
```

When the user has authenticated you can call:
```java
IdToken idToken = ConnectSdk.getIdToken();
```

And access user information by calling for example:
```java
String email = idToken.getEmail();
```

#### Accessing User Information by getUserInfo(…)

You can also access user information by making a network call using `getUserInfo(…)`:

```java
ConnectSdk.getUserInfo(new Callback<UserInfo>() {
    @Override
    public void success(UserInfo userInfo, Response response) {
        // app code
    }

    @Override
    public void failure(RetrofitError error) {
        // app code
    }
});
```

## Example app

For a full example, which includes both the setup in `AndroidManifest.xml` and sign in, see the [`connect-id-example` app](https://github.com/telenordigital/connect-android-sdk/tree/master/connect-id-example).

Run it by following these steps:

1. `git clone git@github.com:telenordigital/connect-android-sdk.git`
2. Open `connect-android-sdk` in Android Studio.
3. Select `id-example` Run Configuration and and run it (ctrl+r)

## Setup

Before using the SDK some entries have to be added to `AndroidManifest.xml`. The example app contains [a full example of a valid `AndroidManifest.xml`](https://github.com/telenordigital/connect-android-sdk/blob/master/connect-id-example/src/main/AndroidManifest.xml).

### Set Staging or Production Environment

Telenor Connect has 2 [environments](http://docs.telenordigital.com/connect/environments.html)
that can be used, staging and production. The environment can be selected using the
`com.telenor.connect.USE_STAGING` meta-data property in your AndroidManifest.xml

```xml
<meta-data
        android:name="com.telenor.connect.USE_STAGING"
        android:value="true" />
```

Set this to `false` if you want to use the production environment.

### Adding the client ID and redirect URI

The Connect ID integration requires a client ID and a redirect URI to work. You should receive these when registering your application.

The client ID and redirect URI should be added to your `strings.xml` file. Add strings with the names `connect_client_id` and `connect_redirect_uri`.

```xml
<string name="connect_client_id">example-clientid</string>
<string name="connect_redirect_uri">example-clientid://oauth2callback</string>
```

Add `meta-data` entries to the `application` section of the manifest.

```xml
<application>
        ...
        <meta-data
                android:name="com.telenor.connect.CLIENT_ID"
                android:value="@string/connect_client_id" />
        <meta-data
                android:name="com.telenor.connect.REDIRECT_URI"
                android:value="@string/connect_redirect_uri" />
        ...
</application>
```

### Select client type and handle the redirect URI

Connect ID supports two different client types: _public_ and _confidential_. Please see the
[Native app guide](http://docs.telenordigital.com/connect/id/native_apps.html) to help you make a
decision.

#### Registering the redirect URI in Android
**Note**: If you do not wish to use the Chrome Custom Tabs feature do not add this to the manifest.

For your app to respond to calls to the redirect URI you need to add an `intent-filter` to your
`Activity` to register this in the Android system. This will allow the
[Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) used by
`ConnectLoginButton` and external browsers to get back to your app.


```xml
<activity android:name=".SignInActivity" >
	<intent-filter>
		<data android:scheme="@string/connect_redirect_uri_scheme" />
		<action android:name="android.intent.action.VIEW" />
		<category android:name="android.intent.category.DEFAULT" />
		<category android:name="android.intent.category.BROWSABLE" />
	</intent-filter>
</activity>
```

#### Public clients

If the app is a public client you need an `Activity` that calls `ConnectSdk.handleRedirectUriCallIfPresent`, as in the [authenticating example above](#authenticating-a-user-and-authorizing-app).

If the app is not using the Chrome Custom Tab feature you only need to override the
`onActivityResult(…)`, also as in the
[authenticating example above](#authenticating-a-user-and-authorizing-app).

#### Confidential clients

Add the following to the manifest:
```xml
<meta-data
	android:name="com.telenor.connect.CONFIDENTIAL_CLIENT"
	android:value="true" />
```

A confidential client can access the **code** parameter from the `Intent` of the Activity with the [intent-filter](#registering-the-redirect-uri-in-android), or the `onActivityResult()` method. This is the _authorization code_. This authorization code must be sent to the server-side part of your client.

The helper method `ConnectSdk.hasValidRedirectUrlCall(Intent intent)` will return `true` if a valid code is present in the `Activity`'s `Intent`. The helper method `ConnectSdk.getCodeFromIntent(Intent intent)` can then be used to get the **code**:

```java
Intent intent = getIntent();
if (ConnectSdk.hasValidRedirectUrlCall(intent)) {
	String authorizationCode = ConnectSdk.getCodeFromIntent(intent);
	// App code using code

    // Debug line:
    Toast.makeText(this, "authorizationCode=" + authorizationCode, Toast.LENGTH_LONG).show();
}
```

The `onActivityResult(…)` method will have to be changed to send the authorization code to
the server-side part of the client:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
        return;
    }

    String authorizationCode = data.getStringExtra("code");

    // Debug line:
    Toast.makeText(this, "authorizationCode=" + authorizationCode, Toast.LENGTH_LONG).show();

    // Send the authorizationCode to the server-side of the client as described in the docs
    // on confidential clients: http://docs.telenordigital.com/connect/id/native_apps.html
    // This can done by for example Android AsyncTask or using the Retrofit library.
    // The server-side of the client must send back a session ID that the native app code
    // stores. Further requests go directly to the server-side of the client with the
    // session ID to identify the correct tokens for the server-side.
}
```

### Adding permissions

Open your application's `AndroidManifest.xml` file and add the permission required to allow your
application to access the internet.

```xml
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET"/>
```

Optionally you can enable the feature that automatically fills in verification PIN codes received
on SMS by adding the following permissions, when you are not using the Chrome Custom Tab feature.

```xml
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
```

Note: You should be conscious about the security implications of using this feature. When using this feature your application will load received SMS into memory for up to 60 seconds. Upon finding an SMS with the word `CONNECT` and a PIN-code, the PIN code will be parsed and passed back to a callback JavaScript function. More discussion can be found in issue [#15](https://github.com/telenordigital/connect-android-sdk/issues/15).


#### Add ConnectActivity for sign in

The `ConnectActivity` needs to be added to the manifest in order for the SDK to work on devices not using the Chrome Custom Tab feature. Also if the `intent-filter` is missing the SDK will fall back to use this `Activity`. Add it to the `application` section.

```xml
<application>
...
    <activity
        android:name="com.telenor.connect.ui.ConnectActivity"
        android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
        android:label="@string/app_name"
        android:theme="@android:style/Theme.Holo.Light.NoActionBar" />
...
</application>
```


## Detailed usage

### Adding a ConnectLoginButton

To let the SDK handle Connect ID login, a `ConnectLoginButton` can be added to your layout. This is
a custom `Button` implementation that has the standard Connect button look-and-feel.

Firstly add a button to your layout XML files using the class name
`com.telenor.connect.ConnectLoginButton`:

```xml
<com.telenor.connect.ConnectLoginButton
    android:id="@+id/login_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Then add the required [scope tokens](http://docs.telenordigital.com/connect/id/scope.html) for your
application to the button in the `onCreate()` method of your `Activity` class.

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    ...
    ConnectLoginButton button = (ConnectLoginButton) findViewById(R.id.login_button);
    button.setLoginScopeTokens("profile");
}
```

The `onActivityResult()` method of your `Activity` will be called with `Activity.RESULT_OK` as
`resultCode` if the login was successful or `Activity.RESULT_CANCELED` when there was an error.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == Activity.RESULT_OK) {
        // App code
    } else if (resultCode == Activity.RESULT_CANCELED) {
        // App code
    }
}

```

Or if the Chrome Custom Tab feature is used, in a `ConnectCallback` in the Activity that has the
`intent-filter` with `connect_client_id` in your AndroidManifest:
```java
ConnectSdk.handleRedirectUriCallIfPresent(getIntent(), new ConnectCallback() {
    @Override
    public void onSuccess(Object successData) {
        // App code
    }

    @Override
    public void onError(Object errorData) {
        // App code
    }
});

```

#### Adding claims

To add additional [claims to your Connect request](http://docs.telenordigital.com/apis/connect/id/authentication.html#authorization-server-user-authorization),
you can use the `setClaims` method on the `ConnectLoginButton`.

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    ...
    ConnectLoginButton button = (ConnectLoginButton) findViewById(R.id.login_button);
    button.setLoginScopeTokens("profile");
    button.setClaims(new Claims(Claims.PHONE_NUMBER, Claims.EMAIL));
}
```

#### Example: Setting the UI locale
To set the locale the user sees in the flows, the following is an example of how this can be done:

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    // ...
    ConnectLoginButton button = (ConnectLoginButton) findViewById(R.id.login_button);
    button.setLoginScopeTokens("profile");

    Map<String, String> additionalLoginParams = new HashMap<>();
    additionalLoginParams.put("ui_locales", "bn en");
    button.addLoginParameters(additionalLoginParams)
}
```

#### Customising native loading screen
One can customise the native loading screen that is shown before the WebView has finished loading
when the Chrome Custom Tab feature isn't used in the following way:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // ...
    ConnectLoginButton loginButton = (ConnectLoginButton) findViewById(R.id.login_button);
    loginButton.setCustomLoadingLayout(R.layout.custom_loading_screen);
}
```

Where `R.layout.custom_loading_screen` can be any custom layout (.xml) file you have created.

### Next steps for public clients

#### Keeping track of the login state

The Connect SDK contains the `ConnectTokensStateTracker` class that tracks the login
state of the user. This is useful for handling UI changes in your app based on the login state of
the user.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    new ConnectTokensStateTracker() {
        @Override
        protected void onTokenStateChanged(boolean hasTokens) {
            // App code
        }
};
```

The `onTokenStateChanged(boolean hasTokens)` method will be called whenever the token state changes.

### Retrieving information about the logged in user

If you request user claims like email, phone, and name, using either scope tokens or the claims
parameter, you can access these fields on the user from `ConnectSdk.getIdToken()` after an authorize
request, without having to do any further requests. Setting the scope will give you access to these
fields and setting the claims will make sure that the user has something in these fields, if the
authorize successfully completes. If both email and phone claims have been requested, we will also
provide the username used for the authentication in the ID token.

See docs.telenordigital.com/apis/connect/id/authentication.html for more details.

## Mobile Connect

This SDK may be used for developing apps that authenticate users via Mobile Connect. Mobile Connect is a 
GSMA initiative aimed at providing an authentication mechanism that utilizes mobile phone as an identity 
berarer. More about MobileConnect can be found here: https://mobileconnect.io.

Since virtually everything said thus far about Connect ID applies to MobileConnect as well, you may 
want to see mobileconnect-example app to spot the differences. What follows are some details about the most 
notable ones.

#### Initializing SDK with Mobile Connect

##### Step 1: Implement your own logic for safekeeping secrets in your application

> The example implementation found in the example app is just a simple example. 
> Developers are encouraged to implement their own scheme to keep this information secret.

##### Step 2: initialize SDK at the application startup

```java

appConfig = ...

...

public class ExampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitializeMobileConnect(
                getApplicationContext(),
                OperatorDiscoveryConfig
                        .builder()
                        .endpoint(appConfig.operatorDiscoveryEndpoint())
                        .clientId(appConfig.operatorDiscoveryClientId())
                        .clientSecret(appConfig.operatorDiscoverySecret())
                        .redirectUri(appConfig.operatorDiscoveryRedirectUri())
                        .build());
    }
}
```

#### MobileConnectLoginButton

Use this button instead of `ConnectLoginButton` (previously discussed).



