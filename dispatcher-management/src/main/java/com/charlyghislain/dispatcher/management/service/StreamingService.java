package com.charlyghislain.dispatcher.management.service;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ApplicationScoped
public class StreamingService {

    public StreamingOutput streamOutput(InputStream inputStream) {
        return output -> this.copyStream(inputStream, output);
    }

    public void copyStream(InputStream input, OutputStream output) throws IOException {
        copyStream(input, output, 1024);
    }

    public void copyStream(InputStream input, OutputStream output, int bufferSize) throws IOException {
        try {
            byte[] buffer = new byte[bufferSize];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
}
