package org.forgerock.openicf.connectors.mParticle;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


public class ApiException extends IOException {
    private final String error;
    private final Integer errorCode;
    private final HttpResponse response;
    private final String body;
    private String errorDescription;

    ApiException(final HttpResponse response) throws IOException {
        super();

        if (response == null) {
            this.response = null;
            this.body = "";
            this.error = "Unknown HTTP error";
            this.errorCode = 0;
        } else {
            this.response = response;
            this.body = EntityUtils.toString(this.response.getEntity());
            this.error = this.response.getStatusLine().getReasonPhrase();
            this.errorCode = this.response.getStatusLine().getStatusCode();
        }

        try {
            final JSONObject jsonBody = new JSONObject(this.body);

            if (jsonBody.has("error")) {
                this.errorDescription = jsonBody.getString("detail");
            } else if (jsonBody.has("message")) {
                this.errorDescription = jsonBody.getString("message");
            } else {
                this.errorDescription = this.error;
            }
        } catch (final JSONException e) {
            if (this.body.isEmpty()) {
                this.errorDescription = this.error;
            } else {
                this.errorDescription = this.body;
            }
        }

        this.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("{0} {1} ({2})", this.errorCode, this.error, this.errorDescription);
    }

    public String getError() {
        return this.error;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    public HttpResponse getResponse() {
        return this.response;
    }

    public String getBody() {
        return this.body;
    }
}
