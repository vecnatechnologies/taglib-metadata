/**
 * Copyright 2011 Vecna Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
*/

package com.vecna.taglib.processor;

import java.io.File;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.common.collect.ImmutableMap;
import com.vecna.taglib.model.JspTaglibModel;

/**
 * Marshals JSP taglib model into a taglib descriptor xml (TLD)
 * @author ogolberg@vecna.com
 */
public class JspModelMarshaller {
  private static class NamespaceMapping {
    private final String m_uri;
    private final String m_location;
    
    NamespaceMapping(String uri, String location) {
      m_uri = uri;
      m_location = location;
    }
    
    String getLocation() {
      return m_location;
    }
    
    String getUri() {
      return m_uri;
    }
    
    String getLocationAttribute() {
      return m_uri + " " + m_location;
    }
  }
  
  public static class JspMarshallerException extends Exception {
    public JspMarshallerException(Throwable cause) {
      super(cause);
    }
    public JspMarshallerException(String message) {
      super(message);
    }
    public JspMarshallerException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  private final Map<String, NamespaceMapping> m_schemaLocations = 
    ImmutableMap.of(JspTaglibModel.VERSION_20, new NamespaceMapping("http://java.sun.com/xml/ns/j2ee", 
                                                                    "web-jsptaglibrary_2_0.xsd"));


  /**
   * Marshal a taglib model into a TLD document
   * @param model taglib model
   * @param out the class to write the TLD document to
   * @param loader the classloader to use
   * @throws JspMarshallerException if a marshalling error occurs
   */
  public void marshal(JspTaglibModel model, File out, ClassLoader loader) throws JspMarshallerException {
    try {
      NamespaceMapping namespaceMapping = m_schemaLocations.get(model.jspVersion);
      if (namespaceMapping == null) {
        throw new JspMarshallerException("JSP version " + model.jspVersion + " is not supported");
      }

      JAXBContext context = JAXBContext.newInstance("com.vecna.taglib.model", loader);
      
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, namespaceMapping.getLocationAttribute());
      
      marshaller.marshal(model, out);
    } catch (JAXBException e) {
      throw new JspMarshallerException(e);
    }
  }
  
  /**
   * Marshal a taglib model into a TLD document using current thread's context classloader
   * @param model taglib model
   * @param out the class to write the TLD document to
   * @throws JspMarshallerException if a marshalling error occurs
   */
  public void marshal(JspTaglibModel model, File out) throws JspMarshallerException {
    marshal(model, out, Thread.currentThread().getContextClassLoader());
  }
}
