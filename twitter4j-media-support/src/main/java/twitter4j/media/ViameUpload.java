/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.media;

import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpParameter;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;

/**
 * @author Makoto Yamazaki - makoto1975 at gmail.com
 */
class ViameUpload extends AbstractImageUploadImpl {

    public ViameUpload(Configuration conf, String apiKey, OAuthAuthorization oauth) {
        super(conf, apiKey, oauth);
    }


    @Override
    protected String postUpload() throws TwitterException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 200) {
            throw new TwitterException("via.me returned invalid status code", httpResponse);
        }

        String response = httpResponse.asString();

        String postId = extractPostId(response);
        return "http://via.me/-" + postId;
    }

    private String extractPostId(String response) throws TwitterException {
        try {
            JSONObject json = new JSONObject(response);
            json = json.getJSONObject("response");
            json = json.getJSONObject("post");
            return json.getString("id");
        } catch (JSONException e) {
            throw new TwitterException("Invalid via.me response: " + response, e);
        }
    }

    @Override
    protected void preUpload() throws TwitterException {
        uploadUrl = "http://api.via.me/v1/upload.json";
        String verifyCredentialsAuthorizationHeader = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON);

        headers.put("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON);
        headers.put("X-Verify-Credentials-Authorization", verifyCredentialsAuthorizationHeader);

        if (null == apiKey) {
            throw new IllegalStateException("No API Key for via.me specified. put media.providerAPIKey in twitter4j.properties.");
        }
        HttpParameter[] params = {
                this.image,
                new HttpParameter("media_type", "photo"),
                new HttpParameter("key", apiKey)};
        if (this.message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    new HttpParameter("text", this.message.getValue())
            }, params);
        }
        this.postParameter = params;
    }
}
