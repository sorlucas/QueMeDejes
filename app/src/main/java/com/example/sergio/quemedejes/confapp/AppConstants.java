/* Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sergio.quemedejes.confapp;

// Replace the next import with your library (it will have your appspot project ID)

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

/**
 * Application constants.
 */
public class AppConstants {
    /**
     * Your WEB CLIENT ID from the API Access screen of the Developer Console for your project. This
     * is NOT the Android client id from that screen.
     *
     * @see <a href="https://developers.google.com/console">https://developers.google.com/console</a>
     */
    public static final String WEB_CLIENT_ID = "732155756921-56040206vk5docd2h91kqqurpcu41b70.apps.googleusercontent.com";
    /**
     * The audience is defined by the web client id, not the Android client id.
     */
    public static final String AUDIENCE = "server:client_id:" + WEB_CLIENT_ID;

    /**
     * Class instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();

    /**
     * Class instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

}