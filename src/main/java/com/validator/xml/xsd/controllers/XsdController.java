package com.validator.xml.xsd.controllers;

import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.validator.xml.common.models.XmlResponse;
import com.validator.xml.xsd.services.XsdCache;

@RestController
@RequestMapping("/api/xsd")
public class XsdController {
    
    @Autowired
    private XsdCache xsdCache;

	@PostMapping("/validate/{schemaName}")
    public XmlResponse transform(
        @RequestBody(required = false) String xml,
        @PathVariable String schemaName,
        @RequestHeader(value = "Content-Type", required = false) String contentType
    ) {
    	XmlResponse response = new XmlResponse();
    	
    	// Verificar si no se proporcionó Content-Type
        if (contentType == null || contentType.isEmpty()) {
            response.addError("El encabezado 'Content-Type' es requerido.");
            return response;
        }
        
    	// Verifica que el Content-Type sea application/xml
        if (!contentType.equals("application/xml")) {
            response.addError("El tipo de contenido debe ser 'application/xml'.");
            return response;
        }
        
    	// Verifica si el XML está vacío o nulo
        if (xml == null || xml.trim().isEmpty()) {
        	response.addError("El cuerpo de la solicitud no contiene un XML válido.");
        	return response;
        }
        
        try {
        	// Obtener el XSD del cache
            Schema schema = xsdCache.getSchema(schemaName);
            
            // Crear un validador usando el XSD
            Validator validator = schema.newValidator();
            
            // Validar el XML
            validator.validate(new StreamSource(new StringReader(xml)));
            
        } catch (Exception e) {
            response.addError("Error al validar XML: " + e.getMessage());
        }
        
        return response;
    }
}