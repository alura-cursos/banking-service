package com.alura.agencias.controller;

import com.alura.agencias.domain.Agencia;
import com.alura.agencias.service.AgenciaService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/agencias")
public class AgenciaController {

    private final AgenciaService agenciaService;

    AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @POST
    public RestResponse<Agencia> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        Agencia novaAgencia = agenciaService.cadastrar(agencia);
        return RestResponse.created(uriInfo.getAbsolutePathBuilder().path(novaAgencia.getId().toString()).build());
    }

    @GET
    @Path("{id}")
    public RestResponse<Agencia> buscarPorId(Long id) {
        Agencia agencia = this.agenciaService.buscarPorId(id);
        return RestResponse.ok(agencia);
    }

    @DELETE
    @Path("{id}")
    public RestResponse<Void> deletar(Long id) {
        this.agenciaService.deletar(id);
        return RestResponse.ok();
    }

    @PUT
    public RestResponse<Agencia> alterar(Agencia agencia) {
        this.agenciaService.alterar(agencia);
        return RestResponse.ok();
    }
}
