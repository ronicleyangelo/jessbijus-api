package br.com.jessbijus.api.controller;

import br.com.jessbijus.api.dto.OrderRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice(assignableTypes = OrderController.class)
final class OrderBodyLimit extends RequestBodyAdviceAdapter {
    private static final int MAX_BYTES = 32 * 1024;

    @Override
    public boolean supports(MethodParameter parameter, Type type, Class<? extends HttpMessageConverter<?>> converter) {
        return type == OrderRequest.class;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter, Type type,
                                          Class<? extends HttpMessageConverter<?>> converter) throws IOException {
        if (input.getHeaders().getContentLength() > MAX_BYTES) throw tooLarge();
        byte[] bytes = input.getBody().readNBytes(MAX_BYTES + 1);
        if (bytes.length > MAX_BYTES) throw tooLarge();
        return new HttpInputMessage() {
            private final InputStream body = new ByteArrayInputStream(bytes);
            public InputStream getBody() { return body; }
            public HttpHeaders getHeaders() { return input.getHeaders(); }
        };
    }

    private static ResponseStatusException tooLarge() {
        return new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE);
    }
}
