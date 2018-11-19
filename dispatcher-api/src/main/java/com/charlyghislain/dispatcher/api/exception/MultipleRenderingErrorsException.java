package com.charlyghislain.dispatcher.api.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipleRenderingErrorsException extends MessageRenderingException {

    private List<MessageRenderingException> renderingExceptions = new ArrayList<>();

    public MultipleRenderingErrorsException(String message) {
        super(message);
    }

    public MultipleRenderingErrorsException(String message, MessageRenderingException... causes) {
        super(message);
        Arrays.stream(causes).forEach(renderingExceptions::add);
    }

    public MultipleRenderingErrorsException(String message, List<MessageRenderingException> renderingExceptions) {
        super(message);
        this.renderingExceptions = renderingExceptions;
    }

    public List<MessageRenderingException> getRenderingExceptions() {
        return renderingExceptions;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }


    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);

        int internalErrors = renderingExceptions.size();
        s.println("MultiException:: " + internalErrors + "  internal rendering exceptions: ");
        for (int index = 0; index < internalErrors; index++) {
            MessageRenderingException cause = renderingExceptions.get(index);
            s.println("  - " + index + ": " + cause.getMessage());
            cause.printStackTrace(s);
        }
        s.println("MultiException:: completed causes enumeration");
    }
}
