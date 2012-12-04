package uk.co.desirableobjects.sendgrid

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.params.HttpParams
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import uk.co.desirableobjects.sendgrid.exception.MissingCredentialsException

class SendGridApiConnectorService {

    private RESTClient sendGrid = new RESTClient(CH.config.sendgrid?.api?.url ?: 'https://sendgrid.com/api/') {
        @Override
        protected AbstractHttpClient createClient(HttpParams params) {
            new DefaultHttpClient(new PoolingClientConnectionManager(), params)
        }
    }

    def post(SendGridEmail email) {

        def response = sendGrid.post(
                path: 'mail.send.json',
                body: prepareParameters(email),
                requestContentType: ContentType.URLENC,
        )
        handle(response)

    }

    private SendGridResponse handle(clientResponse) {

        return SendGridResponse.parse(clientResponse.data)

    }

    Map<String, Object> prepareParameters(SendGridEmail email) {

        if (!CH.config.sendgrid.password || !CH.config.sendgrid.username) {
            throw new MissingCredentialsException()
        }

        Map<String, Object> parameters = email.toMap()
        parameters.put('api_user', CH.config.sendgrid?.username)
        parameters.put('api_key', CH.config.sendgrid?.password)

        return parameters

    }

}
