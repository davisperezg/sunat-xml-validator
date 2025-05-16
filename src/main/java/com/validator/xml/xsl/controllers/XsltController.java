package com.validator.xml.xsl.controllers;
import net.sf.saxon.s9api.*;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.validator.xml.common.models.XmlResponse;
import com.validator.xml.xsl.services.XsltCache;

@RestController
@RequestMapping("/api/xslt")
public class XsltController {
    
    @Autowired
    private XsltCache xsltCache;

    @SuppressWarnings("deprecation")
	@PostMapping("/transform/{validador}/{nombreArchivoEnviado}")
    public XmlResponse transform(
        @RequestBody(required = false) String xml,
        @PathVariable String validador,
        @PathVariable String nombreArchivoEnviado,
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
        	// Obtener el XSLT desde la caché
            XsltExecutable stylesheet = xsltCache.getStylesheet(validador);
            XsltTransformer transformer = stylesheet.load();
            
            // Lista para capturar mensajes emitidos por <xsl:message>
            StringBuilder messageLog = new StringBuilder();

            transformer.setMessageListener(new MessageListener2() {
                @Override
				public void message(XdmNode content, QName errorCode, boolean terminate, SourceLocator locator) {
					// TODO Auto-generated method stub
					messageLog.append(content.getStringValue()).append("\n");
				}
            });
            
            // Establecer la fuente XML y parámetros
            transformer.setSource(new StreamSource(new StringReader(xml)));
            transformer.setParameter(
                    new QName("nombreArchivoEnviado"),
                    new XdmAtomicValue(nombreArchivoEnviado)
            );
            
            // Configurar el serializador de salida
            StringWriter outputWriter = new StringWriter();
            Serializer serializer = xsltCache.getProcessor().newSerializer(outputWriter);
            serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
            
            // Ejecutar la transformación
            transformer.setDestination(serializer);
            transformer.transform();

	        // Procesar la salida para extraer los mensajes de validación
	        if (!messageLog.isEmpty()) {
	        	String[] lines = messageLog.toString().split("\n");
                for (String line : lines) {
                    if (line.contains("error")) {
                        // Parsear cada línea de error como necesites
                    	response.addError(line.trim());
                    }
                }
	        }
            
        } catch (Exception e) {
            response.addError("Error al transformar XML: " + e.getMessage());
        }
        
        return response;
    }
}