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
  * [Adding the Client ID and Redirect URI](#adding-the-client-id-and-redirect-uri)
  * [Handling the Redirect URI](#handling-the-redirect-ui)
  * [Adding permissions](adding-permissions)
* [Detailed Usage](#detailed-usage)
  * [Adding a ConnectLoginButton](#adding-a-connectloginbutton)
  * [Next steps for public clients](#next-steps-for-public-clients)
  * [Retrieving information about the logged in user](#retrieving-information-about-the-logged-in-user)
  * [Next steps for confidential clients](#next-steps-for-confidential-clients)
* [Connect Payment](#connect-payment)

The Connect SDK for Android allows developers to create applications that use Telenor Connect for
sign-in or payment. More information about Telenor Connect can be found on our
[partner portal](http://portal.telenordigital.com/).

This SDK uses the Connect ID and Connect Payment APIs. Documentation for these APIs can be found at
[docs.telenordigital.com](http://docs.telenordigital.com).

## Prerequisites

Before being able to use Telenor Connect in your application, you first need to get your
application registered with Telenor Connect. This can be done using a form on
[our website](http://docs.telenordigital.com/getting_started.html).

## Install

The binaries are included on JCenter, so the SDK can be added by including a line in your `build.gradle` file for your app.

```gradle
dependencies {
    // ...
    compile 'com.telenor.connect:connect-android-sdk:0.5.0' // add this line
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
        // that, and transition the app.
        // It checks if the Activity was started by a valid call to the redirect uri with a
        // code and state, for example example-clientid://oauth2callback?code=123&state=xyz ,
        // with a callback, which has a onSuccess and onError function.
        // If it is a success we have tokens, and can transition to SignedInActivity.
        ConnectSdk.checkIntentForAndHandleRedirectUrlCall(getIntent(), new ConnectCallback() {
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
    // checkIntentForAndHandleRedirectUrlCall further up. It is needed on older devices,
    // which doesn't support Chrome Custom Tabs, or if the intent-filter for the redirect uri
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

### Getting a Valid Access Token

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

### Accessing User Information by getUserInfo(…)

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

```xml
<meta-data
        android:name="com.telenor.connect.USE_STAGING"
        android:value="true" />
```

Set this to `false` if you want to use the production environment.

### Select Client type

Connect ID supports two different client types: _public_ and _confidential_. Please see the
[Native app guide](http://docs.telenordigital.com/connect/id/native_apps.html) to help you make a
decision.

If it is a confidential client add the following to the manifest:
```xml
<meta-data
	android:name="com.telenor.connect.CONFIDENTIAL_CLIENT"
	android:value="true" />
```

### Adding the Client ID and Redirect URI

The Connect ID integration requires a Client ID and a redirect URI to work. You should receive these when registering your application.

The Client ID and redirect URI should be added to your `strings.xml` file. Add strings with the names `connect_client_id` and `connect_redirect_uri`.

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

### Handling the Redirect URI

For your app to respond to calls to the redirect uri you need to add an `intent-filter` to your `Activity` to register this in the Android system. This will allow the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs) used by `ConnectLoginButton` and external browsers to get back to your app.

```xml
<activity android:name=".SignInActivity" >
	<intent-filter>
		<data android:scheme="@string/connect_client_id" />
		<action android:name="android.intent.action.VIEW" />
		<category android:name="android.intent.category.DEFAULT" />
		<category android:name="android.intent.category.BROWSABLE" />
	</intent-filter>
</activity>
```

#### Public Client

If the app is a public client this `Activity` should call `ConnectSdk.checkIntentForAndHandleRedirectUrlCall`, as in the [example above](#authenticating-a-user-and-authorizing-app).

#### Confidential Client

A confidential client can access the **code** parameter from the `Intent` as well. The helper method `ConnectSdk.intentHasValidRedirectUrlCall(Intent intent)` will return `true` if a valid code is present in the `Activity`'s `Intent`. The helper method `ConnectSdk.getCodeFromIntent(Intent intent)` can then be used to get the **code**:

```java
Intent intent = getIntent();
if (ConnectSdk.intentHasValidRedirectUrlCall(intent)) {
	String code = ConnectSdk.getCodeFromIntent(intent);
	// App code using code
}
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

The `ConnectActivity` needs to be added to the manifest in order for the Sdk to work on older devices. Also if the `intent-filter` is missing the Sdk will fall back to use this `Activity`. Add it to the `application` section.

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

####

## Detailed Usage

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

If you are developing a confidential client you should skip to [Next steps for confidential clients](#next-steps-for-confidential-clients)

#### Adding claims

To add additional [claims to your Connect request] (http://docs.telenordigital.com/apis/connect/id/authentication.html#authorization-server-user-authorization),
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
One can customise the native loading screen that is shown before the Web View has finished loading when the Chrome Custom Tab isn't used
in the following way:

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


### Next steps for confidential clients

The user's access and refresh tokens are stored in a database controlled by you. The SDK will
return an `access code` in the `onActivityResult()` function. This access code should be exchanged
for access and refresh tokens in your backend system.

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

```xml
<string name="connect_payment_cancel_uri">example-clientid://transactionCancel</string>
<string name="connect_payment_success_uri">example-clientid://transactionSuccess</string>
```

### Editing the application manifest.

Open your application's `AndroidManifest.xml` file and add two `meta-data` entries to the
`application` section of the manifest.

```xml
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

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

And add the `ConnectActivity`, which handles logging in, to the `application` section.

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

### Adding a ConnectPaymentButton

The SDK contains a custom `Button` implementation which contains translations for the Connect
Payment text. This button can be used by adding a `ConnectPaymentButton` to your layout.

Add the button to your layout XML files using the class name
`com.telenor.connect.ConnectPaymentButton`:

```xml
<com.telenor.connect.ConnectPaymentButton
    android:id="@+id/payment_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

### Performing a payment transaction

To perform a transaction you would add a click handler to the payment button in the `onCreate()`
method of your `Activity` class.

```java
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
