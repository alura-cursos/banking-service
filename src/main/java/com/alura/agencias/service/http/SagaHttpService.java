package com.alura.agencias.service.http;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/saga")
@RegisterRestClient(configKey = "situacao-cadastral-api")
public interface SagaHttpService {

    @PUT
    Uni<Void> fecharSaga(String id);
}
