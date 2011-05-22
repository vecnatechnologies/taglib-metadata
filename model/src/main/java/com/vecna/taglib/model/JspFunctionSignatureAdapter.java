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

package com.vecna.taglib.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Converts between {@link JspFunctionSignature} model objects and JSP-compatible string representations.
 * @author ogolberg@vecna.com
 */
public class JspFunctionSignatureAdapter extends XmlAdapter<String, JspFunctionSignature> {
  private String getJspTypeName(Class<?> type) {
    if (type.isArray()) {
      return type.getComponentType().getName() + "[]";
    } else {
      return type.getName();
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String marshal(JspFunctionSignature signature) throws Exception {
    StringBuilder builder = new StringBuilder();
    builder.append(getJspTypeName(signature.returnType)).append(" ").append(signature.name).append("(");
    for (int i = 0; i < signature.argumentTypes.length; i++) {
      builder.append(getJspTypeName(signature.argumentTypes[i]));
      if (i != signature.argumentTypes.length - 1) {
        builder.append(", ");
      }
    }
    return builder.append(")").toString();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public JspFunctionSignature unmarshal(String signature) throws Exception {
    throw new UnsupportedOperationException("not implemented yet");
  }
}   
