package com.cloud.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public class ErrorFilter extends ZuulFilter{

	private static final Logger logger = LoggerFactory.getLogger(ErrorFilter.class);

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return "error";
	}

	@Override
	public int filterOrder() {
		return -1;
	}

	@Override
	public Object run(){
        try {
            RequestContext ctx = RequestContext.getCurrentContext();
            ZuulException e = (ZuulException)ctx.get("throwable");

            logger.info(ctx.toString());
            if (e != null && e instanceof ZuulException) {
            	ZuulException zuulException = (ZuulException)e;
                logger.error("Zuul failure detected: " + zuulException.getMessage());

                // Remove error code to prevent further error handling in follow up filters
                ctx.remove("throwable");

                // Populate context with new response values
                ctx.setResponseBody("Overriding Zuul Exception Body");
                ctx.getResponse().setContentType("application/json");
                ctx.setResponseStatusCode(500); //Can set any error code as excepted
            }
        }
        catch (Exception ex) {
            logger.error("Exception filtering in custom error filter", ex);
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return null;
    }

}