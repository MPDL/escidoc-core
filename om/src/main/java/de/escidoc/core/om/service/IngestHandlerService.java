package de.escidoc.core.om.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.springframework.security.core.context.SecurityContext;

import de.escidoc.core.common.exceptions.EscidocException;

/**
 * Service endpoint interface for IngestHandler.
 */
public interface IngestHandlerService extends Remote {

    String ingest(String xmlData, SecurityContext securityContext) throws EscidocException, RemoteException;

    String ingest(String xmlData, String authHandle, Boolean restAccess) throws EscidocException, RemoteException;

}
