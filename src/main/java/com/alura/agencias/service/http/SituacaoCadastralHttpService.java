package com.alura.agencias.service.http;

import com.alura.agencias.domain.http.AgenciaHttp;
import com.alura.agencias.exception.AgenciaNaoEncontradaException;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/situacao-cadastral")
@RegisterRestClient(configKey = "situacao-cadastral-api")
public interface SituacaoCadastralHttpService {

    @GET
    @Path("{cnpj}")
    AgenciaHttp buscarPorCnpj(String cnpj);

    @ClientExceptionMapper
    static RuntimeException toThrowable(Response response) {
        if (response.getStatus() == 500) {
            throw new AgenciaNaoEncontradaException();
        }
        return null;
    }
}
