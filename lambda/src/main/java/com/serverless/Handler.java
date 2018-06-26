package com.serverless;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Handler implements RequestStreamHandler {

    private static final Logger log = LoggerFactory.getLogger(Handler.class);

    private static final JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler
            = JerseyLambdaContainerHandler.getAwsProxyHandler(JerseyConfig.JERSY_APPLICATION);


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        log.info("received request");

        handler.proxyStream(input, output, context);

        IOUtils.closeQuietly(output);
    }
}
