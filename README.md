# Connect SDK for Android

* [Prerequisites](#prerequisites)
* [Install](#install)
* [Basic Usage](#basic-usage)
  * [Authenticating a User and Authorizing App](#authenticating-a-user-and-authorizing-app)
  * [Getting a Valid Access Token](#getting-a-valid-access-token)
  * [Access User Information](#access-user-information)
  * [Accessing User Information by getUserInfo(…)](#accessing-user-information-by-getuserinfo)
* [Example App](#example-app)
* [Setup](#setup)
  * [Set Staging or Production Environment](#set-staging-or-production-environment)
  * [Select Client type](#select-client-type)
  * [Adding the Client ID and redirect URI](#adding-the-client-id-and-redirect-uri)
  * [Adding permissions](adding-permissions)
* [Detailed Usage](#detailed-usage)
  * [Adding a ConnectLoginButton](#adding-a-connectloginbutton)
  * [Next steps for public clients](#next-steps-for-public-clients)
  * [Retrieving information about the logged in user](#retrieving-information-about-the-logged-in-user)
* [Connect Payment](#connect-payment)

The Connect SDK for Android allows developers to create applications that use Telenor Connect for
sign-in or payment. More information about Telenor Connect can be found on our
[partner portal](http://portal.telenordigital.com/).

This SDK uses the Connect ID and Connect Payment APIs. Documentation for these APIs can be found at
[docs.telenordigital.com](http://docs.telenordigital.com).

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
    compile 'com.telenor.connect:connect-android-sdk:0.7.0' // add this line
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

## Basic Usage

Notice: The AndroidManifest.xml needs to be [setup](#setup) before you can use the SDK.

### Authenticating a User and Authorizing App

You can authenticate the user and authorize your application by using a `ConnectLoginButton`:


```Java
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
    }

    // onActivityResult will be called once the login has completed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(getApplicationContext(), SignedInActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
```

Where `activity_sign_in.xml` looks like this:
```XML
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

### Getting a Valid Access Token

Once the user is signed in you can get a valid Access Token by calling `ConnectSdk.getValidAccessToken(…)`:

```Java
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

```Java
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
```Java
IdToken idToken = ConnectSdk.getIdToken();
```

And access user information by calling for example:
```Java
String email = idToken.getEmail();
```

### Accessing User Information by getUserInfo(…)

You can also access user information by making a network call using `getUserInfo(…)`:

```Java
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

## Example App

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

```XML
<meta-data
        android:name="com.telenor.connect.USE_STAGING"
        android:value="true" />
```

Set this to `false` if you want to use the production environment.

### Select Client type

Connect ID supports two different client types: _public_ and _confidential_. Please see the
[Native app guide](http://docs.telenordigital.com/connect/id/native_apps.html) to help you make a
decision.

#### Confidential clients

The SDK will return an _authorization code_ in the `onActivityResult()` method. This authorization
code must be sent to the server-side part of your client.

Add the following to the manifest:
```XML
<meta-data
	android:name="com.telenor.connect.CONFIDENTIAL_CLIENT"
	android:value="true" />
```

Then the `onActivityResult(…)` method will have to be changed to send the authorization code to
the server-side part of the client:
```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
        return;
    }

    final String authorizationCode = data.getStringExtra("code");

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

#### Public clients

Either not include the `meta-data` for `com.telenor.connect.CONFIDENTIAL_CLIENT` or explicitly set
it to `false` (this is the default).


### Adding the Client ID and redirect URI

The Connect ID integration requires a Client ID and a redirect URI to work. You should receive these when registering your application.

The Client ID and redirect URI should be added to your `strings.xml` file. Add strings with the names `connect_client_id` and `connect_redirect_uri`.

```XML
<string name="connect_client_id">example-clientid</string>
<string name="connect_redirect_uri">example-clientid://oauth2callback</string>
```

Add `meta-data` entries to the `application` section of the manifest.

```XML
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

### Adding permissions

Open your application's `AndroidManifest.xml` file and add the permission required to allow your
application to access the internet.

```XML
<uses-permission android:name="android.permission.INTERNET"/>
```

Optionally you can enable the feature that automatically fills in verification PIN codes received
on SMS by adding the following permissions.

```XML
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
```

Note: You should be conscious about the security implications of using this feature. When using this feature your application will load received SMS into memory for up to 60 seconds. Upon finding an SMS with the word `CONNECT` and a PIN-code, the PIN code will be parsed and passed back to a callback JavaScript function. More discussion can be found in issue [#15](https://github.com/telenordigital/connect-android-sdk/issues/15).


#### Add ConnectActivity for Sign In

The `ConnectActivity` needs to be added to the manifest in order to work. Add it to the `application` section.

```XML
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

## Detailed Usage

### Adding a ConnectLoginButton

To let the SDK handle Connect ID login, a `ConnectLoginButton` can be added to your layout. This is
a custom `Button` implementation that has the standard Connect button look-and-feel.

Firstly add a button to your layout XML files using the class name
`com.telenor.connect.ConnectLoginButton`:

```XML
<com.telenor.connect.ConnectLoginButton
    android:id="@+id/login_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

Then add the required [scope tokens](http://docs.telenordigital.com/connect/id/scope.html) for your
application to the button in the `onCreate()` method of your `Activity` class.

```Java
@Override
public void onCreate(Bundle savedInstanceState) {
    ...
    ConnectLoginButton button = (ConnectLoginButton) findViewById(R.id.login_button);
    button.setLoginScopeTokens("profile");
}
```

The `onActivityResult()` method of your `Activity` will be called with `Activity.RESULT_OK` as
`resultCode` if the login was successful or `Activity.RESULT_CANCELED` when there was an error.

```Java
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

#### Adding claims

To add additional [claims to your Connect request] (http://docs.telenordigital.com/apis/connect/id/authentication.html#authorization-server-user-authorization),
you can use the `setClaims` method on the `ConnectLoginButton`.

```Java
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

```Java
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
One can customise the native loading screen that is shown before the Web View has finished loading
in the following way:

```Java
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

```Java
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

#### Retrieving the access token

The current Connect ID access token can be retrieved using the `ConnectSdk.getAccessToken()`
method. When the token has expired a new set of tokens can be requested using
`ConnectSdk.updateTokens()`.

Access tokens can be used to access resources on your resource server. Please refer to the document
about [scope tokens](http://docs.telenordigital.com/connect/id/scope.html) for more details.

### Retrieving information about the logged in user

If you request user claims like email, phone, and name, using either scope tokens or the claims
parameter, you can access these fields on the user from `ConnectSdk.getIdToken()` after an authorize
request, without having to do any further requests. Setting the scope will give you access to these
fields and setting the claims will make sure that the user has something in these fields, if the
authorize successfully completes. If both email and phone claims have been requested, we will also
provide the username used for the authentication in the ID token.

See docs.telenordigital.com/apis/connect/id/authentication.html for more details.

## Connect Payment

Connect Payment allows users to pay for content in your app. Connect Payment uses transactions and
subscriptions (recurring transactions) to manage payments. Transactions and subscriptions are
created from your app's backend system. Please see the
[Connect Payment documentation](http://docs.telenordigital.com/connect/payment/) for more details.

### Adding Payment success and cancel URIs

Whenever a payment transaction is completed successfully or a payment is
cancelled, your backend should redirect to your application. There is a separate redirect for
a successful or cancelled transaction. The success and cancel redirect URIs should be added to your
application's `strings.xml` file. Add strings with the names `connect_payment_cancel_uri` and
`connect_payment_success_uri`.

```XML
<string name="connect_payment_cancel_uri">example-clientid://transactionCancel</string>
<string name="connect_payment_success_uri">example-clientid://transactionSuccess</string>
```

### Editing the application manifest.

Open your application's `AndroidManifest.xml` file and add two `meta-data` entries to the
`application` section of the manifest.

```XML
<application>
...
    <meta-data
        android:name="com.telenor.connect.PAYMENT_CANCEL_URI"
        android:value="@string/connect_payment_cancel_uri" />
    <meta-data
        android:name="com.telenor.connect.PAYMENT_SUCCESS_URI"
        android:value="@string/connect_payment_success_uri" />
...
</application>
```

If you are not using Connect ID to sign users into your application you should also add the
permission required to allow your application to access the internet.

```XML
<uses-permission android:name="android.permission.INTERNET"/>
```

And add the `ConnectActivity`, which handles logging in, to the `application` section.

```XML
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

### Adding a ConnectPaymentButton

The SDK contains a custom `Button` implementation which contains translations for the Connect
Payment text. This button can be used by adding a `ConnectPaymentButton` to your layout.

Add the button to your layout XML files using the class name
`com.telenor.connect.ConnectPaymentButton`:

```XML
<com.telenor.connect.ConnectPaymentButton
    android:id="@+id/payment_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### Performing a payment transaction

To perform a transaction you would add a click handler to the payment button in the `onCreate()`
method of your `Activity` class.

```Java
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    View paymentButton = findViewById(R.id.payment_button);
    paymentButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // App code
        }
    });
}
```

This click handler would call your backend to let the backend create a transaction and you would
return the [Payment link](http://docs.telenordigital.com/apis/connect/payment/#single-payment-transaction)
to your application. The Payment link is then used in a call to
`ConnectSdk.initializePayment(Context, String)`, which opens a `ConnectActivity` where the user can
perform the transaction.


## Mobile Connect

This SDK may be used for developing apps that authenticate users via MobileConnect. MobileConnect is a 
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

```Java

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



