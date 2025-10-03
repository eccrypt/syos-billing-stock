package web.async;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestTask implements Runnable {
    private AsyncContext asyncContext;
    private RequestHandler handler;

    public RequestTask(AsyncContext asyncContext, RequestHandler handler) {
        this.asyncContext = asyncContext;
        this.handler = handler;
    }

    @Override
    public void run() {
        HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        try {
            handler.handle(request, response);
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (Exception ex) {
            }
        } finally {
            asyncContext.complete();
        }
    }
}