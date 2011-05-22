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

package com.vecna.taglib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * Annotation for JSP tag classes
 * @author ogolberg@vecna.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JspTag {
  /**
   * Tag name
   */
  String name();
  /**
   * Body content type
   */
  JspTagBodyContent bodyContent() default JspTagBodyContent.empty;
  /**
   * Tag variables
   */
  JspVariable[] variables() default {};
  /**
   * Whether this tag handles dynamic attributes
   */
  boolean dynamicAttributes() default false;
}
