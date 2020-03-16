## Adding SDK to Android Project

Please use the latest release .aar file

### 1. Adding to Project
It can be added to an android project with gradle like:

```
dependencies {
    implementation(name:'engagement', ext:'aar')
}
```


### 2. Config
To setup the library a api token and a websocket url must be provided,
this should be added through the res/values/strings.xml file

```
<string name="rm_ee_url_websocket">%provided by vendor%</string>
<string name="rm_ee_api_token”>%provided by library vendor%</string>
```

Further customisation:

Thousand seperator, configurable in res/values/strings.xml
```
<string name="rm_ee_thousand_seperator">.</string>
```

Color of the points Text in res/values/colors.xml
```
<color name="rm_ee_points_color">#000000</color>
```


### 3. Initialization 
For tracking the usetime it is necessary to initialize the library within the Application class.
An description is found [here](https://github.com/codepath/android_guides/wiki/Understanding-the-Android-Application-Class)


The library is initialized in the onCreate method of the Application class and looks like this:

```
@Override
public void onCreate() {
    super.onCreate();
    EngagementEngine.getInstance().setup(this);
}
```

### 4. Adding a View for displaying the currency
```
LinearLayout dniView = findViewById(R.id.dniView);
EngagementEngine.getInstance().addCurrencyView(dniView);
```

### 5. Adding challenges 
For every Activity where Challenges can be present, the window should be added like

```
View window = getWindow().getDecorView();
String url = "is provided by library vendor";
EngagementEngine.getInstance().registerChallengesFor( window, url, "detail" );
```

### 6. Tracking Events

- Scroll Events:

Scroll Events are automatically tracked

- Click Events:

As there is only ONE click event that can be bound to a view, the 
triggering the api must happen inside the click listener in the application like:

```
View.OnClickListener myhandler = new View.OnClickListener() {
    public void onClick(View view) {
      EngagementEngine.getInstance().fireChallenge(view.getId());
    }
  }

```

### 7. Switch the language
You can call method updateLanguage(lang: String) on running session, to switch language of TEE UI for currently active collector token.
```
EngagementEngine.getInstance().updateLanguage("de")
```

### 8. handle Push Registration
You may want to notify users, with targeting based on your profile. You can associate push notification token with a specific user.

```
EngagementEngine.getInstance().handlePushRegistration(pushToken);
````

You can also optionally call the method with an additional alias.


```
EngagementEngine.getInstance().handlePushRegistration(pushToken, pushAlias);
````

### 9. Open overview page with deeplink

For opening TEE overview page on specific position you can use handleDeepLink(String entryPoint, String additionalParams) where entryPoint correspond to the relevant section, and 
additionalParams are the concatenated Url Params which are added to the Url.

How to create deeplinks is desrcibed here:
https://developer.android.com/training/app-links/deep-linking

How to create applinks is desrcibed here:
https://developer.android.com/training/app-links

Example:

Deeplinks:

You want to open the rewards page, the entrypoint for that is "/me/rewards"
So your deeplink would look like:

scheme://your.app/open-tee/webview?entry=/me/rewards

where the part of 
scheme://your.app/open-tee/webview

is your actual deeplink structure you can define by yourself

In you app you parse the ?entry= parameter and call

```
EngagementEngine.getInstance().handleDeepLink("/me/rewards", null);
```

or if you want to add additional Params:

```
EngagementEngine.getInstance().handleDeepLink("/me/rewards", "&param1=one&param2=two");
```

The same is for applinks which look like that:

https://yourwebsite/open-gamification/webview?entry=/me/rewards


Possible enty points are:

/me/rewards  
/me/challenges  
/me/overview  
/me/challenges  
/me/rewards  
/me/rewards/detail/123  
/me/posts/123
