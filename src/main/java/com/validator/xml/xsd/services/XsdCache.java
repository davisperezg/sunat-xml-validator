package com.validator.xml.xsd.services;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
public class XsdCache {
	private final Map<String, Schema> schemas = new HashMap<>();

    @PostConstruct
    public void init() {
        // Precarga todos los XSLTs al iniciar el servicio
        loadStylesheet("UBL-Invoice-2.1", "sunat_archivos/sfs/VALI/commons/xsd/2.1/maindoc/UBL-Invoice-2.1.xsd");
    }

    private void loadStylesheet(String name, String classpathPath) {
    	try {
    		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    		
            // Carga el recurso desde el classpath
    		URL resource = getClass().getClassLoader().getResource(classpathPath);
            if (resource == null) {
                throw new RuntimeException("XSD no encontrado en classpath: " + classpathPath);
            }

            // Crea el StreamSource con SystemId
            StreamSource source = new StreamSource(resource.openStream());
            source.setSystemId(resource.toURI().toString()); // Esto es lo más importante

            // Crear el schema desde el XSD
            Schema schema = factory.newSchema(source);

            // Guardar el esquema en el mapa
            schemas.put(name, schema);
            System.out.println("✅ XSD cargado y cacheado: " + name + ".xsd");
        } catch (Exception e) {
        	throw new RuntimeException("❌ Error cargando XSD '" + name + "': " + e.getMessage(), e);
        }
    }

    public Schema getSchema(String name) {
        Schema schema = schemas.get(name);
        if (schema == null) {
            throw new IllegalArgumentException("Validador XSD no encontrado: " + name);
        }
        return schema;
    }
}