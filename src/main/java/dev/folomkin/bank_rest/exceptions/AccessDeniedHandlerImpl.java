package dev.folomkin.bank_rest.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        try {
            ObjectMapper mapper = new ObjectMapper();
            ProblemDetail problemDetail =
                    ProblemDetail
                            .forStatusAndDetail(HttpStatus.FORBIDDEN,
                    "У вас нет прав доступа к данному ресурсу");
            problemDetail.setTitle("Отсутствие прав доступа");
            mapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), problemDetail);
        } catch (Exception e) {
            throw new ServletException();
        }
    }
}
