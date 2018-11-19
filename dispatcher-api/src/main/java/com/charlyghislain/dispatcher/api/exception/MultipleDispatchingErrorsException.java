package com.charlyghislain.dispatcher.api.exception;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipleDispatchingErrorsException extends DispatcherException {

    private List<Throwable> renderingExceptions = new ArrayList<>();

    public MultipleDispatchingErrorsException(String message) {
        super(message);
    }

    public MultipleDispatchingErrorsException(String message, Throwable... causes) {
        super(message);
        Arrays.stream(causes).forEach(renderingExceptions::add);
    }

    public MultipleDispatchingErrorsException(String message, List<? extends Throwable> errors) {
        super(message);
        this.renderingExceptions.addAll(errors);
    }

    public List<Throwable> getRenderingExceptions() {
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
        s.println("MultiException:: " + internalErrors + "  internal dispatching exceptions: ");
        for (int index = 0; index < internalErrors; index++) {
            Throwable cause = renderingExceptions.get(index);
            s.println("  - " + index + ": " + cause.getMessage());
            cause.printStackTrace(s);
        }
        s.println("MultiException:: completed causes enumeration");
    }
}
