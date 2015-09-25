#Connect SDK for Android

The Connect SDK for Android allows developers to create applications which use Telenor Connect ID
for sign-in.

This is an __alpha__ release. In order to guide the development of the SDK and allow you to freely
inspect and use the source, we have open-sourced the SDK. The underlying APIs are generally stable,
however we may make changes to the SDK in response to developer feedback.

## Prerequisites

Before being able to use Telenor Connect ID in your application you first need to get your
application registered with Telenor Connect. This can be done using a form on
[our website](http://docs.telenordigital.com/getting_started.html).

## Application setup

The Connect SDK requires a Client ID and a redirect URI to work. You have received these when
registering your application.

#### Adding the Client ID and redirect URI

The Client ID and redirect URI should be added to your `strings.xml` file. Add strings with the
names `connect_client_id` and `connect_redirect_uri`.

    <string name="connect_client_id">example-clientid</string>
    <string name="connect_redirect_uri">example-clientid://oauth2callback</string>

#### Editing the application manifest.

Open your application's `AndroidManifest.xml` file and add the permission required to allow your
application to access the internet.

    <uses-permission android:name="android.permission.INTERNET"/>

Add two `meta-data` entries to the `application` section of the manifest.

    <application>
    ...
        <meta-data android:name="com.telenor.connect.CLIENT_ID"
            android:value="@string/connect_client_id" />
        <meta-data android:name="com.telenor.connect.REDIRECT_URI"
            android:value="@string/connect_redirect_uri" />
    ...
    </application>

And add the `ConnectActivity`, which handles logging in, to the `application` section.

    <application>
    ...
        <activity
            android:name="com.telenor.connect.ui.ConnectActivity"
            android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
            android:label="@string/app_name"
            android:theme="@android:style/Theme.Holo.Light.NoActionBar" />
    ...
    </application>

## Using the SDK

The Connect SDK needs to be initialized before use. This can be done by adding a call to
`ConnectSdk.sdkInitialize()` in the `onCreate` method in your launch `Activity` or in your
`Application`.

    import com.telenor.connect.ConnectSDK
    ...
    @Override
    public void onCreate() {
        super.onCreate();
        ConnectSdk.sdkInitialize(getApplicationContext());
    }

#### Adding a ConnectLoginButton

To let the SDK handle Connect ID login a `ConnectLoginButton` can be added to your layout. This is
a custom `Button` implementation that has the standard Connect button look-and-feel.

Firstly add a button to your layout XML files using the class name
`com.telenor.connect.ConnectLoginButton`:

    <com.telenor.connect.ConnectLoginButton
        android:id="@+id/login_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

Then add the required [scope tokens](http://docs.telenordigital.com/connect/id/scope.html) for your
application to the button in the `onCreate()` method of your `Activity` class.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        ConnectLoginButton button = (ConnectLoginButton) findViewById(R.id.login_button);
        button.setLoginScopeTokens("profile");
    }

The `onActivityResult()` method of your `Activity` will be called with `Activity.RESULT_OK` as
`resultCode` if login was successful or `Activity.RESULT_CANCELED` when there was an error.

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // App code
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // App code
        }
    }

#### Keeping track of the login state

The Connect SDK contains the `ConnectTokensStateTracker` class, which exposes the current login
state of the user. This is useful for handling UI changes in your app based on the login state of
the user.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    ...
    new ConnectTokensStateTracker() {
        @Override
        protected void onTokenStateChanged(boolean hasTokens) {
            // App code
        }
    };

The `onTokenStateChanged(boolean hasTokens)` method will be called when instantiating the
`ConnectTokenStateTracker` class and whenever the token state changes.

#### Retrieving the access token

The current Connect ID access token can be retrieved using the `ConnectSdk.getAccessToken()`
method. When the token has expired a new set of tokens can be requested using
`ConnectSdk.updateTokens()`.

Access tokens can be used to access resources on your resource server. Please refer to the document
about [scope tokens](http://docs.telenordigital.com/connect/id/scope.html) for more details.
